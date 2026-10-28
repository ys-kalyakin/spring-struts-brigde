# Design

## Context

Репозиторий пока не содержит кода — только документацию (см. proposal.md — Why). Зафиксированные роадмапом решения: точка входа — Spring MVC `DispatcherServlet` с `StrutsHandlerMapping` + `StrutsHandlerAdapter`; парсинг конфига — commons-digester с правилами из weblegacy `core`; populate — commons-beanutils; `MessageResources` — поверх Spring `MessageSource`; `@StrutsAction` — singleton, `@StrutsForm` — prototype; стек — Spring Boot 4.0.x / Framework 7, Java 17+, embedded Tomcat 11 (jakarta).

Эталон: `/home/helloween/projects/struts1-reference` (weblegacy 1.5.0-rc2). Его `apps/blank` — war с `web.xml` (servlet `action` → `org.apache.struts.action.ActionServlet`, init-param `config`, mapping `*.do`) и `struts-config.xml` (DTD 1.4): глобальный форвард `welcome` → `/Welcome.do`, экшен `/Welcome` с атрибутом `forward="/pages/Welcome.jsp"` (без типа), `<message-resources parameter="MessageResources"/>` и `ValidatorPlugIn`. JSP blank используют тэг-библиотеки (фаза 2), валидатор — фаза 3.

## Goals / Non-Goals

**Goals:**
- Мультимодульный Maven: `spring-struts1-core` (движок, пакеты `org.apache.struts.*`) + `spring-struts1-apps` (агрегатор портированных приложений, в фазе — `blank`).
- Портирование конфигурационной модели (`org.apache.struts.config`) и Digester-правил из weblegacy `core`.
- Движок жизненного цикла запроса на Spring MVC с семантикой classic `RequestProcessor` weblegacy.
- `./mvnw verify` зелёный: интеграционные тесты запросов всегда на embedded Tomcat 11 (не на mock-стеке).

**Non-Goals:**
- Тэг-библиотеки, Tiles, multipart upload, commons-validator/`DynaActionForm` (фазы 2–3), Spring Boot стартер (фаза 4).
- Несколько модулей конфигурации (`config/<prefix>`) — только модуль по умолчанию.
- Path-mapped экшены (`/action/*`) — только суффиксные; переносится в фазу 2.
- Переход populate на Spring `ConversionService` — кандидат рефакторинга после фазы 1.

## Decisions

1. **Входная точка — `DispatcherServlet`, `ActionServlet` — фасад-бин.** Порт `blank` объявляет в web.xml Spring `DispatcherServlet` с маппингом на суффикс `*.do` и init-params движка (`config` и др.). `ActionServlet` создаётся как Spring-бин, инициализируется `ServletContext` и init-params фронтального сервлета, владеет разобранным `ModuleConfig` и доступен экшенам через `action.getServlet()`. Альтернативы: настоящий `ActionServlet` как фронт-контроллер (дублирует MVC, ломает интеграцию со Spring — отклонено); автоконфигурация всей связки стартером (фаза 4; в фазе 1 — явное объявление в web.xml).
2. **Парсинг — commons-digester с правилами, портированными из `org.apache.struts.config` (ConfigRuleSet).** Портируются `BaseConfig`, `ActionConfig`, `FormBeanConfig`, `ForwardConfig`, `ExceptionConfig`, `MessageResourcesConfig`, `ControllerConfig`, `PlugInConfig`, `ModuleConfig` (+ `impl`), фабрика и `ConfigRuleSet`; для фазы 1 вырезаются зависимости на commons-chain и validator. DTD — 1.4, как в эталоне; валидация XML — init-param `validating` (по умолчанию вкл.). Альтернатива — собственный XML-парсер на Spring — отклонена: правила digester уже кодируют семантики (объединение файлов, set-property, wildcard), риск расхождения с эталоном.
3. **Конвейер запроса — порт семантики classic `RequestProcessor`, без commons-chain.** weblegacy 1.5 выполняет конвейер через commons-chain; наблюдаемое поведение то же, что у classic `RequestProcessor` (populate → validate → execute → forward, обработка исключений). Фаза 1 реализует конвейер напрямую в `StrutsHandlerAdapter` (HandlerExecutionChain: маппинг → форма → экшен → форвард), commons-chain не подключается. Альтернатива — портировать chain-конфигурацию as-is — отклонена: лишняя зависимость без наблюдаемой выгоды.
4. **Populate — commons-beanutils** (решение роадмапа). Весь доступ к статическому реестру `ConvertUtils` изолируется в одном утилитарном классе движка (риск из роадмапа); конвертация — дефолтные конвертеры beanutils.
5. **`MessageResources` — API из weblegacy `util`, backed Spring `MessageSource`.** Публичный API (`MessageResources`, фабрики) портируется; сообщение — из `MessageSource`, локаль — `Globals.LOCALE_KEY` из сессии, иначе локаль запроса; отсутствующий ключ — `???<locale>.<key>???`. При старте в контекст приложения кладётся атрибут `Globals.MESSAGES_KEY` (его проверяет JSP blank).
6. **Резолвинг бинов.** `@StrutsAction`/`@StrutsForm` — стереотипные аннотации, регистрируемые сканированием Spring; движок резолвит экземпляр по классу из контекста (singleton для экшена, prototype для формы — семантику обеспечивает область бина), при отсутствии бина — прямое создание по имени класса из конфига (фолбэк).
7. **Портирование blank — минимальная документированная адаптация.** JSP переписаны без тэг-библиотек (scriptlets: проверка `Globals.MESSAGES_KEY`, вывод `bean:message`-эквивалента, редирект из `index.jsp` на `/Welcome.do`); вместо `ValidatorPlugIn` в конфигурации портированного приложения — минимальный тестовый плагин, exercising init/destroy; остальная структура `struts-config.xml` сохраняется как в эталоне.
8. **Тестовая стратегия.** Модульные тесты конвейера (populate/validate/исключения/сообщения) — на фикстурных конфигурациях и выделенном тестовом приложении в тестах `spring-struts1-core`; все тесты уровня HTTP-запросов — на embedded Tomcat 11 (`tomcat-embed` + JSP), не на mock-стеке. Конфигурации weblegacy подключаются как fixture-тесты парсинга.

## Risks / Trade-offs

- [Статический реестр `ConvertUtils` в beanutils глобален] → изоляция в одном утилитарном классе; при необходимости — сброс/локальная настройка на приложение.
- [JSP-сборка на Boot 4.0 / Tomcat 11 может не взлететь] → спайк в первой задаче; откат на Boot 3.5.x задокументирован и не меняет код движка (базовый MVC API).
- [Расхождение портированных digester-правил с weblegacy] → правила портируются файл-в-файл из эталона; конфигурации weblegacy — fixture-тесты.
- [Неполный контракт `ActionServlet` (init-params)] → реализуется используемый набор (`config`, `validating`); расширяется по мере обнаружения в тестовых приложениях (принцип роадмапа).
- [Прямая реализация конвейера вместо chain] → поведение фиксируется тестами по критериям приёмки; при расхождении с weblegacy поведение правится по эталону.

## Open Questions

- Точная минорная версия Boot 4.0.x — определяется спайком первой задачи; откат на 3.5.x не влияет на спецификации и разбивку задач.
