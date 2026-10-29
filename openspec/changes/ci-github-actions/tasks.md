# Tasks

## 1. Каркас workflow и триггеры

- [x] 1.1 Создать `.github/workflows/ci.yml`: имя `CI`, триггеры `pull_request` в `main`, `push` в `main`, `workflow_dispatch`; `permissions: contents: read`, `concurrency` с группой по PR/ref и `cancel-in-progress: true`, `timeout-minutes: 30`, job `build` на `ubuntu-latest` с checkout. Verify: YAML парсится (`python3 -c "import yaml; yaml.safe_load(open('.github/workflows/ci.yml'))"` или `ruby -e 'require "yaml"; YAML.load_file(...)'`), в файле присутствуют все три триггера и ограничения, `git diff --check` чист.
- [x] 1.2 Добавить шаги JDK и сборки: `actions/setup-java@v4` (`temurin`, `17`, `cache: maven`), кэш `~/.m2/wrapper` по хэшу `.mvn/wrapper/maven-wrapper.properties`, `LANG: C.UTF-8` и `./mvnw -B -ntp verify` без флагов пропуска тестов. Verify: локальный `./mvnw -B -ntp verify` на JDK 17 завершается `BUILD SUCCESS`; в workflow нет `-DskipTests`/`-Dmaven.test.skip`, версия Java и кэш-ключи соответствуют design D2–D3.

## 2. Диагностика и артефакты

- [x] 2.1 Добавить загрузку surefire-отчётов `actions/upload-artifact@v4` с `if: always()`, `if-no-files-found: warn`, retention 7 дней и шаг сводки `tests/failures/errors/skipped` из `TEST-*.xml` в `$GITHUB_STEP_SUMMARY` с `if: always()`. Verify: локальный прогон `verify` создаёт отчёты; скрипт сводки на них печатает `205` тестов и не падает на пустом каталоге отчётов.
- [x] 2.2 Добавить загрузку `build-artifacts` с `if: success()`, `if-no-files-found: error`: JAR `spring-struts1-core`, `spring-struts1-taglib`, `spring-struts1-tiles`, `spring-struts1-apps/mailreader-dao` и WAR `blank`, `examples`, `cookbook`, `mailreader`. Verify: после локального `verify` все восемь путей существуют (`find … -name '*.war' -o -name '*.jar'`), пути в workflow не содержат лишних `*-sources.jar`.

## 3. README и статус

- [x] 3.1 Добавить в `README.md` бейдж `ci.yml` со ссылкой на `https://github.com/ys-kalyakin/spring-struts-brigde/actions/workflows/ci.yml` и `branch=main`. Verify: markdown-ссылка и URL бейджа указывают на существующий workflow и ветку `main`, бейдж отображается в предпросмотре README.

## 4. Интеграционная проверка CI

- [ ] 4.1 Проверить успешный путь на GitHub: запушить ветку с изменением, открыть PR в `main`; убедиться, что workflow стартует на `pull_request`, job `CI / build` появляется в проверках, а после успеха доступны `surefire-reports` и `build-artifacts` (4 WAR + 4 JAR). Verify: статус проверки зелёный, артефакты скачиваются, в summary видны totals тестов.
- [x] 4.2 Проверить путь падения: во временной ветке добавить заведомо падающий тест, запушить и убедиться, что проверка становится failed, `surefire-reports` всё равно загружается, а `build-artifacts` не публикуется; затем откатить тестовый коммит. Verify: failed-прогон с отчётами в артефактах и отсутствием `build-artifacts`.
- [ ] 4.3 Проверить ручной запуск и отмену: запустить `workflow_dispatch` для ветки, затем отправить второй коммит в активный PR и убедиться, что предыдущий прогон отменён. Verify: ручной прогон завершается успешно, в PR остаётся только прогон последнего коммита.
- [x] 4.4 Финальная проверка: `openspec validate ci-github-actions --strict` проходит, `./mvnw -B -ntp verify` локально зелёный, в `git status` нет временных файлов сборки кроме игнорируемого `target/`. Verify: обе команды завершаются с кодом 0, рабочее дерево содержит только `.github/workflows/ci.yml`, `README.md` и артефакты OpenSpec.

## 5. Исправление, выявленное первым прогоном CI

- [x] 5.1 Сделать `TilesIntegrationTest.localizedDefinitionsFileIsSelectedByLocale` независимым от локали JVM: базовый вариант запрашивать с явным `Accept-Language: ru`, потому что на GitHub-раннере JVM по умолчанию `en` и выбирала `tiles-defs_en`. Verify: `LANG=C.UTF-8 LC_ALL=C.UTF-8 ./mvnw -B -ntp verify` завершается `BUILD SUCCESS`, полный прогон на GitHub — зелёный.

## Workflow follow-up

- Администратор репозитория включает `CI / build` как required status check для `main` (branch protection/ruleset) — вне файлов репозитория.
- Archive the change after the project's review requirements are satisfied.
- Verify the archived result.
