# Tasks

## 1. Fixture-тесты текущего populate (golden)

- [x] 1.1 Написать модульные тесты скалярной конвертации на текущей реализации (`RequestUtils.populate`): литералы Boolean (`true/yes/y/on/1`, `false/no/n/off/0`, регистронезависимо, неверный литерал — ошибка), числа всех примитивных типов и обёрток, пустая строка в число — ошибка, `Character` (первый символ), `BigDecimal`/`BigInteger`, `Enum`, `String` без преобразования, `java.util.Date` — ошибка (нет конвертера). Verify: `mvnw test -pl spring-struts1-core` зелёный на текущей beanutils-реализации
- [x] 1.2 Написать тесты имён свойств: простое, индексированное `prop[0]`, отображаемое `prop(key)`, вложенное `a.b`, комбинация `a.b[0](key)`, неизвестное имя — пропуск, `org.apache.struts.*` — пропуск, паттерн `class` (CVE-2014-0114) — игнор. Verify: те же тесты зелёные на текущей реализации
- [x] 1.3 Написать тесты массивов и коллекций: `String[]` → массив/список покомпонентно, `int[]` из multibox-параметров, одиночный параметр в скаляр (первый элемент), `FormFile` из multipart (по образцу `RequestUtilsMultipartTest`). Verify: тесты зелёные на текущей реализации
- [x] 1.4 Написать тесты DynaBean-пути: `DynaActionForm` с типами `String`/`Boolean`/`Integer`/`String[]`, отображаемое свойство dyna-формы, `LazyValidatorForm` — необъявленное свойство добавляется как есть, объявленное конвертируется. Verify: тесты зелёные на текущей реализации

## 2. Сервис конвертации и парсер выражений

- [x] 2.1 Реализовать `StrutsConversionService` (`org.apache.struts.util`): конфигурация `DefaultConversionService` с конвертерами beanutils-семантики по матрице из design.md (Boolean-литералы, числа с ошибкой на пустой строке, `Character`, `BigDecimal`/`BigInteger`, `java.sql.Date/Time/Timestamp` и `Calendar` с паттернами beanutils, `Class`/`File`/`URL`, `Enum`; без `java.util.Date`). Verify: модульные тесты сервиса по всей матрице конвертации зелёные
- [x] 2.2 Реализовать парсер beanutils-выражений (простое/`[i]`/`(key)`/точки/комбинации) с трансляцией в путь `BeanWrapper` (отображаемый ключ в квотированной форме). Verify: модульные тесты парсера на всех формах имён из fixture-теста 1.2 зелёные

## 3. Новый populate

- [x] 3.1 Реализовать JavaBean-путь присвоения через `BeanWrapper` с `StrutsConversionService`: покомпонентные массивы/коллекции, скаляр-первый-элемент, пропуск неизвестных свойств, прерывание на первой ошибке. Verify: fixture-тесты 1.1–1.3 зелёные уже на новом пути (через публичный `RequestUtils.populate`)
- [x] 3.2 Реализовать DynaBean-путь по метаданным `DynaClass` с тем же сервисом: конвертация по `DynaProperty`, присвоение `set(name[, index | key])`, авто-добавление для lazy-форм, пропуск необъявленных у обычных DynaBean. Verify: fixture-тесты 1.4 зелёные на новом пути
- [x] 3.3 Переключить `RequestUtils` на новый populate: убрать `BeanUtils.populate` и `PropertyUtils` из populate-пути (`rationalizeMultipleFileProperty` — через `BeanWrapper`), сохранить `ServletException("BeanUtils.populate", e)`, сбор параметров в `HashMap`, проверки `org.apache.struts.*`/CVE и early-return multipart-лимита. Verify: `grep beanutils spring-struts1-core/src/main/java/org/apache/struts/util/RequestUtils.java` пусто; fixture-тесты 1.1–1.4 зелёные без правок

## 4. Интеграционная проверка

- [x] 4.1 Прогнать полный набор интеграционных тестов приложений без изменений: `./mvnw verify` — все IT (`blank`, `examples`, `cookbook`, `mailreader`, `demo`, стартер) зелёные. Verify: сборка зелёная, отчёты surefire без падений
- [x] 4.2 Убедиться, что populate-путь не зависит от статического реестра: тест, регистрирующий кастомный конвертер в `ConvertUtils`, не влияет на populate. Verify: такой тест зелёный (populate проигнорировал реестр)

## 5. Документация

- [x] 5.1 Обновить README (раздел «Совместимость»): populate выполняется сервисом конвертации движка на Spring `ConversionService`; статический реестр `ConvertUtils` в populate не участвует (кастомные конвертеры в нём на populate не влияют). Verify: раздел README описывает новое поведение
- [x] 5.2 Отметить в ROADMAP.md пункт «переход на Spring ConversionService» как реализованный (изменение `populate-conversion-service`). Verify: строка роадмапа ссылается на выполненное изменение
