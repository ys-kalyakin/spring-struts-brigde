# Tasks

## 1. Формы и экшены как Spring-бины (core)

- [x] 1.1 Реализовать `StrutsBeanRegistrar`: после разбора конфигурации регистрирует bean definitions в `BeanDefinitionRegistry` — экшены singleton (lazy-init), формы prototype, имена `strutsAction:<fqcn>`/`strutsForm:<fqcn>`; типы, уже зарегистрированные пользователем, и `DynaActionForm` пропускаются. Verify: unit-тест — конфигурация с экшеном и формой даёт определения с ожидаемыми именами и областями, явная регистрация не дублируется.
- [x] 1.2 Переработать `StrutsInstanceResolver`: пользовательский бин по типу (один кандидат) → авто-регистрация → страховочный `AutowireCapableBeanFactory.createBean`; несколько кандидатов — ошибка старта с перечислением кандидатов; кэш прямых экземпляров удалён. Verify: обновлённые unit-тесты `StrutsInstanceResolverTest` — приоритет пользовательского бина, создание через Spring, ошибка при двух кандидатах одного типа.
- [x] 1.3 IT: экшен и форма без аннотаций создаются Spring-ом — зависимость внедрена, singleton-экшен переиспользуется между запросами, форма свежая на каждый запрос, `@PostConstruct` вызван до первого execute, `@PreDestroy` — при закрытии контекста. Verify: IT-тесты в `spring-struts1-core` (TomcatTestServer) зелёные; полный прогон `mvn -pl spring-struts1-core test`.

## 2. Мост MessageResources → MessageSource

- [x] 2.1 Адаптер `MessageSource` поверх классических ресурсов + регистрация бандлов из `MessageResourcesConfig`: бандл по умолчанию становится `messageSource` контекста, если пользовательский не объявлен; именованные — под выделенными именами. Verify: unit-тест — `MessageSource.getMessage` возвращает то же, что классический API, включая локаль из сессии и формат `???ru_RU.key???`.
- [x] 2.2 Тест приоритета: при пользовательском бине `messageSource` основной не подменяется, бандлы Struts доступны под выделенными именами. Verify: unit-тест с пользовательским `messageSource` зелёный.

## 3. База тегов и сервисы логики (taglib)

- [x] 3.1 Ввести `SpringTagSupport`/`SpringBodyTagSupport` (ленивый доступ к `WebApplicationContext`, хелперы получения бинов) и `StrutsTagServicesConfiguration` с фиксированными именами сервисов. Verify: unit-тест — тег на новой базе получает контекст и бин сервиса.
- [x] 3.2 Реализовать `TagMessageResolver` (локаль из сессии/запроса, формат `???ru_RU.key???`) как Spring-компонент. Verify: unit-тесты локали и формата отсутствующих ключей.
- [x] 3.3 Реализовать `TagUrlBuilder`, `TagFormLocator`, `ErrorsMessagesRenderer` как Spring-компоненты. Verify: unit-тесты — URL с модульным префиксом и кодированием, поиск формы в request/session, рендер ошибок с header/footer/prefix/suffix.
- [x] 3.4 Мигрировать соответствующие методы `TagUtils` на делегирование сервисам из контекста текущего запроса, остальной статический API без изменений. Verify: существующие тесты `TagUtils` проходят; тест переопределения сервиса меняет вывод.

## 4. Перевод тегов на Spring-реализацию

- [x] 4.1 Библиотека `bean` — теги на новой базе и сервисах. Verify: приёмочные сценарии спеки `bean-taglib` (write, message, define, size, header/cookie/parameter) проходят, вывод идентичен.
- [x] 4.2 Библиотека `html` — теги на новой базе и сервисах (form, виджеты, errors/messages, img/link/rewrite). Verify: приёмочные сценарии спеки `html-taglib` проходят, HTML-вывод идентичен.
- [x] 4.3 Библиотека `logic` — теги на новой базе и сервисах (iterate, сравнения, present/empty, redirect/forward). Verify: приёмочные сценарии спеки `logic-taglib` проходят.
- [x] 4.4 Библиотека `nested` — теги на новой базе и сервисах. Verify: приёмочные сценарии спеки `nested-taglib` проходят.

## 5. Spring-JSP-стек

- [x] 5.1 Зарегистрировать `RequestContextFilter` в конфигурации движка. Verify: IT — `spring:message` на странице движка рендерит сообщение из бандла Struts.
- [x] 5.2 Регистрировать `BeanNameResolver` (имена из Spring-контекста) через `JspApplicationContext` однократно до первой JSP. Verify: IT — EL-выражение по имени бина отрендерено; повторная инициализация не падает.
- [x] 5.3 IT смешанной страницы: `html:form` с `html:text`, `spring:message` и EL-доступ к бину на одной JSP. Verify: IT зелёный, все три конструкции отрендерены.

## 6. Демонстрация и финальная интеграция

- [x] 6.1 Демо-страница и экшен в `examples`: экшен с `@Autowired`-сервисом, форма с `@Autowired` и `@PostConstruct`, JSP, смешивающая Struts- и Spring-теги. Verify: IT `ExamplesTest` покрывает демо-сценарий.
- [x] 6.2 Полный регресс: `mvn test` всех модулей, существующие IT `blank` и `examples` без изменений ожиданий. Verify: сборка зелёная целиком; `openspec validate --strict` по изменению проходит.

## Workflow follow-up

- Archive the change after the project's review requirements are satisfied.
- Verify the archived result.
