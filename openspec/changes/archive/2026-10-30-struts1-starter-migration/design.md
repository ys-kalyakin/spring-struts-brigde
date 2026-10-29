# Design: struts1-starter-migration

## Context

Движок конфигурируется приложениями вручную: web.xml объявляет `DispatcherServlet` с `AnnotationConfigWebApplicationContext` и классическими init-params (`config`, `config/<prefix>`, `validating`), контекст приложения импортирует `StrutsEngineConfiguration` (+ `StrutsTagServicesConfiguration`). `StrutsEngineInitializer` и `StrutsEngineRegistrar` получают `ServletConfig` через `ServletConfigAware` — в классическом режиме их обрабатывает дочерний контекст фронт-сервлета, где `ServletConfig` известен. Spring Boot в проекте не используется; Spring Framework закреплён на 7.0.10, Tomcat 11.0.27, Java 17. Интеграционные тесты приложений гоняются на встроенном `TomcatTestServer` (реальный контейнер).

## Goals / Non-Goals

**Goals:**

- Стартер = единственная точка подключения движка для Boot-приложения: бины, фронт-сервлет, контракт init-params через свойства.
- Классический WAR-режим (web.xml + `@Import`) остаётся рабочим без изменений.
- Миграция приложений `apps/*` и демо с аннотациями как исполняемое Boot-приложение.
- Руководство, матрица совместимости, инфраструктура публикации.

**Non-Goals:**

- Поддержка Boot 3.5.x (матрица заявляет только 4.0.x; движок использует базовый MVC API, откат возможен, но не проверяется в этом изменении).
- Публикация во внешний репозиторий (Maven Central): требуются учётные данные вне репозитория.
- Изменения семантик движка и тэг-библиотек; struts-el.

## Decisions

### 1. Модуль и зависимости стартера

Новый модуль `spring-struts1-starter` (артефакт `spring-struts1-starter`, пакет `org.apache.struts.spring.boot`). Зависимости: `spring-struts1-core` (обязательная), `spring-struts1-taglib` (optional — для условной интеграции), `spring-boot-autoconfigure`. Стартер не тянет веб-стартеры Boot (не навязывает приложениею стек); демо подключает `spring-boot-starter-webmvc` (актуальное имя в Boot 4; `spring-boot-starter-web` объявлен deprecated). Версия Boot закрепляется в родительском pom (линия 4.0, патч 4.0.6 — актуальная на момент планирования; финальная версия фиксируется при реализации по актуальному патч-релизу 4.0.x). Регистрация авто-конфигурации — `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`.

Альтернатива, отклонённая: держать тэг-сервисы в стартере безусловно — это потребовало бы жёсткой зависимости на taglib и ломало бы приложения без тэг-библиотек.

### 2. ServletConfig в Boot root-контексте (ключевая проблема)

В Boot root-контексте (`ServletWebServerApplicationContext`) `ServletConfigAware`-бины получают `null`: Boot регистрирует `WebApplicationContextServletContextAwareProcessor`, который резолвит `ServletContext` лениво, но `ServletConfig` всегда `null` (проверено по байткоду spring-boot-web-server 4.0.9). Классический режим не затронут: там конфигурацию бинов движка обрабатывает дочерний контекст фронт-сервлета, где `ServletConfig` известен.

Дополнительное ограничение порядка фаз: `StrutsEngineRegistrar` — `BeanDefinitionRegistryPostProcessor` и инстанцируется в фазе bean-factory post-processing, **до** регистрации `BeanPostProcessor`-бинов; значит, `@Bean`-объявленный BPP стартера не может доставить адаптер регистратору. Зато `ServletContextAware`/`EnvironmentAware` в root-контексте обрабатываются стандартным процессором, зарегистрированным до фазы BFPP, и работают даже для ранних бинов.

Решение — два уровня:

- **Движок (аддитивно, поведение не меняется)**: новый класс `org.apache.struts.spring.EnvironmentServletConfig` — адаптер `ServletConfig` над `Environment` (ленивое чтение свойств) с резолвингом ресурсов `classpath:` через classloader и остальных путей через `ServletContext`. `StrutsEngineRegistrar` дополнительно получает `ServletContextAware`/`EnvironmentAware`: если `ServletConfig` не внедрён (Boot-режим), но доступен контекст сервлета или задано свойство `spring.struts1.config`, он сам собирает адаптер; иначе ведёт себя как прежде (no-op с debug-логом). Обычный путь классического режима и ресурс-резолвинг не меняются.
- **Стартер**: ранний `BeanPostProcessor` (static `@Bean`, регистрируется в фазе `registerBeanPostProcessors`) внедряет адаптер остальным `ServletConfigAware`-бинам (инициализатор и прочие обычные бины) и синхронизирует маппинг `StrutsHandlerMapping` со свойством. Обычные бины создаются после старта веб-сервера, поэтому `ServletContext` к этому моменту доступен.

Альтернативы, отклонённые: (а) конструкторная передача `ServletConfig` из авто-конфигурации — registrar обязан быть `BeanDefinitionRegistryPostProcessor`, обычный @Bean-инстанс не поддерживается; (б) только BPP — не достаёт до регистратора по порядку фаз; (в) переопределение бина регистратора в стартере — конфликт имён с `StrutsEngineConfiguration` и дублирование списка бинов движка.

### 3. Адаптер читает Environment, а не @ConfigurationProperties-бин

`StrutsEngineRegistrar` и `BeanPostProcessor` стартера срабатывают в фазах, когда `ConfigurationPropertiesBindingPostProcessor` ещё не гарантированно зарегистрирован; принудительное создание типизированного бина свойств на этом пути дало бы незабинженные значения. Адаптер резолвит init-params лениво (при вызовах `getInitParameter`) прямо из `Environment`: `spring.struts1.config`, `spring.struts1.config.<префикс>` (перечисление property sources), `spring.struts1.validating`. Типизированный `Struts1Properties` (`spring.struts1.*`) используется только поздними бинами (регистрация сервлета), где биндинг гарантирован.

### 4. Фронт-сервлет и маппинг

Авто-конфигурация определяет собственные бины `DispatcherServlet` (имя `dispatcherServlet`) и `ServletRegistrationBean` (имя `dispatcherServletRegistration`) с шаблонами из `spring.struts1.mapping.url-pattern` (по умолчанию `*.do`, список) **плюс `/error`**: с не-корневым маппингом контейнерный error-dispatch иначе не находит сервлет, и незарегистрированные пути не дают корректных 404. Имена — канонические для Boot: его `DispatcherServletAutoConfiguration` в Boot 4 уходит в сторону по **имени** бина (не по типу), и его регистрация отступает при наличии `dispatcherServletRegistration`; типизация имени избавляет от зависимости от порядка обработки авто-конфигураций. `@AutoConfigureBefore` (по имени класса Boot) сохранён для определённости. Сервлет, созданный как бин root-контекста, получает его же через `ApplicationContextAware` (так работает и авто-конфигурация Boot — проверено по исходнику `FrameworkServlet`), child-контекст не создаётся, и движок находит в root-контексте `StrutsHandlerMapping`/`StrutsHandlerAdapter`. То же свойство настраивает маппинг обработчика запросов движка, а его приоритет поднимается до высшего: в root-контексте resource-handler Boot (`/**`) иначе перехватывает запросы `*.do` раньше маппинга движка. Для error-страниц Boot дополнительно публикуется бин `DispatcherServletPath` (mounted at root). Альтернатива, отклонённая: дочерний `AnnotationConfigWebApplicationContext` как в классическом режиме — дублирует контекст Boot и отрезает сервлет от бинов приложения.

Дополнительно: multipart в Boot-режиме остаётся за движком — стартер публикует no-op `multipartResolver`, чтобы `DispatcherServlet.checkMultipart` не перехватывал multipart-запросы до populate (классическая семантика разбора multipart внутри жизненного цикла), а авто-конфигурация Boot отступала.

### 5. Условная интеграция тэг-сервисов

Nested `@Configuration` внутри авто-конфигурации: `@ConditionalOnClass(StrutsTagServicesConfiguration.class)` + `@Import`. Зависимость на taglib — optional, поэтому условие по классу, а не по бину.

### 6. Миграция приложений apps/*

Для каждого приложения: зависимость `spring-struts1-starter`; класс `<App>Application extends SpringBootServletInitializer` с `@SpringBootApplication`; web.xml сокращается до минимума (display-name, welcome-file-list) — сервлет, маппинг и init-params больше не объявляются; модульные конфиги `config/<prefix>` переносятся в свойства `spring.struts1.config.<префикс>` там, где использовались. struts-config.xml остаётся в `/WEB-INF` (значение по умолчанию свойства `spring.struts1.config` совпадает с классическим). Интеграционные тесты (`TomcatTestServer`) не меняются: Boot-контекст стартует внутри развёрнутого WAR.

### 7. Демо-приложение

Модуль `spring-struts1-apps/demo` (артефакт `spring-struts1-demo`): executable WAR без web.xml (аннотационный бутстрап Boot) с `tomcat-embed-jasper` — JSP в Boot работают в executable WAR (в executable jar JSP не поддерживаются, руководство фиксирует это). Экшен и форма с `@StrutsAction`/`@StrutsForm`, зависимость экшена через `@Autowired`; struts-config.xml, JSP и message-resources — классические. IT: (а) разворачивание собранного WAR на `TomcatTestServer` — полный набор сценариев вровень с другими приложениями; (b) smoke-запуск через `SpringApplication` (эквивалент `java -jar`) с docBase `src/main/webapp` на случайном порту — страница, форма, сообщение.

### 8. Публикация

Профиль `publication` в родительском pom: `distributionManagement` с URL/credentials из пользовательских свойств (`-Dpublication.release.url=...` и т.п.), по умолчанию не активен. Проверка — deploy в файловый репозиторий (`file:`) и подключение опубликованных артефактов. Выбор внешнего канала остаётся открытым вопросом роадмапа и решается владельцем репозитория снаружи кода; README документирует запуск.

### 9. Версии и выравнивание Spring

Импорт `spring-boot-dependencies` BOM (4.0.x) в `dependencyManagement` родителя; явная `spring.version` проекта выравнивается с версией, управляемой Boot, чтобы избежать конфликта Spring Framework 7.0.x. Полный `./mvnw verify` — арбитр.

## Risks / Trade-offs

- [Boot root-контекст не внедряет `ServletConfigAware`] → адаптер на стороне движка (`EnvironmentServletConfig`) для регистратора + BPP стартера для обычных бинов (решение 2); классический режим не затронут, IT приложений покрывают оба режима.
- [BPP стартера не достаёт до `BeanDefinitionRegistryPostProcessor`-бинов по порядку фаз] → регистратор собирает адаптер сам из `ServletContextAware`/`EnvironmentAware` (решение 2).
- [Ранняя инициализация бинов при чтении свойств в фазе BFPP] → адаптер читает `Environment` лениво, `@ConfigurationProperties`-бин вне критического пути (решение 3).
- [Fallback-адаптер мог бы менять поведение не-web контекстов, импортирующих движок] → адаптер собирается только при доступном `ServletContext` или явно заданном `spring.struts1.config`; иначе прежний no-op (решение 2).
- [Двойная регистрация `DispatcherServlet` с авто-конфигурацией Boot] → собственные бины сервлета/регистрации, Boot уходит по `@ConditionalOnMissingBean`; IT демо проверяют отсутствие дубликата.
- [JSP в executable jar не работают] → демо и руководство: executable WAR с jasper (решение 7).
- [Конфликт версий Spring Framework проекта и Boot 4.0.x] → импорт BOM Boot, полный verify (решение 9).
- [Boot 4 переименовывает артефакты и свойства] → используются актуальные имена (`spring-boot-starter-webmvc`); точные имена свойств (например, регистрация JSP-сервлета) проверяются при реализации.

## Migration Plan

Изменение аддитивно: классический режим не меняется, приложения мигрируют на стартер внутри этого изменения. Откат — revert ветки: движок и приложения возвращаются к ручному бутстрапу.

## Open Questions

- Точный патч-релиз Boot 4.0.x фиксируется при реализации (решение 1); на подход, спеки и задачи не влияет.
