# spring-struts-bridge

[![CI](https://github.com/ys-kalyakin/spring-struts-brigde/actions/workflows/ci.yml/badge.svg?branch=main)](https://github.com/ys-kalyakin/spring-struts-brigde/actions/workflows/ci.yml)

Воссоздание фреймворка **Struts 1** поверх актуального Spring: существующее Struts-приложение мигрирует на современный стек заменой зависимости, подключением Spring Boot стартера и минимальными правками кода (аннотации над формами и экшенами, чтобы они стали Spring-бинами). Эталон совместимости — [weblegacy/struts1](https://github.com/weblegacy/struts1) (линия 1.5.x, `jakarta.*`): его API, TLD и тестовые приложения служат критерием корректности реализации.

Ключевые принципы:

- пакеты `org.apache.struts.*` и публичный API Struts 1 сохраняются — замена артефакта компилируется;
- `struts-config.xml` остаётся источником конфигурации, экшены и формы становятся Spring-бинами;
- тэг-библиотеки Struts (bean, html, logic, nested, tiles) работают с теми же URI и TLD на движке Spring.

План работ по фазам и критерии приёмки — в [ROADMAP.md](ROADMAP.md). Планирование и реализация ведутся через [OpenSpec](openspec/) (`/opsx:propose`, `/opsx:apply`). Руководство по миграции существующего приложения — [docs/MIGRATION.md](docs/MIGRATION.md), живой пример — [spring-struts1-apps/demo](spring-struts1-apps/demo/).

## Совместимость

| Компонент | Версия |
|---|---|
| Java | 17+ |
| Spring Boot | 4.0.x (сборка и тесты проверены на 4.0.9) |
| Spring Framework | 7.0.x — выровнен с Boot импортом `spring-boot-dependencies` BOM |
| Контейнер | embedded Tomcat 11 (standalone Tomcat 11 — для WAR-развёртывания), `jakarta.servlet` 6.1 |
| Совместимость API | weblegacy/struts1 1.5.x (`jakarta.*`) |

Другие минорные версии Boot 4.0.x не гоняются в CI постоянно, но заявлены: автоконфигурация опирается только на публичный API Boot (условия по классам/именам бинов, `AutoConfiguration.imports`).

## Модули и артефакты

Группа `org.springstruts`:

| Артефакт | Назначение |
|---|---|
| `spring-struts1-core` | движок: конфигурационная модель (commons-digester), жизненный цикл запроса (`StrutsHandlerMapping`/`StrutsHandlerAdapter`), фасад `ActionServlet`, `MessageResources` на Spring `MessageSource`, аннотации `@StrutsAction`/`@StrutsForm` |
| `spring-struts1-taglib` | тэг-библиотеки struts-bean/html/logic/nested с классическими URI и TLD + commons-validator (`ValidatorPlugIn`) |
| `spring-struts1-tiles` | Tiles-definitions, контроллеры и `TilesAction` (1.x-совместимый стек) |
| `spring-struts1-starter` | Spring Boot автоконфигурация движка: свойства `spring.struts1.*`, регистрация фронт-сервлета с классическим маппингом, RequestContextFilter, default-servlet для embedded-контейнера |

Портированные тестовые приложения (`spring-struts1-apps/*`) — `blank`, `examples`, `cookbook`, `mailreader` из эталонного репозитория weblegacy — служат интеграционными тестами против собранного стартера; `demo` — минимальный пример миграции с аннотированными экшеном и формой (executable WAR).

## Структура проекта

```
spring-struts1-core       — движок и конфигурационная модель
spring-struts1-taglib     — тэг-библиотеки и валидация
spring-struts1-tiles      — Tiles-стек
spring-struts1-starter    — Spring Boot стартер (автоконфигурация)
spring-struts1-apps/      — blank, examples, cookbook, mailreader (IT-набор), demo (миграционный пример)
docs/MIGRATION.md         — руководство по миграции
openspec/                 — спека-артефакты (proposal/specs/design/tasks по фазам)
```

Сборка: `./mvnw verify` — весь набор, включая интеграционные прогоны приложений на embedded Tomcat 11.

## Публикация

Канал публикации — внешняя конфигурация: сборка не содержит координат репозиториев, они передаются свойствами при активном профиле `publication`:

```bash
./mvnw -Ppublication deploy \
  -Dpublication.release.url=https://.../releases \
  -Dpublication.snapshot.url=https://.../snapshots \
  -DskipTests
```

Для файлового репозитория (локальная проверка):

```bash
./mvnw -Ppublication deploy \
  -Dpublication.release.url=file:$PWD/target/pubrepo \
  -Dpublication.snapshot.url=file:$PWD/target/pubrepo-snapshots \
  -DskipTests
```

SNAPSHOT-версии публикуются в `snapshotRepository`, релизные — в `repository`; подключение артефактов потребителем — обычное декларативное (`spring-struts1-starter` + `spring-struts1-taglib`/`-tiles`, см. [MIGRATION.md](docs/MIGRATION.md#шаг-1-замена-зависимости)).
