# logic-taglib Specification

## Purpose

Тэг-библиотека `struts-logic` (URI `http://struts.apache.org/tags-logic`): итерация по коллекциям, сравнения, сопоставление, проверка присутствия и пустоты, переходы redirect/forward — с TLD, именами и поведением эталона weblegacy.

## Requirements

### Requirement: Идентичность TLD, URI и имён тегов
TLD библиотеки SHALL быть идентична эталонной weblegacy (`struts-logic.tld`, 17 тегов): тот же URI `http://struts.apache.org/tags-logic`, те же имена тегов и атрибуты; декларация в JSP — `<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>`.

#### Scenario: Подключение и компиляция JSP
- **WHEN** JSP декларирует библиотеку по URI `http://struts.apache.org/tags-logic` и использует `logic:iterate`
- **THEN** JSP компилируется и тег обрабатывается без дополнительных объявлений

### Requirement: Итерация тегом logic:iterate
`logic:iterate` SHALL повторять тело для каждого элемента коллекции, массива, карты или `Enumeration` (по `name`/`property` или вложенному набору), определяя переменную `id` с текущим элементом (и `indexId` с индексом); атрибуты `offset` и `length` ограничивают диапазон.

#### Scenario: Итерация по коллекции
- **WHEN** в области лежит список из трёх строк, и JSP содержит `<logic:iterate id="item" name="list"><bean:write name="item"/>;</logic:iterate>`
- **THEN** тело выведено трижды со значениями элементов через точку с запятой

#### Scenario: Индекс итерации
- **WHEN** JSP содержит `<logic:iterate id="item" name="list" indexId="i">…</logic:iterate>`
- **THEN** переменная `i` содержит 0, 1, 2 по итерациям

### Requirement: Сравнения значений
Теги `equal`, `notEqual`, `greaterEqual`, `lessEqual`, `greaterThan`, `lessThan` SHALL обрабатывать тело, когда значение (из `name`/`property`, `parameter`, `cookie` или `header`) сравнимо с `value` указанным способом; нечисловые значения сравниваются как строки.

#### Scenario: Равенство параметра запроса
- **WHEN** запрос содержит параметр `page=3`, и JSP содержит `<logic:equal parameter="page" value="3">shown</logic:equal>`
- **THEN** тело выведено

#### Scenario: Неравенство свойства
- **WHEN** свойство бина `status = "closed"`, и JSP содержит `<logic:notEqual name="bean" property="status" value="open">shown</logic:notEqual>`
- **THEN** тело выведено

### Requirement: Сопоставление строк
Теги `match` и `notMatch` SHALL обрабатывать тело, когда значение совпадает (или не совпадает) с `value`; атрибут `location` ограничивает проверку началом (`start`) или концом (`end`) строки.

#### Scenario: Совпадение начала строки
- **WHEN** свойство бина `path = "/admin/users"`, и JSP содержит `<logic:match name="bean" property="path" value="/admin" location="start">shown</logic:match>`
- **THEN** тело выведено

### Requirement: Проверки присутствия и пустоты
Теги `present`/`notPresent` SHALL обрабатывать тело в зависимости от наличия бина (по `name` в области или `property`), параметра запроса (`parameter`), заголовка (`header`), cookie (`cookie`) или атрибута конфигурации; теги `empty`/`notEmpty` — по пустоте значения (null, пустая строка, пустая коллекция); `messagesPresent`/`messagesNotPresent` — по наличию сообщений/ошибок в запросе (с фильтром `property`).

#### Scenario: Присутствие атрибута приложения
- **WHEN** JSP содержит `<logic:notPresent name="org.apache.struts.action.MESSAGE" scope="application">ERROR</logic:notPresent>` (как Welcome.jsp эталона)
- **THEN** при загруженных message-resources тело не выводится, а при их отсутствии — выводится

#### Scenario: Пустое значение
- **WHEN** свойство бина `note = ""`, и JSP содержит `<logic:empty name="bean" property="note">shown</logic:empty>`
- **THEN** тело выведено

### Requirement: Переходы redirect и forward
`logic:redirect` SHALL выполнять HTTP-редирект на ресурс по `forward`, `action` или `href` (с параметрами и кодированием); `logic:forward` SHALL выполнять форвард контроллера по имени глобального форварда (или локального).

#### Scenario: Редирект по глобальному форварду
- **WHEN** глобальный форвард `welcome` ведёт на `/Welcome.do`, и JSP (index.jsp) содержит `<logic:redirect forward="welcome"/>`
- **THEN** клиент получает HTTP-редирект на `/ctx/Welcome.do`

#### Scenario: Форвард по имени
- **WHEN** JSP содержит `<logic:forward name="welcome"/>`
- **THEN** обработка передана ресурсу глобального форварда `welcome`
