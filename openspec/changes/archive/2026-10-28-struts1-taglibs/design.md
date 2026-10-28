# Design

## Context

Фаза 1 дала движок (`DispatcherServlet` + `StrutsHandlerMapping`/`StrutsHandlerAdapter`, фасад `ActionServlet`, `MessageResources` поверх `MessageSource`, парсинг конфигов) и портированный `blank` с JSP без тэг-библиотек. Эталон фазы: weblegacy-модуль `taglib` — 4 библиотеки (bean 11, html 30, logic 17, nested 40 тегов) + `TagUtils`, TLD в `META-INF/tld`. Целевое приложение `apps/examples` использует **мульти-модульную конфигурацию** (`config/exercise`, `config/dispatch`; upload/validator — фаза 3), `DynaActionForm` в dispatch-конфиге и dispatch-экшены из модуля `extras` (`org.apache.struts.extras.actions`). См. proposal.md — Why.

## Goals / Non-Goals

**Goals:**
- Модуль `spring-struts1-taglib` с 98 тегами, TLD «как есть» из эталона, теми же URI.
- Портированные утилиты `TagUtils` (в taglib-модуле, как в эталоне) и `ResponseUtils`/`ModuleUtils` (в core).
- Мульти-модульная конфигурация и выбор модуля по запросу; path-mapped экшены.
- Минимальный `DynaActionForm`; семейство dispatch-экшенов (subset extras).
- Прогон `apps/examples` (exercise + dispatch) и `blank` на оригинальных JSP — на embedded Tomcat 11.

**Non-Goals:**
- struts-el, commons-validator/`ValidatorPlugIn`/`DynaValidatorForm`/`html:javascript` (фаза 3), multipart-обработка (рендер `html:file` — да, приём загрузки — нет), Tiles, `DownloadAction` и прочий непортящийся extras, отдельный артефакт extras.

## Decisions

1. **Портирование тегов — файл-в-файл из эталона** с сохранением пакетов `org.apache.struts.taglib.{bean,html,logic,nested}` и `TagUtils`; адаптация минимальная (jakarta уже в эталоне). TLD копируются байт-в-байт — контейнер находит их по `<uri>` внутри jar, декларации JSP работают без правок. Альтернатива — переизобрести теги поверх Spring — отклонена: теряется совместимость вывода и атрибутов.
2. **Общий источник правды для URL и сообщений — `TagUtils`.** Она разрешает модуль по запросу (атрибут `Globals.MODULE_KEY`, который адаптер уже выставляет), строит URL с контекстом и рендерит сообщения. `ModuleUtils` (`selectModule`/`getModuleConfig`) портируется в core; выбор модуля делается в `StrutsHandlerMapping` до диспетчеризации.
3. **Мульти-модульность.** `ActionServlet.initModuleConfig` разбирает все init-params с префиксом `config/` (суффикс после `config/` — префикс модуля со слэшем) в карту префиксов. В `StrutsHandlerMapping` добавляется выбор модуля по пути запроса (префикс после контекста; иначе модуль по умолчанию), выбранный `ModuleConfig` кладётся в атрибут запроса; адаптер и `TagUtils` читают модуль из запроса. Модуль-относительные форварды работают в пределах модуля; кросс-модульные — через атрибут `module` форварда (префикс подставляется к пути).
4. **Path-mapped экшены.** Свойство маппинга `servletMapping` принимает классический формат (`*.do` или `/action/*`): суффиксное — как сейчас, префиксное — отбрасывает префикс (опциональный суффикс тоже срезается). Значение по умолчанию — `*.do`.
5. **`DynaActionForm` — минимальный порт** (`org.apache.struts.action.DynaActionForm` поверх `DynaBean` commons-beanutils, которая уже есть): создание из `<form-property>` с `initial`-значениями, populate через существующий `RequestUtils.populate`, динамический доступ по имени/индексу. `FormBeanConfig.createActionForm` получает ветку для динамических форм. Валидаторные варианты (`DynaValidatorForm`) — фаза 3.
6. **Dispatch-экшены — subset extras в core.** Классы портируются с сохранением пакета `org.apache.struts.extras.actions`: `DispatchAction`, `LookupDispatchAction`, `MappingDispatchAction`, `EventDispatchAction`, `ActionDispatcher`, `EventActionDispatcher`, `ForwardAction`, `IncludeAction`. Положить в core (а не отдельный артефакт) — временно: при миграции по фазе 4 артефакты всё равно собирает стартер; при необходимости выделится отдельный модуль.
7. **`html:javascript` не портируется в этой фазе** (зависит от commons-validator): TLD остаётся без изменений, реализация тега появится вместе с валидатором в фазе 3 — JSP с этим тегом даст ошибку обработки. Остальные 29 тегов html — в объёме.
8. **Портирование examples — только работающие секции.** Модули `config` (корень), `config/exercise`, `config/dispatch`; секции upload/validator из web.xml и webapp исключаются до фазы 3. Исходники приложения (`org.apache.struts.webapp.examples.*`, `...dispatch.*`, `...validator.LocaleAction`) и JSP — из эталона без правок; `CustomFormBean`/`CustomActionMapping` уже поддержаны (`set-property` через `BaseConfig`).
9. **blank — на оригинальных JSP.** JSP из эталона (с `html:html`, `bean:message`, `logic:notPresent`) копируются как есть, замещая адаптацию фазы 1: базовая регрессия тегов bean/html/logic на реальном приложении.
10. **Тестовая стратегия.** IT на embedded Tomcat 11: каждая exercise-страница → 200 + ключевые маркеры структуры HTML (атрибуты, вложенность — по критерию совместимости вывода); интерактивные сценарии dispatch (методы по кнопкам/событиям) прогоняются POST-запросами; `blank` на оригинальных JSP; собственные страницы для nested и xhtml (examples их не exercise-ирует). Модульные тесты — для `TagUtils` (filter, URL), выбора модуля, path-mapped, `DynaActionForm` (initial/populate) и dispatch-экшенов.

## Risks / Trade-offs

- [Расхождение HTML-вывода с weblegacy] → теги портируются файл-в-файл, вывод фиксируется тестами по атрибутам/вложенности; эталонные exercise-страницы — fixture-тесты.
- [JSP 4.0 (Tomcat 11) против JSP 3.1 эталона] → API `jakarta.servlet.jsp.tagext` стабилен между 3.x и 4.0; риск закрывается IT-прогоном всех страниц на Tomcat 11.
- [Nested-теги зависят от стека `BodyTagSupport`] → портируется `NestedPropertyHelper`/`NestedReferenceHelper` как в эталоне; тесты на вложенность изолированы в собственных страницах.
- [Мульти-модульное состояние в сессии (формы разных модулей)] → фаза 1 хранит формы по имени form-bean; семантика эталона (общее пространство имён сессии) сохраняется как есть, расхождения фиксируются в tasks.
- [`TagUtils` тянет смежные мелочи core (ModuleUtils, ResponseUtils)] → портируются одной группой до тегов, чтобы теги компилировались без правок.
- [TLD объявляет тег без реализации (`html:javascript`)] → контейнер парсит TLD без инстанцирования классов; падает только JSP с этим тегом — поведение зафиксировано спекой.

## Open Questions

- Точка подключения struts-el (отдельный change после фазы 2 или внутри фазы 3) — не влияет на объём и спеки этой фазы.
