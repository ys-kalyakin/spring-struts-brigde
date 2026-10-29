# Design

## Context

Движок (StrutsHandlerMapping/StrutsHandlerAdapter в DispatcherServlet), тэг-библиотеки bean/html/logic/nested на Spring-базе, плагин-механизм (PlugIn + `<plug-in>` + `<set-property>`), DynaActionForm с парсингом `<form-property>` и мост MessageResources→MessageSource уже реализованы. Конфигурации upload/validator-модулей examples перенесены и ждут классов (`ValidatorPlugIn`, `UploadForm`, `RegistrationForm`); TLD `struts-html` объявляет `html:javascript` без реализации. В `ControllerConfig` уже парсятся `bufferSize/maxFileSize/maxStringLen/memFileSize/multipartClass/tempDir`, но отсутствуют `maxSize`/`fileCountMax`, а multipart-обработки в цикле запроса нет вовсе.

Эталон (weblegacy 1.5.x, jakarta) даёт фактуру: Tiles — отдельный модуль `struts-tiles` (пакет `org.apache.struts.tiles.*`, 11 тегов, фабрика `I18nFactorySet` по умолчанию, digester-парсер, DTD 1.1/1.3/1.4) плюс отдельный опциональный мост `struts-tiles2` на org.apache.tiles 3.0.8; upload — `org.apache.struts.upload` поверх commons-fileupload2-jakarta (2.0.0-M1) с изящной семантикой превышения лимитов (request-атрибуты вместо исключений) и multi-file `getFileElements() → FormFile[]`; validator — `org.apache.struts.validator` поверх commons-validator 1.10.1, validwhen на ANTLR4-runtime 4.13.2, `ValidatorActionForm`/`DynaValidatorActionForm` вынесены в extras; mailreader зависит от dao-модуля `struts-mailreader-dao` и плагинов extras (`ModuleConfigVerifier`, `DigestingPlugIn`).

## Goals / Non-Goals

**Goals:**

- Классический контракт `org.apache.struts.upload`/`org.apache.struts.validator`/`org.apache.struts.tiles.*` с поведением эталона; целевые приложения компилируются и работают без правок своего кода.
- Spring-нативность в духе фазы 3: точки расширения — Spring-компоненты (сервисы логики тайлов, hook резолва форвардов, адаптация Spring-мультипарсера), плагиныTiles/валидатора идут через существующий плагин-механизм; ядро не зависит от tiles-модуля.
- Все три целевых приложения (upload/validator-секции examples, cookbook, mailreader) запускаются и проходят основные сценарии как IT.

**Non-Goals:**

- tiles2-мост (`org.apache.tiles`), struts-el, faces/scripting-приложения.
- `BeanValidatorForm`/`LazyValidatorForm` (не нужны целевым приложениям; добавить дёшево при запросе).
- Стартер Boot, публикация артефактов (фаза 4).
- Замена commons-beanutils на Spring ConversionService (отложенный рефакторинг, не в этой фазе).

## Decisions

### D1. Tiles — порт 1.x-модуля, не tiles2 (закрывает открытый вопрос роадмапа)

Портим weblegacy-модуль `struts-tiles` (пакеты `org.apache.struts.tiles`/`tiles.taglib`/`tiles.xmlDefinition`) в новый модуль `spring-struts1-tiles`. Альтернатива tiles2 (мост на Apache Tiles 3.0.8) отвергнута: принцип совместимости №3 требует те же URI/TLD/имена/поведение для `struts-tiles`, классические приложения используют 1.x-семантику (`tiles:insert` с `put`), а tiles2 тянет внешний стек (tiles-api/core/request) с другой моделью — это отдельный опциональный продукт и в weblegacy, и у нас.

### D2. Tiles Spring-нативно: hook форвардов вместо подмены процессора

Классика интегрирует tiles подменой RequestProcessor (`TilesRequestProcessor`) или chain-командой (`TilesPreProcessor`). У нас один адаптер — вводим в ядре расширяемую точку: `StrutsHandlerAdapter` перед обработкой форварда опрашивает Spring-бины типа `DefinitionForwardResolver` (новый интерфейс в `org.apache.struts.spring`); `TilesPlugin` при init регистрирует реализацию синглтоном в `WebApplicationContext`. Ядро зависит только от интерфейса — tiles-модуль опционален. Фабрика определений хранится и в servlet context под классическими ключами (`org.apache.struts.tiles.DEFINITIONS_FACTORY[+prefix]` — работает классический `TilesUtil`/legacy-код), и доступна как Spring-компонент; парсинг XML — digester-правила эталона с локальными DTD; фабрика по умолчанию — `I18nFactorySet`. Теги — на `SpringTagSupport`-базе, логика (резолв definition, контекст тайла) — Spring-сервисы (переопределяемые), TLD `struts-tiles.tld` эталона в `META-INF/tld/` модуля.

### D3. Upload: классический контракт поверх commons-fileupload2-jakarta; Spring-мультипарсер — адаптация

`CommonsMultipartRequestHandler` реализуем на commons-fileupload2-jakarta (базовая версия как в weblegacy 2.0.0-M1; при реализации берём новейший стабильный 2.x, если API совместим) — это даёт точные семантики эталона: изящное превышение лимитов (request-атрибуты `ATTRIBUTE_MAX_LENGTH_EXCEEDED` и специфичные byte/string/count вместо исключения), temp-файлы с threshold, multi-file. Интеграция: `StrutsHandlerAdapter.processMultipart` оборачивает POST `multipart/form-data` в `MultipartRequestWrapper`; `processPopulate` выставляет `Globals.MULTIPART_KEY` (класс из `mapping.multipartClass` или `ControllerConfig`), `RequestUtils.populate` получает элементы через handler (`getAllParametersForMultipartRequest` + `rationalizeMultipleFileProperty`), `rollback`/`finish` — в ветках ошибки/успеха адаптера.

Альтернатива «всё через Spring `StandardServletMultipartResolver`» отвергнута как основная: лимиты живут в контейнере (`MultipartConfig`), превышение даёт исключение контейнера/`MultipartException` вместо атрибута — поведение эталона (валидация формы видит атрибут и пишет сообщение) не воспроизводится. Но путь совместимости сохраняем: если пользователь объявил свой `multipartResolver` и запрос уже `MultipartHttpServletRequest`, адаптер строит те же element-map-ы из его частей (`SpringMultipartRequestHandler`-адаптер), не разбирая повторно.

### D4. Validator: порт пакета в ядро, validwhen на ANTLR4-runtime

`org.apache.struts.validator` (ValidatorPlugIn, Resources, ValidatorForm, DynaValidatorForm, FieldChecks, validwhen) — в `spring-struts1-core`; `ValidatorActionForm`/`DynaValidatorActionForm` — в `org.apache.struts.extras.validator` (раскладка weblegacy); ресурсы `validator-rules.xml`, `LocalStrings.properties`, JS-утилиты — из эталона в classpath ядра. Зависимости: commons-validator 1.10.1 + antlr4-runtime 4.13.2. validwhen-парсер вендорим сгенерированными исходниками (как в weblegacy) вместо ANTLR-плагина в сборке — детерминированная сборка без дрейфа версий генератора; при расхождении — генерация плагином (fallback). Ключ валидации — `mapping.getAttribute()` (core-формы), у extras-форм — `mapping.getPath()`. `DynaValidatorForm` extends `DynaActionForm` — существующий skip-Dyna в registrar/resolver покрывает его без правок spring-bean-integration. `ValidatorPlugIn` идёт через существующий плагин-механизм (ключи контекста `VALIDATOR_KEY+prefix`, `STOP_ON_ERROR_KEY+prefix` — как в эталоне). `html:javascript` (`JavascriptValidatorTag`) — в taglib-модуле на Spring-базе, JS-ресурсы читает из ядра.

### D5. mailreader-dao вендорим как собственный модуль

Dao-модуль (`org.apache.struts.apps.mailreader.dao.*` — UserDatabase/User/Subscription, memory-impl) копируем из эталона в репозиторий модулем `mailreader-dao` под `spring-struts1-apps`. Альтернативы: зависимость от опубликованного `io.github.weblegacy:struts-mailreader-dao` — отвергнута (внешняя зависимость эталонного SNAPSHOT/релиза в наш build; dao — доменный код приложения, а не фреймворка) и сборка из локального клонa эталона — отвергнута (build перестаёт быть самодостаточным). Плагины extras для mailreader (`ModuleConfigVerifier`, `DigestingPlugIn`) портируем в ядро (`org.apache.struts.extras.plugins`, пакет уточняем по эталону при реализации) — это часть публичного API struts-extras.

### D6. Приложения: examples включает модули, cookbook и mailreader — новые war

- `examples`: в web.xml добавляются init-params `config/upload` и `config/validator` (и соседние конфиги-варианты validator), портируются классы и JSP секций upload/validator из эталона (они jakarta-совместимы; правки не требуются, кроме вариантов конфигов, ссылающихся на классы из перенесённых пакетов).
- `cookbook`: новый war-модуль — портирование эталонного приложения (DynaValidatorForm-валидация, кастомный `twofields`-валидатор, локализация, токены); IT по основным сценариям.
- `mailreader`: новый war-модуль + зависимость от вендоренного dao; портирование приложения (DynaValidatorForm с `extends` form-bean, MemoryDatabasePlugIn — в исходниках приложения, wildcard-маппинги); IT по основным сценариям (logon → registration → subscription).
- `blank`: заглушка `TestPlugin` заменяется реальным `ValidatorPlugIn` (как в эталоне blank: валидация welcome-формы), остаётся регрессом фазы 1.

## Risks / Trade-offs

- [commons-fileupload2 — младшая линия (2.0.0-M1 у weblegacy)] → при реализации взять новейший стабильный 2.x и прогнать IT upload (включая превышение лимитов и multi-file); API-дрейф локализован в одном классе `CommonsMultipartRequestHandler`.
- [ANTLR4-вендоренные исходники validwhen могут разойтись с версией runtime] → фиксируем antlr4-runtime 4.13.2 как у эталона; при несовпадении — переход на генерацию antlr4-maven-plugin (решение локально для одного пакета).
- [Портирование ~100 классов taglib/tiles/taglib-taglib высокой связности] → переносим пакетами из эталона с минимальными правками (jakarta уже в эталоне), правим только интеграционные точки (базы тегов, сервисы); IT на эталонных приложениях ловят регрессии вывода.
- [Расхождение деталей поведения валидатора (аргументы, бандлы, page) с эталоном] → конфигурации и validation.xml целевых приложений подключаем как fixture-тесты; `stopOnFirstError` и порядок сообщений сверяем на IT examples/validator.
- [Static TilesUtil хранит фабрики в servlet context] → ключи и схема module-aware точно как в эталоне; Spring-компоненты — дополнительный, а не заменяющий путь доступа.
- [Вендоринг dao/validwhen увеличивает объём репозитория] → даёт самодостаточность и воспроизводимость сборки; модули помечены origin-комментарием в pom/README модуля.

## Migration Plan

Изменение аддитивно: новый tiles-модуль и новые пакеты ядра не затрагивают существующие спеки (кроме двух модификаций в дельте). Порядок внедрения: upload → validator (ядро, тег javascript) → tiles-модуль → включение examples → cookbook → mailreader → полный регресс. Откат — revert изменений фазы; публичный API предыдущих фаз не меняется.

## Open Questions

- Точная версия commons-fileupload2 (2.0.0-M1 как у эталона или новейший стабильный 2.x) — решается первой задачей upload прогоном IT; на спеку не влияет.
- Пакет extras-плагинов у weblegacy (`org.apache.struts.extras.plugins` или `org.apache.struts.plugins`) — фиксируется по факту эталона в первой задаче mailreader-зависимостей; имена и поведение классов не меняются.
