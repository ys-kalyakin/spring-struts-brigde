# ROADMAP — Struts 1 на движке Spring

Проект **spring-struts-bridge**: воссоздание фреймворка Struts 1 поверх актуального Spring как drop-in замены. Роадмап разбивает работу на фазы; каждая фаза реализуется отдельным OpenSpec-изменением и завершается работой целевого тестового приложения из эталонного репозитория [weblegacy/struts1](https://github.com/weblegacy/struts1).

## Видение

Существующее Struts 1 приложение должно мигрировать на современный стек минимальными усилиями:

1. **Заменить зависимость**: артефакт Struts 1 (weblegacy 1.5.x) → `spring-struts1` — код приложения компилируется без изменений.
2. **Подключить стартер**: Spring Boot стартер автоматически настраивает движок (в фазе 4).
3. **Минимальные правки**: расставить аннотации над формами и экшенами, чтобы они стали Spring-бинами; всё остальное — struts-config.xml, JSP, тэг-библиотеки, message-resources — работает как раньше, но на движке Spring.

Эталон совместимости — [weblegacy/struts1](https://github.com/weblegacy/struts1) (линия 1.5.x, уже на `jakarta.*`): его публичный API, TLD и тестовые приложения (`apps/blank`, `apps/examples`, `apps/cookbook`, `apps/mailreader`) — критерий того, что реализация верна. Локальный клон эталона: `/home/helloween/projects/struts1-reference`.

## Принципы совместимости

Сквозные для всех фаз; каждое изменение проверяется на соответствие:

1. **Пакеты и API**: сохраняем пакеты `org.apache.struts.*` и публичный API Struts 1 — замена артефакта должна компилироваться.
2. **Конфигурация**: `struts-config.xml` остаётся главным источником конфигурации (маппинги, формы, форварды, исключения, message-resources, плагины). Аннотации `@StrutsAction`/`@StrutsForm` — способ сделать классы бинами Spring, а не замена конфигу.
3. **Тэг-библиотеки**: те же URI, TLD, имена тегов и поведение для `struts-bean`, `struts-html`, `struts-logic`, `struts-nested`, `struts-tiles`.
4. **Jakarta-стек**: `jakarta.servlet` / `jakarta.servlet.jsp` / `jakarta.el` — соответствие weblegacy 1.5.x.
5. **Поведение**: семантики Struts 1 воспроизводятся — populate по правилам commons-beanutils, локаль в сессии (`Globals.LOCALE_KEY`), формат отсутствующих ключей `???<locale>.<key>???`, приоритет локальных форвардов над глобальными.

## Целевой стек

- **Spring Boot 4.0.x / Spring Framework 7**, Java 17+, embedded Tomcat 11 (jakarta servlet 6.1).
  ⚠ Точная минорная версия Boot и работоспособность JSP + jakarta.el на Tomcat 11 проверяются в первом спринте фазы 1; при блокере — откат на Spring Boot 3.5.x без изменения кода движка (используется базовый MVC API).
- **Структура**: мультимодульный Maven — `spring-struts1-core` (движок и конфигурационная модель), `spring-struts1-apps` (портированные тестовые приложения); в поздних фазах — `spring-struts1-taglib`, `spring-struts1-tiles`, `spring-struts1-starter`.

## Ключевые архитектурные решения

Зафиксированы на этапе планирования; фазовые изменения уточняют детали в своих design-артефактах:

- **Точка входа**: Spring MVC `DispatcherServlet`; движок — `StrutsHandlerMapping` + `StrutsHandlerAdapter`. `ActionServlet` сохраняется как фасад совместимости (init-param `config`, рабочая ссылка `action.getServlet()`).
- **Парсинг конфига**: commons-digester с правилами, портированными из weblegacy `core` (`org.apache.struts.config`).
- **Populate форм**: commons-beanutils (совместимая семантика конвертации); переход на Spring `ConversionService` — кандидат рефакторинга после фазы 1. *Реализовано*: изменение `populate-conversion-service` — populate выполняется собственным сервисом конвертации движка на Spring `ConversionService` с семантикой дефолтного реестра beanutils; статический реестр в populate не участвует.
- **MessageResources**: поверх Spring `MessageSource`; локаль — Struts-семантика (сессия, инициализация из запроса).
- **Бины**: `@StrutsAction` — singleton; `@StrutsForm` — prototype (экземпляр на запрос, состояние не разделяется); классы без регистрации в Spring создаются напрямую (классический режим).

## Соответствие «фаза → change»

| Фаза | Change | Целевое приложение weblegacy |
|---|---|---|
| 1. Базовый функционал движка | `struts1-engine-core` | `apps/blank` |
| 2. Полные тэг-библиотеки | `struts1-taglibs` | `apps/examples` |
| 3. Tiles, upload, validation | `struts1-tiles-validation` | секции validator/upload в `apps/examples`, `apps/cookbook`, `apps/mailreader` |
| 4. Стартер и миграция | `struts1-starter-migration` | весь набор `apps/*` как интеграционные тесты |

**Правило**: фаза может быть разбита на несколько изменений, если при запуске окажется крупной; решение принимается в момент создания change фазы, а не в этом роадмапе. Имена выше — рекомендуемые точки старта.

## Фаза 1 — Базовый функционал движка

**Change:** `struts1-engine-core` · **Целевое приложение:** `apps/blank`

**Объём**: скелет мультимодульного Maven-проекта; портирование конфигурационной модели и Digester-правил из weblegacy `core`; движок жизненного цикла запроса на Spring MVC; аннотации `@StrutsAction`/`@StrutsForm` и резолвинг бинов; MessageResources поверх MessageSource; декларативная обработка исключений; фасад `ActionServlet`; портирование и интеграционный прогон `blank`.

**Критерии приёмки:**

- *Конфигурация*: старт с валидным `struts-config.xml` регистрирует маппинги, формы, форварды, message-resources; список файлов в init-param `config` объединяется; отсутствующий/некорректный файл — ошибка старта с именем файла.
- *Маршрутизация*: запрос `/x.do` обрабатывается экшеном с path `/x` (суффикс настраивается); незарегистрированный путь — 404.
- *Жизненный цикл*: populate формы → валидация (`validate=true`: ошибки → `input`-страница с ошибками; `validate=false`: без валидации) → `execute` → форвард.
- *Форварды*: локальные приоритетнее глобальных; `redirect=true` — HTTP-редирект, иначе dispatch.
- *MessageResources*: сообщения по ключу с локалью из сессии; отсутствующий ключ — `???ru_RU.<key>???`.
- *Исключения*: объявленные в `global-exceptions`/маппинге — форвард + `ActionMessage`; неописанные — проброс в контейнер.
- *Экшены как бины*: `@StrutsAction` — singleton, зависимости внедряются (`@Autowired`), экземпляр переиспользуется между запросами.
- *Формы как бины*: `@StrutsForm` — prototype, каждый запрос получает свой экземпляр (изоляция состояния), зависимости формы внедряются.
- *Фолбэк*: приложение без аннотаций работает — классы создаются напрямую по имени из конфига.
- *PlugIn*: init при старте, destroy при остановке; из Spring-контекста, если бин, иначе прямым созданием.
- *Инфраструктура*: `./mvnw verify` зелёный; JSP рендерится на embedded Tomcat 11 (или документированный откат на Boot 3.5.x).

**Открытые вопросы**: точная минорная версия Boot 4; объём init-param контракта `ActionServlet` в фазе 1; beanutils vs Spring `ConversionService` (решить по итогам прогонов совместимости); path-mapped экшены (`/action/*` наряду с `*.do`) — фаза 1 или 2.

## Фаза 2 — Полные тэг-библиотеки

**Change:** `struts1-taglibs` · **Целевое приложение:** `apps/examples` (exercise-taglib)

**Объём**: модуль `spring-struts1-taglib`; тэг-библиотеки `struts-bean` (write, message, define, size, header, cookie, parameter, include, resource, struts), `struts-html` (форма и все виджеты, img, link, rewrite, javascript, errors, xhtml-режим), `struts-logic` (iterate, equal/greater/less, match, present, empty, redirect, forward), `struts-nested`; интеграционные тесты рендера на реальном Tomcat.

**Критерии приёмки**:

- TLD/URI/имена тегов идентичны weblegacy (тэг-библиотеки подключаются теми же `taglib`-декларациями в JSP).
- `apps/examples` рендерится и проходит сценарии exercise-taglib (dispatch, exercise-страницы).
- Структура сгенерированного HTML совместима с weblegacy (атрибуты, вложенность).

**Открытые вопросы**: EL-варианты (struts-el) — в этой фазе или отдельной; полный xhtml-режим.

## Фаза 3 — Tiles, upload, validation

**Change:** `struts1-tiles-validation` · **Целевое приложение:** секции validator/upload в `apps/examples`, `apps/cookbook`, `apps/mailreader`

**Объём**: модуль `spring-struts1-tiles` (definitions, контроллеры); multipart upload через Spring с совместимым `FormFile`/`MultipartRequestHandler`; commons-validator + `ValidatorPlugIn`; `DynaActionForm`/`DynaValidatorForm`.

**Критерии приёмки**:

- Секции validator/upload в `apps/examples` работают (валидация по validation.xml, загрузка файла).
- `apps/cookbook` и `apps/mailreader` запускаются и проходят основные сценарии.
- Плагины (валидация, tiles) подключаются декларативно через struts-config.xml.

**Открытые вопросы**: tiles (1.x-совместимый) против tiles2 из weblegacy; зависимости mailreader-dao.

## Фаза 4 — Стартер и миграция

**Change:** `struts1-starter-migration` · **Целевое приложение:** весь набор `apps/*` как интеграционные тесты

**Объём**: `spring-struts1-starter` с авто-конфигурацией и свойствами настройки; руководство по миграции (замена зависимости, аннотации, типовые правки); матрица совместимости; настройка публикации артефактов.

**Критерии приёмки**:

- Миграция демо-приложения = замена зависимости + подключение стартера + аннотации над формами/экшенами (+ минимальные задокументированные правки).
- Набор `apps/*` проходит как интеграционные тесты против собранного стартера.
- Руководство по миграции описывает все известные ручные шаги.

**Открытые вопросы**: канал публикации (Maven Central / внутренний репозиторий); матрица поддерживаемых версий Boot. *Разрешено при реализации фазы 4:* канал публикации — внешняя конфигурация через профиль `publication` (README, раздел «Публикация»); матрица Boot-версий — 4.0.x заявлена (README, раздел «Совместимость»).

## Известные риски

- [JSP и кастомные тэги требуют контейнера JSP] → интеграционные тесты всегда на embedded Tomcat, не на mock-стеке.
- [Статический глобальный реестр ConvertUtils в commons-beanutils] → изоляция работы с реестром в одном утилитарном классе движка.
- [Boot 4.0 — модульный релиз, возможны сюрпризы в JSP-сборке] → проверка в первом спринте фазы 1; откат на Boot 3.5.x не меняет код движка.
- [Legacy-код ожидает синглтон `ActionServlet` и его init-params] → фасад с рабочим контрактом; неполный набор init-params закрывается по мере обнаружения в тестовых приложениях.
- [Расхождения с weblegacy-поведением] → конфигурации и сценарии weblegacy подключаются как fixture-тесты.
