# Tasks

## 1. Fail-silent навигация populate (D1)

- [x] 1.1 Добавить регрессионные тесты пропуска неизвестных/нечитаемых промежуточных свойств: параметр `nonExistent.deep.path` (первый сегмент отсутствует) и `nested.missing.child` (промежуточное свойство есть, вложенного нет) — populate завершается без ошибки, остальные параметры заполнены. Проверка: `mvnw -pl spring-struts1-core test -Dtest=PopulatePropertyNamesFixtureTest` — новые тесты падают на текущем коде (ServletException вместо пропуска).
- [x] 1.2 В `FormPopulator.navigate()` и `descriptorOf()` перехватывать `InvalidPropertyException` (родитель `NotReadablePropertyException`/`NotWritablePropertyException`/`NullValueInNestedPathException`) и трактовать как «пропустить без ошибки». Проверка: тесты из 1.1 зелёные; существующие fixture-тесты populate зелёные.
- [x] 1.3 Убедиться, что сценарий null-промежуточного свойства (уже покрытый fixture-тестами) не изменился. Проверка: `mvnw -pl spring-struts1-core test -Dtest="Populate*FixtureTest"`.

## 2. Второй слой защиты от class-доступа (D2)

- [x] 2.1 Добавить populate-тесты class-доступа: параметры `class=x`, `CLASS=x`, `x.class=y`, `servlet.class=y`, `class[0]=1`, `class(key)=1`, `class.module.classLoader.DefaultAssertionStatus=1`, `a.class.b=1` — каждый пропускается молчаливо, остальные параметры формы заполняются; те же случаи для DynaBean и lazy-формы (для lazy — отсутствие map-записи `class`). Проверка: новые тесты запускаются и фиксируют требуемое поведение (голое `class` сейчас падает — тест 2.1 красный до 2.2).
- [x] 2.2 В `FormPopulator.populateProperty` после разбора имени пропускать параметр целиком, если имя любого сегмента — `class` в любом регистре; задокументировать в javadoc, что имя свойства `class` в формах не поддерживается (сигнатура CVE-2014-0114). Проверка: тесты 2.1 зелёные; `mvnw -pl spring-struts1-core test -Dtest="Populate*"` зелёные.
- [x] 2.3 Проверить совместимость с эталоном: поведение неизвестного свойства (в т.ч. `class`) в эталонном beanutils — молчаливый пропуск; поведение populate-конвертаций не изменилось. Проверка: полный прогон `mvnw -pl spring-struts1-core test`.

## 3. Порядок фильтра и интроспекции в multipart-пути (D3)

- [x] 3.1 Добавить тест в `RequestUtilsMultipartTest`: multipart-запрос с полем `class.x` и текстовым полем `name=Alice` — populate без ошибки, `name` заполнено, поле `class.x` проигнорировано; отдельный тест: имя с `class.x` не достигает `rationalizeMultipleFileProperty` (исключение от интроспекции не наблюдается). Проверка: тест красный на текущем коде (сейчас исключение до фильтра). [факт: тест зелёный и до правки — `getPropertyType` в Spring 7 возвращает null молча, исключения нет; тест фиксирует требуемое поведение]
- [x] 3.2 В `RequestUtils.populate` применить CVE-фильтр и пропуск `org.apache.struts.*` к имени до вызова `rationalizeMultipleFileProperty`; сделать `rationalizeMultipleFileProperty` fail-silent по `InvalidPropertyException` (возвращает значение без изменения). Проверка: тесты 3.1 зелёные; `mvnw -pl spring-struts1-core test -Dtest=RequestUtilsMultipartTest` зелёный.

## 4. Регрессионный тест regex-слоя (CVE-2014-0114)

- [x] 4.1 Добавить `RequestUtilsClassAccessFilterTest` с матрицей из design.md: BLOCKED — `class.classLoader`, `class.module.classLoader.DefaultAssertionStatus`, `class.protectionDomain`, `foo.class.bar`, `CLASS.classLoader`, `Class.classLoader`, `x.class.y`; allowed — `class`, `classname`, `foo[class]`, `foo('class')`, `map(class)`, `x.class`. Проверка: тест зелёный без изменения кода (фиксирует текущее поведение слоя 1).

## 5. Финальная верификация

- [x] 5.1 Полный прогон тестов ядра: `mvnw -pl spring-struts1-core test` — без падений, в том числе fixture-тесты populate, `StrutsExpressionParserTest`, `RequestUtilsMultipartTest`, новые тесты 1.1/2.1/3.1/4.1.
- [x] 5.2 Полная сборка всех модулей с тестами: `mvnw test` — populate-путь используется стартером и приложениями, кросс-модульных регрессий нет.
