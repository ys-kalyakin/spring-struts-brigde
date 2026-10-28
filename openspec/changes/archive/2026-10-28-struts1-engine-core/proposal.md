# Proposal

## Why

Роадмап (фаза 1) и заархивированный change `spring-struts1-foundation` назначили этот change точкой старта реализации: проект до сих пор содержит только документацию, кода нет. Цель фазы 1 — минимальный движок Struts 1 на Spring MVC, способный запустить портированное `apps/blank` из эталона weblegacy/struts1: это первый проверяемый шаг к drop-in замене артефакта Struts 1 на `spring-struts1`.

## What Changes

- Создаётся мультимодульный Maven-проект: `spring-struts1-core` (движок и конфигурационная модель), `spring-struts1-apps` (портированные тестовые приложения); в этой фазе — только `blank`.
- Портируется конфигурационная модель и Digester-правила из weblegacy `core` (`org.apache.struts.config`): парсинг `struts-config.xml` — маппинги, формы, форварды, исключения, message-resources, плагины.
- Строится движок жизненного цикла запроса на Spring MVC (`StrutsHandlerMapping` + `StrutsHandlerAdapter` внутри `DispatcherServlet`): маршрутизация по суффиксу (`*.do`), populate формы по семантике commons-beanutils → валидация (`validate=true/false`) → `execute` → форвард; локальные форварды приоритетнее глобальных; `redirect=true` — HTTP-редирект, иначе dispatch.
- `MessageResources` реализуется поверх Spring `MessageSource` со Struts-семантикой локали (сессия, инициализация из запроса) и форматом отсутствующих ключей `???<locale>.<key>???`.
- Добавляется декларативная обработка исключений: `global-exceptions` и `exception` в маппинге — форвард + `ActionMessage`; неописанные — проброс в контейнер.
- Добавляются аннотации `@StrutsAction` (singleton) / `@StrutsForm` (prototype) и резолвинг бинов из Spring-контекста; классы без регистрации в Spring создаются напрямую (фолбэк классического режима).
- `ActionServlet` сохраняется как фасад совместимости (init-param `config`, рабочая ссылка `action.getServlet()`); жизненный цикл `PlugIn` (init при старте, destroy при остановке; из Spring-контекста, если бин, иначе прямое создание).
- Портируется `apps/blank` с минимальной документированной адаптацией: JSP переписываются без Struts-тэг-библиотек (фаза 2), вместо `ValidatorPlugIn` подключается простой тестовый плагин (валидатор — фаза 3); прогон — интеграционный тест на embedded Tomcat 11.

## Capabilities

### New Capabilities

- `struts-config-parsing`: чтение и объединение конфигурации из `struts-config.xml` — маппинги, формы, глобальные/локальные форварды, исключения, message-resources, плагины; контракт init-param `config` (список файлов) и ошибки старта при отсутствующем/некорректном файле.
- `request-processing`: маршрутизация запросов на экшены (суффиксное маппирование) и полный жизненный цикл запроса — populate → validate → execute → forward — с семантикой форвардов и 404 для незарегистрированных путей.
- `message-resources`: доступ к сообщениям приложения по ключу поверх Spring `MessageSource`, Struts-семантика локали и формат отсутствующих ключей.
- `exception-handling`: декларативная обработка исключений через `global-exceptions` и `exception` в маппинге.
- `spring-bean-integration`: аннотации `@StrutsAction`/`@StrutsForm`, семантика singleton/prototype, внедрение зависимостей, фолбэк прямого создания классов, не зарегистрированных в Spring.
- `action-servlet-facade`: фасад `ActionServlet` (совместимость с legacy-кодом, init-params) и жизненный цикл `PlugIn`.

### Modified Capabilities

(нет — спецификаций в проекте ещё нет, `openspec list --specs` пуст)

## Impact

- **Структура:** новые Maven-модули `spring-struts1-core` и `spring-struts1-apps` (родительский POM в корне); код движка сохраняет пакеты `org.apache.struts.*`.
- **Зависимости:** Spring Framework 7 / Spring Boot 4.0.x (при блокере JSP на Tomcat 11 — документированный откат на Boot 3.5.x без изменения кода движка), commons-digester, commons-beanutils; Java 17+; embedded Tomcat 11 для интеграционных тестов.
- **Тестовые приложения:** портированный `apps/blank` из `/home/helloween/projects/struts1-reference` (v1.5.0-rc2) — целевое приложение фазы; эталон совместимости для конфигураций и сценариев.
- **Не входит в объём:** тэг-библиотеки, Tiles, multipart upload, commons-validator/`DynaActionForm` (фазы 2–3), Spring Boot стартер (фаза 4), поддержка нескольких модулей (`config/<prefix>`) и path-mapped экшенов (`/action/*`) — решение о path-mapped зафиксировано в design.md.
- **Рефакторинг-кандидат после фазы 1:** переход populate с commons-beanutils на Spring `ConversionService`.
