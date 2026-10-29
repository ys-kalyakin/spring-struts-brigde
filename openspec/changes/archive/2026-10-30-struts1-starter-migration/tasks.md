# Tasks: struts1-starter-migration

## 1. Родительский pom: версии Boot и модуль стартера

- [x] 1.1 Зафиксировать версию Spring Boot 4.0.x (актуальный патч линии 4.0) в родительском pom, импортировать `spring-boot-dependencies` BOM в `dependencyManagement`, выровнять `spring.version` с версией, управляемой Boot; `./mvnw -pl spring-struts1-core -am verify` зелёный после выравнивания
- [x] 1.2 Добавить модуль `spring-struts1-starter` в `<modules>` и создать скелет модуля: pom с зависимостями `spring-struts1-core`, optional `spring-struts1-taglib`, `spring-boot-autoconfigure`, provided `jakarta.servlet-api`; `./mvnw -pl spring-struts1-starter -am compile` проходит

## 2. Авто-конфигурация стартера

- [x] 2.1 `Struts1Properties` (`spring.struts1.*`): `enabled` (true), `config` (`/WEB-INF/struts-config.xml`), `configModules` (Map → `config/<префикс>`), `validating` (true), `mapping.urlPattern` (`*.do`, список), `servletName` (`action`); unit-тест биндинга свойств (`./mvnw -pl spring-struts1-starter test`)
- [x] 2.2 Адаптер `EnvironmentServletConfig` в `spring-struts1-core` (implements ServletConfig): ленивое чтение init-params из `Environment` (`config`, `config/<префикс>` из `spring.struts1.config.*`, `validating`, дефолты `/WEB-INF/struts-config.xml` и `true`), резолвинг ресурсов `classpath:` и через `ServletContext`; unit-тест: значения по умолчанию, свойство вместо дефолта, модульный параметр `spring.struts1.config.admin` → `getInitParameter("config/admin")`, перечисление имён параметров
- [x] 2.3 Fallback регистратора: `StrutsEngineRegistrar` получает `ServletContextAware`/`EnvironmentAware` и при отсутствии `ServletConfig` сам собирает адаптер (если доступен `ServletContext` или задан `spring.struts1.config`), ресурс-резолвинг через адаптер; unit-тест: plain-контекст без ServletConfig с `spring.struts1.config=classpath:fixture` регистрирует типы из struts-config; BPP стартера `StrutsBootConfigurer`: внедряет адаптер остальным `ServletConfigAware`-бинам и синхронизирует маппинг `StrutsHandlerMapping` из свойства
- [x] 2.4 `Struts1AutoConfiguration`: импорт `StrutsEngineConfiguration`, собственные бины `DispatcherServlet` и `ServletRegistrationBean` из `Struts1Properties` (маппинг согласован с обработчиком запросов движка), условный nested `@Configuration` с `@ConditionalOnClass` для тэг-сервисов; файл `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`; тест с `ApplicationContextRunner`: бины движка зарегистрированы, с taglib в classpath — тэг-сервисы тоже, без taglib — старт без ошибок, `spring.struts1.enabled=false` отключает всё, Boot-овская регистрация `DispatcherServlet` не дублируется
- [x] 2.5 Интеграционный тест стартера на embedded Tomcat (по образцу `TomcatTestServer`): мини-приложение на Boot со стартером и struts-config в ресурсах — запрос `*.do` проходит жизненный цикл populate → валидация → execute → форвард; path-mapped вариант `/action/*` через `spring.struts1.mapping.url-pattern`

## 3. Миграция приложений apps/* на стартер

- [x] 3.1 blank: зависимость стартера, класс `BlankApplication extends SpringBootServletInitializer` с `@SpringBootApplication`, web.xml сокращён до display-name и welcome-file-list (сервлет, маппинг и init-params удалены); интеграционный тест `BlankAppTest` проходит без изменений сценариев — `./mvnw -pl spring-struts1-apps/blank verify`
- [x] 3.2 examples: та же миграция; модульные конфиги `config/<prefix>` из web.xml перенесены в свойства `spring.struts1.config.<префикс>`; `ExamplesTest` проходит — `./mvnw -pl spring-struts1-apps/examples verify`
- [x] 3.3 cookbook и mailreader: та же миграция; `CookbookAppTest`, `MailreaderAppTest` и тесты mailreader-dao проходят — `./mvnw -pl spring-struts1-apps/cookbook,spring-struts1-apps/mailreader,spring-struts1-apps/mailreader-dao verify`
- [x] 3.4 Убедиться, что классический режим не сломан: тесты `spring-struts1-core` (ручная конфигурация движка) проходят — `./mvnw -pl spring-struts1-core verify`

## 4. Демо-приложение с аннотациями

- [x] 4.1 Модуль `spring-struts1-apps/demo` (артефакт `spring-struts1-demo`, packaging war, executable через `spring-boot-maven-plugin`): зависимости стартер, taglib, `spring-boot-starter-webmvc`, `tomcat-embed-jasper`; аннотированные `@StrutsAction` (singleton, зависимость `@Autowired`) и `@StrutsForm` (prototype), struts-config.xml, JSP, message-resources — сборка `./mvnw -pl spring-struts1-apps/demo package` даёт executable WAR
- [x] 4.2 Интеграционный тест разворачивания WAR на `TomcatTestServer`: страница рендерится, форма заполняется и валидируется (ошибки → input), сообщения по локали, singleton экшена между запросами, отдельная форма на запрос — `./mvnw -pl spring-struts1-apps/demo verify`
- [x] 4.3 Smoke-тест Boot-запуска: `SpringApplication` на случайном порту с docBase `src/main/webapp` (эквивалент `java -jar`) — страница, форма и сообщение доступны

## 5. Руководство, матрица, публикация

- [x] 5.1 `docs/MIGRATION.md`: шаги миграции (замена артефакта, стартер, класс приложения, свойства, аннотации), типовые правки (jasper, static-ресурсы, JSP-ограничение jar/WAR), ссылка на демо; проверка — миграция демо воспроизводима строго по шагам руководства
- [x] 5.2 Матрица совместимости в README: Boot 4.0.x, Java 17+, Spring Framework (выровненная версия), Tomcat 11 / jakarta servlet 6.1, состав модулей и артефактов; обновить раздел структуры проекта
- [x] 5.3 Профиль `publication` в родительском pom (`distributionManagement` из свойств `-Dpublication.release.url` и т.п.) и раздел «Публикация» в README; проверка — `./mvnw -Ppublication deploy -Dpublication.release.url=file:target/pubrepo -DskipTests` публикует все модули, и тестовое подключение артефактов резолвится

## 6. Финальная интеграция

- [x] 6.1 Полный прогон `./mvnw verify` зелёный на всех модулях, включая стартер и демо
- [x] 6.2 Проверка двойной регистрации сервлета и отключения стартера на реальном приложении: `spring.struts1.enabled=false` на blank Boot-запуске — движок не стартует, приложение не падает; при включённом — единственный `DispatcherServlet`

## Workflow follow-up

- OpenSpec: обновить статусы задач по ходу реализации (`openspec status --change struts1-starter-migration`), в конце — `openspec archive` и коммит фазы 4.
- Дописать в README/ROADMAP разрешение открытых вопросов фазы 4: канал публикации (документирован как внешний конфиг), матрица Boot-версий (4.0.x заявлена).
