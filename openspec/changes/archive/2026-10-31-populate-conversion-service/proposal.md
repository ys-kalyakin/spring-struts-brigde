# Proposal

## Why

Populate форм — единственное место движка, где обработка запроса зависит от статического глобального реестра `ConvertUtils` commons-beanutils: известный риск из роадмапа (пересечение регистраций между приложениями, скрытая изменяемость, невозможность настройки через Spring). Роадмап дважды откладывал переход populate на Spring `ConversionService` (фаза 1, фаза 3); все фазы завершены — наступило окно для рефакторинга без регрессий: поведение уже закреплено интеграционными тестами всех тестовых приложений (`blank`, `examples`, `cookbook`, `mailreader`, `demo`).

## What Changes

- Новый движковый populate поверх Spring `ConversionService`: собственный `ConversionService` с набором конвертеров, воспроизводящих семантику дефолтного реестра commons-beanutils (примитивы и обёртки, `BigDecimal`/`BigInteger`, литералы Boolean, `String`, массивы, `FormFile`).
- Поддержка имён параметров в beanutils-синтаксисе: простые, индексированные (`prop[0]`), отображаемые (`prop(key)`), вложенные (`a.b.c`) и их комбинации; неизвестные свойства пропускаются без ошибок (как `BeanUtils.populate`).
- Два пути присвоения значений: JavaBean-формы — через `BeanWrapper`; `DynaBean`-формы (`DynaActionForm`, `DynaValidatorForm`, `LazyValidatorForm`, `BeanValidatorForm`) — через метаданные `DynaClass` с конвертацией тем же `ConversionService`.
- `RequestUtils` перестаёт вызывать `BeanUtils.populate`/`PropertyUtils` в populate-пути; публичный API `RequestUtils.populate` сохраняется без изменений.
- Ошибка конвертации по-прежнему приводит к `ServletException` (сообщение `BeanUtils.populate` сохраняется для совместимости логов/обработчиков).
- commons-beanutils остаётся зависимостью движка: публичный API (`DynaBean`-иерархия динамических форм, `FieldChecks`, клонирование конфигураций) не затрагивается; изменяется только populate-путь.

## Capabilities

### New Capabilities

- `form-populate`: семантика заполнения форм — источники значений (параметры запроса и multipart-элементы), синтаксис имён свойств, правила конвертации значений (набор поддерживаемых типов и литералы, поведение пустой строки, скаляр/массив), обработка DynaBean-форм и ошибок конвертации.

### Modified Capabilities

- `request-processing`: требование populate — семантика commons-beanutils воспроизводится движком на Spring `ConversionService` (привязка к спеке `form-populate`); наблюдаемое поведение жизненного цикла не меняется.
- `dynamic-form-beans`: требование populate динамической формы — та же переформулировка; заполнение динамических форм идёт через метаданные `DynaClass` с конвертацией тем же `ConversionService`.

## Impact

- **Код**: `spring-struts1-core` — `RequestUtils` (populate-путь), новый класс populate + `ConversionService` (пакет `org.apache.struts.util`), тесты. Не затрагиваются: `StrutsHandlerAdapter` (вызов `RequestUtils.populate` остаётся), тэг-библиотеки, стартер, валидатор.
- **API**: публичный API не меняется (`RequestUtils.populate` с теми же сигнатурами; `DynaBean`-иерархия остаётся на beanutils).
- **Зависимости**: `commons-beanutils` остаётся (публичный API); populate-путь движка больше его не использует. `spring-core` (ConversionService) уже транзитивно доступен через `spring-webmvc`.
- **Совместимость**: наблюдаемое поведение populate должно остаться идентичным — это фиксируют существующие IT всех тестовых приложений без изменений плюс новые fixture-тесты на семантику конвертеров.
