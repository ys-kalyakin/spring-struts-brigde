# Proposal

## Why

Фаза 1 (`struts1-engine-core`) дала работающий движок и запустила портированный `apps/blank`, но его JSP пришлось переписать без тэг-библиотек — миграция реальных приложений без них невозможна: тэг-библиотеки Struts — второй столп совместимости наряду с конфигурацией (см. proposal.md фазы 1 и принципы совместимости роадмапа). Фаза 2 реализует тэг-библиотеки `struts-bean`, `struts-html`, `struts-logic`, `struts-nested` с теми же URI, TLD и поведением, что у weblegacy, и прогоняет целевое приложение `apps/examples` (сценарии exercise-taglib и dispatch).

## What Changes

- Создаётся модуль `spring-struts1-taglib`: 98 тегов четырёх библиотек, портированных из weblegacy `taglib` с сохранением пакетов `org.apache.struts.taglib.*`; TLD копируются без изменений (те же `<uri>`, имена и атрибуты тегов) — JSP подключают библиотеки теми же `taglib`-декларациями.
- В `spring-struts1-core` портируются утилиты, на которых стоят теги: `TagUtils`, `ResponseUtils`, `ModuleUtils` (классические API).
- Движок получает поддержку нескольких модулей конфигурации: init-params `config/<prefix>` (например `config/exercise`), выбор модуля по пути запроса (модуль по умолчанию — как раньше), модуль-относительные форварды. Phase-1 спеки `struts-config-parsing` и `request-processing` модифицируются.
- Маршрутизация дополняется path-mapped экшенами (`/action/*` наряду с `*.do`) — отложенное из фазы 1 решение.
- Портируется минимальный `DynaActionForm` (создание из `<form-property>`, initial-значения, populate, динамический доступ DynaBean): нужен dispatch-сценариям целевого приложения; DynaValidatorForm остаётся в фазе 3.
- Портируются dispatch-экшены из weblegacy `extras` (`org.apache.struts.extras.actions`): `DispatchAction`, `LookupDispatchAction`, `MappingDispatchAction`, `EventDispatchAction`, `ActionDispatcher`, `EventActionDispatcher`, `ForwardAction`, `IncludeAction`.
- Портируется `apps/examples` с модулями exercise и dispatch (JSP и исходники приложения — без изменений); секции upload и validator исключаются до фазы 3.
- Портированный `blank` переключается на оригинальные JSP из эталона (с тэг-библиотеками) вместо адаптации фазы 1 — регрессионная проверка базовых тегов.

## Capabilities

### New Capabilities

- `bean-taglib`: библиотека `struts-bean` (URI `http://struts.apache.org/tags-bean`): write, message, define, size, header, cookie, parameter, include, resource, struts — вывод значений, сообщения, определение переменных, доступ к данным запроса.
- `html-taglib`: библиотека `struts-html`: form и виджеты, errors/messages, img/link/rewrite, base/html, xhtml-режим; тег `javascript` откладывается до фазы валидации (зависит от commons-validator).
- `logic-taglib`: библиотека `struts-logic`: iterate, сравнения, match, present/empty, messagesPresent, redirect/forward.
- `nested-taglib`: библиотека `struts-nested` — вложенные обёртки над bean/html/logic с относительными путями свойств.
- `dynamic-form-beans`: `DynaActionForm` — динамические формы из `<form-property>` (сейчас без валидаторных вариантов).
- `dispatch-actions`: семейство dispatch-экшенов (DispatchAction, LookupDispatchAction, MappingDispatchAction, EventDispatchAction, ActionDispatcher) — выбор метода обработки по запросу.

### Modified Capabilities

- `struts-config-parsing`: init-params `config/<prefix>` задают модули конфигурации; `config` остаётся модулем по умолчанию (объединение списка файлов — как было).
- `request-processing`: маршрутизация выбирает модуль по префиксу пути запроса; добавляется path-mapped маппинг экшенов (`/action/*`).

## Impact

- **Структура:** новый Maven-модуль `spring-struts1-taglib` (зависимость от core); `spring-struts1-apps/examples` — новое тестовое приложение (war).
- **Зависимости:** jakarta.servlet.jsp-api (provided) для компиляции тегов; commons-validator не подключается (фаза 3).
- **Эталон:** `/home/helloween/projects/struts1-reference` — taglib-модуль (v1.5.0-rc2), TLD в `META-INF/tld`, `apps/examples`; HTML-вывод тегов сверяется с weblegacy.
- **Не входит в объём:** struts-el (EL-варианты тегов — отдельное решение после фазы), commons-validator/`ValidatorPlugIn`/`DynaValidatorForm`, multipart upload (html:file рендерится, но обработка загрузки — фаза 3), Tiles, полный xhtml-покрытие за пределами классического режима, остальные утилиты extras (DownloadAction и пр.).
- **Совместимость:** имена тегов, TLD, URI и структура сгенерированного HTML — по эталону weblegacy; критерии приёмки фазы 2 из ROADMAP.md.
