# Design

## Context

Populate-путь (публикация форм) принимает beanutils-выражения имён параметров (`a.b[i](key)`), разбирает их собственным структурным парсером `StrutsExpressionParser` и заполняет форму через `FormPopulator` поверх Spring `BeanWrapper` и `StrutsConversionService`. Вопрос аудита «можно ли составить выражение, выполняющее произвольный код» проверен эмпирически (пробник против Spring 7.0.10): **нельзя**.

Экспериментальная матрица (пробник, слой 1 — regex `RequestUtils.CLASS_ACCESS_PATTERN`, слой 2 — `FormPopulator`/`BeanWrapper`):

| Выражение | Слой 1 | Слой 2 (Spring 7.0.10) |
|---|---|---|
| `class.classLoader`, `class.protectionDomain`, `CLASS.classLoader` | BLOCKED | — |
| `class.module.classLoader.DefaultAssertionStatus` | BLOCKED | на классе-объекте чтение `module`/`classLoader` запрещено (`NotReadablePropertyException`) |
| `foo.class.bar` | BLOCKED | — |
| `class` (голое), `x.class` (хвост), `servlet.class` | allowed | дескриптор `class` читаем; конвертация `String`→`Class` даёт ошибку populate (500) |
| `class[0]` | BLOCKED (альтернатива `\[`) | при прямом вызове populate — `InvalidPropertyException` → 500 |
| `classname`, `map(class)`, `foo('class')`, `foo[class]` | allowed | безопасно: ключ/индекс не участвует в навигации (`map(class.classLoader)` ложится ключом в Map) |
| `nonExistent.deep.path` | allowed | `NotReadablePropertyException` не перехватывается → 500 (нарушение спеки «пропускаться без ошибки») |
| lazy-форма, `class.classLoader` | allowed | DynaBean-навигация смотрит объявленные свойства → пропуск (безопасно) |

Инстанцирование классов по имени (`RequestUtils.applicationInstance/applicationClass`, фабрики `ConfigRuleSet`, Tiles, `StrutsInstanceResolver`) достижимо только из конфигурации (struts-config, Spring-свойства) — из HTTP-ввода имена классов не поступают (проверены все вызывающие). `ValidWhen` — ANTLR-визитор над выражениями из конфигурации валидации, из запроса берутся только значения. Скан Mimosa (61 находка) подтверждает: «HIGH» — клиентский `eval` в портированных JS commons-validator (серверного эффекта нет), «MEDIUM» — рефлексия из конфигурационных строк.

## Goals / Non-Goals

**Goals**
- Восстановить соответствие спеке: populate молчаливо пропускает неизвестные/нечитаемые имена и любой class-доступ, без ошибок 500.
- Второй (независимый) слой защиты от class-обхода на уровне `FormPopulator`.
- Устранить окно в multipart-пути, где имя поля интроспектируется до CVE-фильтра.
- Закрепить всё регрессионными тестами (сейчас CVE-фильтр не покрыт ни одним тестом).

**Non-Goals**
- Сужение поверхности навигации по вложенным свойствам (сценарий `nested.intProperty` закреплён спекой и эталоном beanutils; вариант «allow-list корней навигации» — кандидаты на отдельное изменение).
- Конвертация `String`→`Class` для `Class`-типизированных свойств (закреплена спекой как совместимость; риск реализуем только для форм, объявивших свойство типа `Class` — документируем, не убираем).
- Клиентский JS commons-validator и конфигурационная рефлексия (см. proposal — вне scope).

## Decisions

### D1. Fail-silent навигация через родительское исключение
В `FormPopulator.navigate()` и `descriptorOf()` перехватывать `InvalidPropertyException` (общий предок `NotReadablePropertyException`, `NotWritablePropertyException`, `NullValueInNestedPathException`), а не только два конкретных подкласса. Альтернатива — предварительная проверка существования свойств — отвергнута: дублирует интроспекцию Spring и добавляет окно рассинхронизации. Перехват предка устойчив к изменениям иерархии исключений Spring.

### D2. Второй слой: пропуск сегментов `class` в `FormPopulator.populateProperty`
После разбора имени параметр пропускается целиком (молчаливо), если имя любого сегмента — `class` в любом регистре. Это закрывает случаи, которые regex-слой пропускает умышленно или по недосмотру: голое `class`, хвостовое `x.class`, `servlet.class`, `class[0]`, `class(key)`. Альтернативы: расширить regex `RequestUtils` (не закрывает прямой вызов `FormPopulator.populate` и расширяет regex с рисками ложных срабатываний), либо проверять только в конвертере `String`→`Class` (слишком поздно — уже прошёл навигацию). Двухслойность выбрана осознанно: слои независимы, регресс любого из них не открывает вектор. Побочный эффект: lazy-форма больше не добавляет map-запись `class` — это выравнивает поведение с эталоном beanutils (неизвестное свойство → пропуск).

### D3. Фильтр раньше интроспекции в multipart-пути
В `RequestUtils.populate` CVE-фильтр и пропуск `org.apache.struts.*` применяются к имени до вызова `rationalizeMultipleFileProperty` (сейчас порядок обратный), а сама `rationalizeMultipleFileProperty` становится fail-silent по `InvalidPropertyException` (возвращает значение без изменения). Имя параметра из запроса не должно доходить до `BeanWrapper` до фильтрации.

### D4. Регрессионная матрица из пробника
- Тест regex-слоя: 13 выражений матрицы (BLOCKED/allowed).
- Тесты populate-слоя: class-случаи (голое/хвостовое/`servlet.class`/`class[0]`/`class(key)`/`class.module.classLoader.DefaultAssertionStatus`) — молчаливый пропуск, остальные параметры заполняются; статическая форма, DynaBean и lazy-форма.
- Тесты fail-silent навигации: неизвестное промежуточное (`nonExistent.deep.path`), нечитаемое промежуточное (`nested.missing.child`), null-промежуточное (уже покрыто fixture-тестами).
- Тест multipart: поле `class.x` игнорируется до разрешения типов, остальные поля заполняются.
Места: `spring-struts1-core/src/test/java/org/apache/struts/util/` (рядом с fixture-тестами populate). Существующие fixture-тесты не закрепляют текущее падающее поведение (проверено) — изменений не требуют.

## Risks / Trade-offs

- [Изменение наблюдаемого поведения: аномальные имена с 500 на молчаливый пропуск] → это и есть цель (соответствие спеке и эталону beanutils); fixture-тесты не закрепляют 500; матрица D4 фиксирует новое поведение.
- [Легитимная форма со свойством, названным `class`] → такой доступ совпадает с сигнатурой CVE-2014-0114 и уже частично режется regex-слоем; guard распространяет это на все позиции. Документируем в javadoc `FormPopulator`; имя свойства `class` в формах не использовать.
- [Regex-слой пропускает `foo[class]`, `foo('class')`, `map(class)`] → структурно безопасно (позиции индекса/ключа не участвуют в навигации), закрепляется тестами как ожидаемое поведение.
- [Апгрейд Spring изменит иерархию исключений BeanWrapper] → перехват родителя `InvalidPropertyException` (D1) + поведенческие тесты матрицы, не привязанные к типам исключений.

## Migration Plan

Изменение без миграции: пересборка и прогон полного тестового набора модуля. Откат — реверс коммита. Конфигурация и зависимости не меняются.

## Open Questions

(нет — вопрос о достижимости RCE закрыт экспериментально; остальные варианты зафиксированы как Non-Goals или риски)
