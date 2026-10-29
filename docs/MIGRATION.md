# Миграция Struts 1 приложения на spring-struts1

Руководство для существующего приложения Struts 1 (эталон совместимости —
[weblegacy/struts1](https://github.com/weblegacy/struts1) 1.5.x, линия
`jakarta.*`). Результат миграции: приложение работает на Spring Boot 4.0.x /
Spring Framework 7 / embedded Tomcat 11, при этом `struts-config.xml`, JSP,
тэг-библиотеки и message-resources остаются без изменений.

Полный живой пример миграции — [spring-struts1-apps/demo](../spring-struts1-apps/demo/);
раздел [«Проверка по демо»](#проверка-по-демо) показывает, что демо
воспроизводится строго шагами этого руководства.

## Шаг 1. Замена зависимости

Уберите артефакты Struts 1 (weblegacy: `struts1-core`, `struts1-taglib`,
`struts1-tiles` и сопутствующие) и подключите артефакты `org.springstruts`:

| Артефакт | Что заменяет | Обязательность |
|---|---|---|
| `spring-struts1-core` | `struts1-core` — движок и конфигурационная модель | всегда (приходит транзитивно из стартера) |
| `spring-struts1-taglib` | `struts1-taglib` — тэг-библиотеки bean/html/logic/nested + commons-validator | нужен, если приложение рендерит JSP тэгами Struts |
| `spring-struts1-tiles` | `struts1-tiles` — Tiles-definitions и контроллеры | нужен, если приложение использует Tiles |
| `spring-struts1-starter` | ручная настройка движка (ContextLoaderListener, web.xml) | всегда |

Пакеты `org.apache.struts.*` и публичный API сохранены — код приложения
компилируется без изменений. commons-зависимости (digester, beanutils,
validator, fileupload2) приходят транзитивно.

Минимальный набор для приложения с JSP:

```xml
<dependency>
  <groupId>org.springstruts</groupId>
  <artifactId>spring-struts1-starter</artifactId>
</dependency>
<dependency>
  <groupId>org.springstruts</groupId>
  <artifactId>spring-struts1-taglib</artifactId>
</dependency>
```

## Шаг 2. Класс приложения Spring Boot

Добавьте один класс — он же точка входа `java -jar`, он же бутстрап в
standalone-контейнере:

```java
@SpringBootApplication
public class MyApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(MyApplication.class, args);
    }
}
```

`web.xml` сокращается: декларация `ActionServlet` (или классического
`servlet-class`), `servlet-mapping` и все `init-param` удаляются — движок
настраивает стартер. Обычно остаются только `display-name` и
`welcome-file-list`.

**Обязательно**: удалите `metadata-complete="true"` из корня `<web-app>`, если
он есть — этот атрибут запрещает контейнеру сканировать
`ServletContainerInitializer`-ы, через которые Boot разворачивает контекст,
и приложение просто не стартует.

## Шаг 3. Свойства конфигурации

Классический init-param контракт читается из свойств Spring
(`spring.struts1.*`); дефолты совпадают с классикой, поэтому пустое
`application.properties` валидно:

| Свойство | Дефолт | Классический аналог |
|---|---|---|
| `spring.struts1.config` | `/WEB-INF/struts-config.xml` | init-param `config` (список через запятую) |
| `spring.struts1.config.<prefix>` | — | init-param `config/<prefix>` (модули) |
| `spring.struts1.validating` | `true` | init-param `validating` |
| `spring.struts1.mapping.url-pattern` | `*.do` | `servlet-mapping` (список: `*.do`, `/action/*`) |
| `spring.struts1.servlet-name` | `action` | `<servlet-name>` |
| `spring.struts1.enabled` | `true` | — (полное отключение стартера) |

Поддерживается `classpath:`-путь: `spring.struts1.config=classpath:struts-config.xml`
помещает конфиг в `src/main/resources` — так он одинаково доступен и
executable-запуску, и WAR-развёртыванию (так сделано в демо). Модульные
конфиги задаются свойствами с суффиксом-префиксом, например
`spring.struts1.config.admin=/WEB-INF/struts-config-admin.xml`.

## Шаг 4. Аннотации над экшенами и формами (необязательно)

Аннотации нужны только когда экшенам/формам нужна зависимость-инъекция Spring;
без них приложение работает в классическом фолбэке — классы создаются
напрямую по имени из конфига.

```java
@StrutsAction  // org.apache.struts.action.StrutsAction — singleton-бин
public class SubscribeAction extends Action {

    private final SubscribeService subscribeService;

    public SubscribeAction(SubscribeService subscribeService) { // @Autowired-конструктор
        this.subscribeService = subscribeService;
    }
    // execute(...) — классический контракт
}

@StrutsForm  // org.apache.struts.action.StrutsForm — prototype-бин
public class SubscribeForm extends ActionForm {
    // set/get-пары для populate, validate(...) — классический контракт
}
```

Семантика: `@StrutsAction` — один экземпляр на приложение (переиспользуется
между запросами, как классический синглтон-экшен); `@StrutsForm` — новый
экземпляр на запрос (состояние формы не разделяется). `struts-config.xml`
остаётся источником маппингов, форм, форвардов и message-resources — аннотации
делают классы бинами Spring, а не заменяют конфигурацию.

## Типовые правки

- **JSP-компиляция.** Для executable-режима добавьте `tomcat-embed-jasper`
  (compile) и `spring-boot-starter-tomcat` (provided — Boot repackage
  вернёт его в `WEB-INF/lib-provided`). В WAR-развёртывании на standalone
  Tomcat JSP работает из коробки.
- **JSP внутри jar не поддерживаются.** Контейнер компилирует JSP только из
  webapp-каталога/WAR: JSP-страницы должны оставаться в
  `src/main/webapp` (или в WAR), а не в jar-библиотеках.
- **Статические ресурсы.** Во встроенном контейнере классический
  default-servlet контейнера не зарегистрирован автоматически; стартер
  регистрирует его сам (`DefaultServletInitializer`, Tomcat 11). Ничего
  делать не нужно — `*.do`-маппинг со статикой не конфликтует.
- **Ошибки 404.** Стартер добавляет в маппинг фронт-сервлета путь `/error`,
  чтобы неклассифицированные запросы отдавали честный 404 через механизм
  ошибок Boot, а не 405.
- **Multipart/upload.** Движок парсит multipart сам (контракт
  `FormFile`); стартер держит multipart-автоконфигурацию Boot выключенной —
  загрузка работает как в классике.
- **RequestContextFilter.** Стартер регистрирует его как Boot-фильтр —
  request-context доступен и прямым JSP-запросам, не только форвардам.
- **Не объявляйте tomcat-embed-\* вручную** в compile/test/optional-скоупах:
  Maven «ближайший» маркер зависимости затеняет транзитивную, и Tomcat может
  выпасть из собираемого WAR. Пусть Tomcat приходит из
  `spring-boot-starter-tomcat`.

## Проверка по демо

Демо [spring-struts1-apps/demo](../spring-struts1-apps/demo/) — классическое
Struts-приложение (форма подписки с валидацией и форвардами), мигрированное
строго по шагам выше. Дельта миграции — четыре файла:

1. `pom.xml` — зависимости: стартер + taglib + jasper (+ provided
   starter-tomcat и servlet-api), repackage-плагин Boot (шаг 1, типовая
   правка jasper);
2. `src/main/java/demo/DemoApplication.java` — класс приложения (шаг 2);
3. `@StrutsAction` на `SubscribeAction` (внедряемый `SubscribeService`) и
   `@StrutsForm` на `SubscribeForm` (шаг 4);
4. `src/main/resources/application.properties` —
   `spring.struts1.config=classpath:struts-config.xml` (шаг 3).

Всё остальное — `struts-config.xml`, JSP, message-resources (`demo.Messages`)
— классические артефакты без изменений.

Проверить сборку и оба режима:

```bash
./mvnw -pl spring-struts1-apps/demo verify          # интеграционные тесты WAR на Tomcat + Boot-запуск
./mvnw -pl spring-struts1-apps/demo package
java -jar spring-struts1-apps/demo/target/demo.war  # http://localhost:8080/subscribe.do
```

Матрица совместимости версий — в [README](../README.md#совместимость).
