# Design

## Context

См. `proposal.md` — Why. Сборка проекта уже воспроизводима локально: `./mvnw -B -ntp verify` на JDK 17 собирает 10 модулей реактора, прогоняет 205 тестов (включая интеграционные на embedded Tomcat с портом `0`) за ~31 с на прогретом кэше и даёт 4 WAR и 4 JAR. Maven wrapper зафиксирован на 3.9.9, `maven.compiler.release=17`. Основная ветка в `origin` — `main`; GitHub-репозиторий — `ys-kalyakin/spring-struts-brigde` (написание в remote сохранено как есть).

Ограничения: workflow должен работать на fork-PR без секретов, не требовать самописных runner-ов и не менять pom/команду сборки; ветка `main` не должна собираться дважды из разных workflow.

## Goals / Non-Goals

**Goals:**

- Один воспроизводимый pipeline для PR, `main` и ручного запуска с одинаковым набором проверок.
- Диагностируемость: отчёты тестов доступны даже при падении, WAR/JAR — при успехе.
- Минимальные права и предсказуемое время выполнения.

**Non-Goals:**

- Публикация в Maven Central, релизы по тегам, деплой, Docker-образы.
- Матрица JDK/OS, CodeQL/SAST, dependency review, Dependabot.
- Настройка branch protection: включение проверки как required — операция администратора репозитория, а не файла workflow (см. Migration Plan).

## Decisions

### D1. Один workflow `ci.yml` вместо отдельных PR/main workflow

`on: pull_request` (ветка `main`), `on: push` (ветка `main`), `on: workflow_dispatch`. Один job `build` с именем `CI / build` даёт стабильное имя проверки для branch protection и исключает расхождение шагов между PR и main. Альтернатива — `pr.yml` + `main.yml` — отвергнута: дублирование конфигурации и два разных статуса для одной и той же сборки.

### D2. JDK 17, Temurin, Maven wrapper

`actions/setup-java@v4` с `distribution: temurin`, `java-version: '17'`, `cache: maven`. JDK 17 — минимальная версия проекта (`maven.compiler.release=17`) и совпадает с локальной проверкой; матрица 17/21/25 отклонена пользователем как избыточная для текущего этапа. Сборка — только `./mvnw -B -ntp verify`: wrapper фиксирует Maven 3.9.9, `verify` включает compile, surefire (unit + IT), packaging. Флаги пропуска тестов не используются. Для детерминизма окружения задаётся `LANG: C.UTF-8` (JSP и кириллические ресурсы).

### D3. Два уровня кэша: Maven-репозиторий и wrapper

`setup-java` с `cache: maven` кэширует `~/.m2/repository` по хэшу `**/pom.xml` (все модули). Дополнительно `actions/cache@v4` кэширует `~/.m2/wrapper` по хэшу `.mvn/wrapper/maven-wrapper.properties`, потому что `setup-java` не покрывает дистрибутив wrapper-а. `restore-keys` не задаётся: промах кэша — штатная холодная сборка, а не ошибка. Кэш fork-PR доступен только на чтение, поэтому отравление кэша из fork исключено.

### D4. Отчёты — всегда, артефакты — только при успехе

Surefire-отчёты (`**/target/surefire-reports/**`) загружаются `actions/upload-artifact@v4` с `if: always()` и `if-no-files-found: warn`: при падении тестов отчёты — главный источник диагностики. WAR/JAR загружаются отдельным артефактом `build-artifacts` с `if: success()` и `if-no-files-found: error`: неуспешная сборка не должна выглядеть как поставка. Срок хранения — 7 дней, этого достаточно для разбора PR и не раздувает storage. Дополнительно шаг `if: always()` собирает сводку `tests/failures/errors/skipped` из `TEST-*.xml` в `$GITHUB_STEP_SUMMARY` (python3 есть на ubuntu-latest), чтобы итог был виден без скачивания артефакта.

Альтернатива — сторонний JUnit-reporter action — отвергнута: лишняя зависимость и права; нативная сводка + артефакт закрывают потребность.

### D5. Конкурентность и таймауты

`concurrency.group: ci-${{ github.workflow }}-${{ github.event.pull_request.number || github.ref }}`, `cancel-in-progress: true`: новый коммит в PR отменяет устаревший прогон, новый push в `main` — предыдущий. `timeout-minutes: 30` на job (холодная сборка с загрузкой зависимостей укладывается с большим запасом; локально прогретая — ~31 с). Это защищает от зависших embedded-Tomcat тестов, которые иначе держали бы runner.

### D6. Минимальные права, только официальные actions

`permissions: contents: read` на уровне workflow, секреты не используются, поэтому fork-PR работают без `pull_request_target` (и его рисков). Используются только `actions/checkout@v4`, `actions/setup-java@v4`, `actions/cache@v4`, `actions/upload-artifact@v4`; версии — major-теги, обновление — отдельной задачей сопровождения. Никаких `secrets.*`, `GITHUB_TOKEN` с write-правами и сторонних action-ов.

### D7. Бейдж в README

Бейдж `ci.yml` со ссылкой на `actions/workflows/ci.yml` и `branch=main`; URL репозитория — фактический `ys-kalyakin/spring-struts-brigde`. Отдельный workflow для бейджа не нужен.

## Risks / Trade-offs

- [Cold build скачивает зависимости и wrapper] → два кэша (D3); промах не ломает сборку, только замедляет.
- [Интеграционные тесты поднимают embedded Tomcat] → порт `0`, тесты изолированы; таймаут 30 мин и `cancel-in-progress` не дают зависшему прогону блокировать очередь.
- [Артефакты PR занимают storage] → retention 7 дней и только успешные сборки для WAR/JAR; отчёты тестов малы.
- [Major-теги actions могут быть перетегированы] → используем только официальные `actions/*`; при ужесточении требований безопасности — пиннинг по SHA отдельной задачей.
- [Branch protection не настраивается из workflow] → в Migration Plan зафиксирован ручной шаг администратора: сделать `CI / build` required для `main`.
- [Локальный remote содержит опечатку `brigde`] → бейдж и ссылки используют фактический URL; переименование репозитория — вне объёма.

## Migration Plan

1. Добавить `.github/workflows/ci.yml` и бейдж в README; прогнать YAML-валидацию и `./mvnw -B -ntp verify` локально.
2. Открыть тестовый PR: убедиться, что workflow стартует, отчёты и артефакты доступны, а падение теста (временный коммит) даёт failed и всё равно сохраняет surefire-отчёты; затем откатить временный коммит.
3. Администратор репозитория включает `CI / build` как required status check для `main` (branch protection/ruleset) — это единственный шаг вне репозитория.
4. Откат: удалить `.github/workflows/ci.yml` и бейдж; на код, сборку и артефакты это не влияет.
