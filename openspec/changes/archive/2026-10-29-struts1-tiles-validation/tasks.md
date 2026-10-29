# Tasks

## 1. Multipart upload в ядре

- [x] 1.1 Добавить зависимости commons-fileupload2-core/`-jakarta` (новейший стабильный 2.x; базовая сверка с 2.0.0-M1 эталона) в `spring-struts1-core`; зафиксировать выбор версии в комментарии pom. Verify: `mvn -pl spring-struts1-core dependency:resolve` проходит, версия задокументирована.
- [x] 1.2 Порт пакетов `org.apache.struts.upload` из эталона: `FormFile`, `MultipartRequestHandler` (с константами превышений), `CommonsMultipartRequestHandler` (+`CommonsFormFile`), `MultipartRequestWrapper`, сопутствующие классы; `Globals.MULTIPART_KEY`, доступ к обработчику на `ActionForm`, `ActionServletWrapper`, дефолт `ControllerConfig.maxStringLen=4K` + новые `maxSize`/`fileCountMax`. Verify: unit-тесты контракта — FormFile-адаптер, лимиты из ControllerConfig, константы атрибутов; `mvn -pl spring-struts1-core test` зелёный.
- [x] 1.3 Интеграция в цикл запроса: `StrutsHandlerAdapter.processMultipart` (обёртка запроса), `processPopulate` выставляет `Globals.MULTIPART_KEY` по `mapping.multipartClass`/ControllerConfig, `RequestUtils.populate` — ветка handler-элементов с `rationalizeMultipleFileProperty`, `rollback`/`finish` в ветках ошибки/успеха, адаптация `MultipartHttpServletRequest` без повторного разбора. Verify: unit-тесты populate по handler-элементам (одиночный FormFile, массив, пропуск populate при превышении).
- [x] 1.4 IT multipart в `spring-struts1-core` (TomcatTestServer-фикстура): загрузка файла + текстовое поле одной формой, multi-file в массив, превышение `maxFileSize` → страница input с ошибкой через request-атрибут, временные файлы удалены. Verify: IT-тесты зелёные, `mvn -pl spring-struts1-core test` целиком.

## 2. commons-validator в ядре

- [x] 2.1 Добавить зависимости commons-validator 1.10.1 и antlr4-runtime 4.13.2 в core; перенести ресурсы эталона: `validator-rules.xml`, `LocalStrings.properties`, JS-утилиты commons-validator; вендорить validwhen (ANTLR4-исходники или плагин — по design D4). Verify: `mvn -pl spring-struts1-core test` зелёный, ресурсы доступны по классическим путям classpath.
- [x] 2.2 Порт `org.apache.struts.validator`: `ValidatorPlugIn` (pathnames/stopOnFirstError, module-ключи контекста, ошибка повторной инициализации), `Resources` (initValidator, аргументы 0–3, бандлы, локаль), `ValidatorForm`, `DynaValidatorForm`, `FieldChecks` (полный набор правил), `validwhen`; `ValidatorActionForm`/`DynaValidatorActionForm` в `org.apache.struts.extras.validator` (пакет — как в эталоне). Verify: unit-тесты — initValidator по ключу формы, конвертация нарушений в ActionMessages, страница/page-фильтрация, validwhen-выражения; ошибка старта при плохом pathnames.
- [x] 2.3 Unit/IT валидации на движке: validation.xml с required/mask/minlength/intRange/validwhen по динамической и статической формам, сообщения через message-resources (`???`-формат для отсутствующих ключей), input-переход при ошибках, DynaValidatorForm с `extends` form-bean. Verify: IT-тесты в core-фикстуре зелёные.

## 3. html:javascript в taglib

- [x] 3.1 Порт `JavascriptValidatorTag` из эталона на Spring-базу тегов (JS-ресурсы из ядра, static/dynamic/c методы, formName/submitMethod и атрибуты TLD). Verify: unit-тесты рендера — скрипт формы по validation.xml, static-режим; сценарий «тег без валидатора» из TagRenderingTest перевёрнут в рабочий.
- [x] 3.2 IT на фикстуре taglib: страница с `<html:javascript formName="..."/>` рендерит функции валидации объявленных правил. Verify: IT зелёный, `mvn -pl spring-struts1-taglib test` целиком.

## 4. Модуль spring-struts1-tiles

- [x] 4.1 Каркас модуля: pom (зависимость core+taglib), подключение в корневой aggregator; перенос `org.apache.struts.tiles` ядра API из эталона: ComponentDefinition/ComponentContext/AttributeDefinition-иерархия, DefinitionsFactory(+Config), TilesUtil(+Impl/Struts/Modules), контроллеры (Controller, ControllerSupport, UrlController, ActionController), TilesAction, beans (MenuItem/SimpleMenuItem). Verify: `mvn -pl spring-struts1-tiles test-compile` зелёный.
- [x] 4.2 XML-парсинг определений: перенос `xmlDefinition` (XmlParser на digester с локальными DTD 1.1/1.3/1.4, XmlDefinition/XmlAttribute/XmlListAttribute, I18nFactorySet с локальными вариантами файлов, FactorySet). Verify: unit-тесты — парсинг tiles-defs с extends/put/putList, локализованные варианты файлов, ошибка при отсутствующем файле.
- [x] 4.3 `TilesPlugin` через плагин-механизм: init-params эталона (definitions-config, parser-validate, factory-classname, moduleAware), фабрика в servlet context под классическими ключами (+module-prefix), регистрация definition-resolver как Spring-компонента, ошибка повторной инициализации, destroy фабрики. Verify: unit-тесты init/destroy/moduleAware.
- [x] 4.4 Hook форвардов в ядре: интерфейс `DefinitionForwardResolver` в `org.apache.struts.spring`, опрос бинов типа в `StrutsHandlerAdapter.processForwardConfig` до обычного рендера; tiles-реализация (контекст атрибутов, вызов контроллера, рендер path definition, fallback include при committed). Verify: unit-тесты адаптера — форвард на definition рендерится, обычный путь не тронут; ядро не зависит от tiles-модуля (проверка pom).
- [x] 4.5 Тэг-библиотека: `struts-tiles.tld` эталона + 11 тегов на SpringTagSupport-базе (insert/definition/put/putList/add/get/getAsString/useAttribute/importAttribute/initComponentDefinitions), логика резолва definition и контекста — Spring-сервисы с переопределением. Verify: unit-тесты рендера пар definition/layout, вложенных тайлов, importAttribute/useAttribute.
- [x] 4.6 IT tiles на фикстуре модуля: приложение с tiles-defs (наследование, putList, локализованные файлы), форвард экшена на definition, контроллер-класс добавляет атрибут, TilesAction, moduleAware в двух модулях. Verify: IT зелёные, `mvn -pl spring-struts1-tiles test` целиком.

## 5. examples: upload и validator

- [x] 5.1 Включить модули в `examples`: init-params `config/upload`, `config/validator` (+варианты -type/-validwhen/-bundles/-i18nVariables по эталону) в web.xml; портировать классы секций из эталона (UploadForm/UploadAction, RegistrationForm/CityStateZip/действия multiRegistration, TypeTestForm и пр.) и JSP (upload.jsp, display.jsp, registration-страницы). Verify: приложение стартует со всеми модулями, `mvn -pl spring-struts1-apps -am test-compile` зелёный.
- [x] 5.2 IT upload-секции: multipart-POST upload-submit с файлом и текстом → display-страница с именем/типом/содержимым; превышение 2M-лимита → сообщение maxLengthExceeded; multi-file сценарии upload-multi. Verify: IT-тесты в ExamplesTest зелёные.
- [x] 5.3 IT validator-секции: registration-submit с нарушениями required/mask/minlength → ошибки на input; успешная отправка → подтверждение; multiRegistration (page=1/2); validwhen-конфиг; jsRegistration рендерит скрипт; локаль/бандлы (`bundle="alternate"`). Verify: IT-тесты зелёные.

## 6. cookbook

- [x] 6.1 Новый war-модуль `cookbook` под `spring-struts1-apps`: портирование эталона (конфиги + Wildcard, DynaValidatorForm, кастомный `twofields`-валидатор, jsp-секции simple/dyna/options/multibox/bean/logic/links/validator/messages/localization/token). Verify: приложение стартует, `mvn -pl spring-struts1-apps -am test-compile` зелёный.
- [x] 6.2 IT cookbook: валидация DynaValidatorForm (byte/integer/range/creditCard/email), кастомное правило twofields, базовые сценарии секций (multibox, localization, token). Verify: CookbookAppTest зелёный.

## 7. mailreader (+dao)

- [x] 7.1 Вендоринг dao: модуль `mailreader-dao` под apps из эталона (пакеты org.apache.struts.apps.mailreader.dao.*, memory-impl, digester-загрузка database.xml). Verify: unit-тесты dao (создание/чтение user/subscription) зелёные.
- [x] 7.2 Портировать плагины extras в ядро: `ModuleConfigVerifier`, `DigestingPlugIn` (+сопутствующие классы пакета эталона). Verify: unit-тесты init/destroy обоих плагинов с set-property.
- [x] 7.3 Новый war-модуль `mailreader`: портирование эталона (struts-config с wildcard и extends form-bean, DynaValidatorForm-формы, MemoryDatabasePlugIn в исходниках приложения, validation.xml, JSP, DigestingPlugIn-конфиг server-types). Verify: приложение стартует, `mvn -pl spring-struts1-apps -am test-compile` зелёный.
- [x] 7.4 IT mailreader: logon (валидация required/minlength), registration нового пользователя → logon, subscription create/edit/delete, токены, ExpiredPasswordException-форвард. Verify: MailreaderAppTest зелёный.

## 8. blank и полный регресс

- [x] 8.1 blank: заменить `TestPlugin` на реальный `ValidatorPlugIn` (как в эталоне blank), validation.xml и ключи сообщений из эталона; TestPlugin остаётся дополнительным регрессом плагин-механизма либо удаляется — по факту эталона. Verify: BlankAppTest зелёный с валидацией.
- [x] 8.2 Полный регресс: `mvn test` всех модулей; существующие IT blank/examples (exercise/dispatch) без изменений ожиданий; `openspec validate --strict` по изменению проходит. Verify: сборка зелёная целиком.

## Workflow follow-up

- Archive the change after the project's review requirements are satisfied.
- Verify the archived result.
