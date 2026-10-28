# Tasks

## 1. Скелет сборки и спайк стека

- [x] 1.1 Создать мультимодульный Maven в корне: родительский POM (Java 17+, Spring Framework 7), модули `spring-struts1-core` и `spring-struts1-apps`, Maven wrapper `./mvnw`; проверить, что `./mvnw -q validate` проходит и структура модулей на месте
- [x] 1.2 Спайк: тест с embedded Tomcat 11 (`tomcat-embed-core` + `tomcat-embed-jasper`) рендерит JSP на jakarta-стеке, зафиксировать выбранные версии Spring/Boot (при блокере — документированный откат на Boot 3.5.x в README корня); проверить, что тест рендера JSP зелёный

## 2. Конфигурационная модель и парсинг (`spring-struts1-core`)

- [x] 2.1 Портировать из weblegacy `core` конфигурационную модель `org.apache.struts.config` (BaseConfig, ActionConfig, FormBeanConfig, FormPropertyConfig, ForwardConfig, ExceptionConfig, MessageResourcesConfig, ControllerConfig, PlugInConfig, ModuleConfig + impl, фабрика) без зависимостей на commons-chain и validator; проверить юнит-тестом фабрики и регистрации элементов
- [x] 2.2 Портировать `ConfigRuleSet` и Digester-парсер `struts-config.xml` (DTD 1.4): form-beans, global-forwards, global-exceptions, action-mappings (локальные форварды/исключения, set-property, атрибуты forward/include), message-resources, plug-in; проверить fixture-тестом на `struts-config.xml` эталона `apps/blank` — все элементы зарегистрированы
- [x] 2.3 Реализовать контракт init-param `config` (список файлов через запятую, объединение в модуль по умолчанию) и init-param `validating`; проверить юнит-тестами: несколько файлов объединяются, отсутствующий и некорректный файл дают ошибку старта с именем файла

## 3. MessageResources поверх MessageSource

- [x] 3.1 Портировать API `org.apache.struts.util.MessageResources` (с фабрикой) поверх Spring `MessageSource`: локаль — `Globals.LOCALE_KEY` из сессии, иначе локаль запроса; отсутствующий ключ — `???<locale>.<key>???`; при старте в контекст приложения кладётся `Globals.MESSAGES_KEY`; проверить тестами: сообщение по ключу для локали сессии, именованный бандл по ключу `key`, формат `???ru_RU.<key>???`

## 4. Движок жизненного цикла запроса на Spring MVC

- [x] 4.1 Реализовать `StrutsHandlerMapping`: суффиксная маршрутизация (суффикс настраивается, по умолчанию `*.do`), 404 для незарегистрированных путей; проверить интеграционным тестом на embedded Tomcat: `/Welcome.do` → маппинг `/Welcome`, `/Nope.do` → 404, кастомный суффикс маршрутизируется
- [x] 4.2 Реализовать резолвинг экземпляров: классы без регистрации в Spring создаются напрямую по имени из конфига (фолбэк), зарегистрированные берутся из контекста; проверить юнит-тестами обоих режимов
- [x] 4.3 Реализовать `StrutsHandlerAdapter`: populate параметрами запроса через commons-beanutils (доступ к статическому `ConvertUtils` изолировать в один утилитарный класс), размещение формы по `scope` (request/session) под именем form-bean, валидация по `validate` (ошибки → `input` без вызова экшена, ошибки доступны в запросе; `validate=false` — без валидации), вызов execute, обработка маппинга с атрибутом `forward`/`include` без экшена; проверить интеграционными тестами на embedded Tomcat по сценариям спеки `request-processing`
- [x] 4.4 Реализовать семантику форвардов: локальные приоритетнее глобальных, `redirect=true` → HTTP-редирект, `redirect=false` → dispatch; проверить интеграционными тестами (конфликт имён, 302, контент JSP в том же запросе)
- [x] 4.5 Реализовать обработку исключений: `global-exceptions` и `exception` маппинга (локальные приоритетнее, подтипы обрабатываются), форвард на `path` + сообщение по `key` в запросе; неописанные исключения пробрасываются в контейнер; проверить интеграционными тестами всех трёх сценариев спеки `exception-handling`

## 5. Аннотации и Spring-бины

- [x] 5.1 Добавить стереотипные аннотации `@StrutsAction` (singleton) и `@StrutsForm` (prototype) с регистрацией сканированием Spring и внедрением зависимостей; проверить интеграционным тестом: один экземпляр экшена переиспользуется двумя запросами, форма создаётся заново на каждый запрос (состояние не переходит), зависимости внедрены в экшен и форму

## 6. Фасад ActionServlet и PlugIn

- [x] 6.1 Реализовать `ActionServlet` как фасад-бин: инициализация из `ServletContext` и init-params фронтального сервлета, владение `ModuleConfig`, работающий `action.getServlet()`; проверить тестом: `getInitParameter("config")` возвращает значение из web.xml, экшен получает servlet
- [x] 6.2 Реализовать жизненный цикл `PlugIn`: init при старте в порядке объявления, destroy при остановке, создание из Spring-контекста для бинов и прямое для остальных; проверить тестом фиксирующего плагина (init/destroy вызваны, зависимости бина внедрены)

## 7. Портирование apps/blank и интеграционный прогон

- [x] 7.1 Создать модуль `spring-struts1-apps` (агрегатор) с модулем blank (war): web.xml с `DispatcherServlet` (mapping `*.do`, init-params `config` и др.), портированный `struts-config.xml` эталона (тестовый плагин вместо `ValidatorPlugIn`), JSP без тэг-библиотек (scriptlets), `MessageResources.properties`; проверить, что war собирается `./mvnw -pl spring-struts1-apps/blank -am package`
- [x] 7.2 Написать интеграционный тест прогона blank на embedded Tomcat 11: старт без ошибок, `index.jsp` перенаправляет на `/Welcome.do`, `/Welcome.do` рендерит `Welcome.jsp`, сообщения по ключам из `MessageResources.properties`, атрибут `Globals.MESSAGES_KEY` в контексте; проверить, что тест зелёный
- [x] 7.3 Сквозная проверка фазы 1: `./mvnw verify` зелёный целиком; пройтись по критериям приёмки фазы 1 в `ROADMAP.md` и убедиться, что каждый покрыт тестом или задокументированным решением; проверить итоговый отчёт прогона

## Workflow follow-up

- Синхронизировать спеки и заархивировать change (`/opsx:sync`, `/opsx:archive`) после ревью.
- Проверить заархивированный результат (`openspec list`).
