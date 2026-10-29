# Spec Delta: struts-validator

## Purpose

Декларативная валидация форм commons-validator: правила объявляются в `validation.xml` поверх `validator-rules.xml`, подключаются декларативно `ValidatorPlugIn` через struts-config.xml, ошибки попадают в стандартный цикл Struts (input-страница, `html:errors`), клиентские скрипты генерирует `html:javascript` — с поведением эталона weblegacy.

## ADDED Requirements

### Requirement: Подключение валидатора плагином
`ValidatorPlugIn` SHALL подключаться декларативно элементом `<plug-in>` struts-config.xml с init-параметрами `pathnames` (список ресурсов через запятую: validator-rules.xml и validation.xml) и `stopOnFirstError`. Ресурсы SHALL загружаться при старте; повторное объявление плагина в модуле SHALL быть ошибкой старта.

#### Scenario: Декларативное подключение
- **WHEN** struts-config.xml объявляет `ValidatorPlugIn` с `pathnames` на validator-rules.xml и validation.xml
- **THEN** валидатор инициализирован при старте, формы по правилам валидируются без дополнительного кода

#### Scenario: Отсутствие ресурса pathnames
- **WHEN** `pathnames` указывает на несуществующий ресурс
- **THEN** старт приложения завершается ошибкой с именем ресурса

### Requirement: Валидация ValidatorForm по правилам
Форма `ValidatorForm` (и её подклассы) SHALL валидироваться по правилам validation.xml для её имени-ключа: ключ — атрибут формы из конфигурации (`mapping.getAttribute()`); обнаруженные нарушения SHALL вернуть `ActionErrors` с сообщениями правил, которые стандартный цикл направляет на input-страницу; при отсутствии нарушений экшен вызывается.

#### Scenario: Обязательное поле не заполнено
- **WHEN** поле объявлено `depends="required"` и отправлено пустым, а маппинг использует `validate="true"` с input-страницей
- **THEN** экшен не вызывается, на input-странице рендерится `html:errors` с сообщением ключа `errors.required` и меткой поля из `<arg>`

#### Scenario: Успешная валидация
- **WHEN** все поля формы удовлетворяют правилам
- **THEN** validate возвращает без ошибок и экшен вызывается

### Requirement: Набор правил FieldChecks
Набор правил `validator-rules.xml` эталона SHALL работать из коробки: required, requiredif, mask, minlength, maxlength, byte/short/integer/long/float/double (включая *Locale-варианты), date, intRange/longRange/floatRange/doubleRange, creditCard, email, url, validwhen. Сообщения SHALL браться по ключам правил (`errors.required`, `errors.invalid`, `errors.range` и т.д.) из message-resources, аргументы — из `<arg>` поля (позиции 0–3, поддержка литералов и `${var:...}`).

#### Scenario: Правило mask
- **WHEN** поле объявлено `depends="required,mask"` с `var-name="mask"`, и значение не соответствует регулярному выражению
- **THEN** на input-странице выведено сообщение ключа `errors.invalid` с настроенным `<msg>`/`<arg>` (включая бандл из атрибута `bundle`)

#### Scenario: Правило диапазона
- **WHEN** поле объявлено `depends="intRange"` с переменными min/max, и значение вне диапазона
- **THEN** выведено сообщение `errors.range` с подставленными min/max из `<arg>` с `${var:min}`/`${var:max}`

#### Scenario: Правило validwhen
- **WHEN** поле объявлено `depends="validwhen"` с выражением-условием (`<var-name>test</var-name>`), и условие нарушено
- **THEN** поле помечено ошибкой `errors.required`

### Requirement: Мультистраничная валидация page
Поля validation.xml с атрибутом `page` SHALL валидироваться по значению `page` формы: валидируются поля, чей page не больше текущего; при необъявленном ограничении страниц маппинга валидируются все поля. Формы `ValidatorActionForm`/`DynaValidatorActionForm` SHALL использовать ключом валидации путь маппинга (`mapping.getPath()`).

#### Scenario: Вторая страница многостраничной формы
- **WHEN** multi-page форма отправляет `page=2`, а маппинг ограничивает допустимые страницы
- **THEN** валидируются только поля page ≤ 2; поля page 3 не валидируются

#### Scenario: Ключ по действию
- **WHEN** маппинг использует форму типа `ValidatorActionForm`, и в validation.xml правила объявлены по имени пути действия
- **THEN** валидация выполнена по правилам этого действия

### Requirement: Валидация DynaValidatorForm
`DynaValidatorForm` (динамическая форма-наследник `DynaActionForm`) SHALL валидироваться по правилам validation.xml так же, как статическая `ValidatorForm`, без Java-класса формы; свойства берутся из `<form-property>`, ключ валидации — атрибут формы из конфигурации.

#### Scenario: Динамическая форма по правилам
- **WHEN** struts-config.xml объявляет form-bean типа `DynaValidatorForm`, validation.xml содержит правила для его имени, и запрос отправляет невалидные значения
- **THEN** на input-странице выведены ошибки правил, экшен не вызван

#### Scenario: Наследование form-bean с валидацией
- **WHEN** form-bean-наследник (`extends`) расширяет базовую `DynaValidatorForm` дополнительными свойствами, и validation.xml объявляет правила по имени наследника
- **THEN** валидируются и унаследованные, и собственные свойства

### Requirement: Пользовательские валидаторы
Пользовательские правила SHALL подключаться секцией `<global><validator>` в validation.xml: имя правила, класс и метод, ключ сообщения; такие правила SHALL применяться в `depends` наравне со встроенными.

#### Scenario: Кастомное правило
- **WHEN** validation.xml объявляет глобальный валидатор `twofields` с пользовательским классом, и поле объявляет `depends="twofields"`
- **THEN** метод пользовательского класса вызван, при нарушении выведено сообщение его ключа

### Requirement: Клиентские скрипты html:javascript
Тег `html:javascript` SHALL генерировать клиентские скрипты commons-validator по правилам validation.xml: статический/динамический режим, форма по `formName` (или атрибуту формы контекста), методы полей по их правилам; вывод соответствует эталону (функции валидации формы, `required`-поля, JS-утилиты правил).

#### Scenario: Генерация скрипта формы
- **WHEN** JSP содержит `<html:javascript formName="registrationForm"/>`, и validation.xml объявляет правила этой формы
- **THEN** страница содержит JavaScript-функцию валидации формы с проверками объявленных правил

#### Scenario: StaticJavascript
- **WHEN** JSP запрашивает статические JS-утилиты валидатора (как StaticJavascript.jsp mailreader)
- **THEN** отдан скрипт утилит commons-validator без привязки к конкретной форме
