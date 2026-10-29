# Proposal

## Why

Сейчас качество сборки подтверждается только локальным `./mvnw verify`: в репозитории нет ни одной проверки GitHub Actions, поэтому регрессии в PR и в `main` обнаруживаются вручную, а собранные WAR/JAR нигде не сохраняются. Проект уже прошёл фазы 1–3 и содержит 205 тестов, включая интеграционные прогоны на embedded Tomcat, — им нужен воспроизводимый gate на каждый PR и на каждое изменение основной ветки.

## What Changes

- **Новый workflow `.github/workflows/ci.yml`** для GitHub Actions:
  - триггеры: `pull_request` в `main`, `push` в `main`, ручной `workflow_dispatch`;
  - job `build` на `ubuntu-latest`: checkout, Temurin JDK 17 (минимум проекта, `maven.compiler.release=17`), кэш Maven-репозитория и wrapper, `./mvnw -B -ntp verify` (компиляция, unit- и интеграционные тесты, упаковка WAR/JAR);
  - `concurrency` с отменой устаревших прогонов для PR, `permissions: contents: read`, `timeout-minutes`, отключение интерактивного вывода Maven.
- **Публикация результатов**: отчёты surefire (`**/target/surefire-reports/**`) загружаются артефактом всегда, даже при падении тестов; собранные WAR/JAR — артефактом успешной сборки. Это делает падение диагностируемым без повторного локального прогона.
- **Кэширование** зависимостей Maven и дистрибутива Maven wrapper с ключом по `pom.xml`/`maven-wrapper.properties`, чтобы холодная сборка не скачивала зависимости заново.
- **Бейдж статуса CI** в `README.md` со ссылкой на workflow.
- Ветка `main` используется как основная (она же default branch в `origin`); отдельного workflow для релизов, публикации в Maven Central и деплоя нет — это вне объёма изменения.

## Capabilities

### New Capabilities

- `ci-pipeline`: поведение GitHub Actions CI — обязательная проверка PR и `main`, состав сборки, кэширование, конкурентность, сохранение отчётов и артефактов, ручной запуск.

### Modified Capabilities

Нет: изменение не меняет поведение фреймворка, его API или тестовых приложений.

## Impact

- **Файлы:** новый `.github/workflows/ci.yml`; правка `README.md` (бейдж); больше ничего в коде и сборке не меняется.
- **Инфраструктура:** GitHub-hosted runner `ubuntu-latest`, официальные actions `actions/checkout`, `actions/setup-java`, `actions/cache`, `actions/upload-artifact`; секреты не требуются, fork-PR работают.
- **Сборка:** остаётся `./mvnw verify` без изменения pom; CI использует те же команды, что и локальная проверка.
- **Не входит в объём:** релизы и публикация артефактов по тегам, деплой, CodeQL/SAST, dependency review, матрица JDK/OS, self-hosted runners, Docker-образы.
