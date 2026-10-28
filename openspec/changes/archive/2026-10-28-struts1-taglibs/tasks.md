# Tasks

## 1. Модуль taglib и утилиты ядра

- [x] 1.1 Создать модуль `spring-struts1-taglib` (зависимость: core, jakarta.servlet.jsp-api provided, beanutils) с TLD, скопированными байт-в-байт из эталона (`META-INF/tld/struts-{bean,html,logic,nested}.tld`); проверить, что `./mvnw -q -pl spring-struts1-taglib -am validate` проходит
- [x] 1.2 Портировать в core `org.apache.struts.util.ResponseUtils` (filter/render/write) и `ModuleUtils` (карта модулей, selectModule по пути запроса) из эталона; проверить юнит-тестом filter (экранирование) и выбора модуля по пути
- [x] 1.3 Портировать `org.apache.struts.taglib.TagUtils` из эталона с адаптацией к нашему ядру (MessageResources/ModuleConfig из запроса); проверить юнит-тестом построения URL с контекстом и фильтрации значений

## 2. Библиотека struts-bean

- [x] 2.1 Портировать 11 тегов `org.apache.struts.taglib.bean` (write, message, define, size, header, cookie, parameter, include, resource, struts, page?) файл-в-файл из эталона; проверить компиляцию модуля
- [x] 2.2 Написать IT на embedded Tomcat 11 (собственная test-страница в тестовом webapp): bean:write значения и filter=true/false, bean:message по ключу и с arg0..arg3 и bundle, bean:define + bean:write переменной, bean:parameter/header/cookie, bean:size, bean:include; проверить, что тесты зелёные и соответствуют сценариям спеки `bean-taglib`

## 3. Библиотека struts-html

- [x] 3.1 Портировать 29 тегов `org.apache.struts.taglib.html` (form, text, password, hidden, textarea, checkbox, multibox, radio, select, option, options, optionsCollection, submit, reset, button, cancel, image, file, errors, messages, img, link, rewrite, base, html, xhtml, javascript-заглушка отсутствует — тег не портируется); проверить компиляцию модуля
- [x] 3.2 Написать IT: html:form с action и без (как exercise html-form), виджеты text/select/checkbox/multibox с значениями из формы, html:errors (header/prefix/suffix по умолчанию и фильтр по property), html:messages, html:link по forward и с paramId/paramName, html:rewrite, html:img, html:base; проверить соответствие сценариям спеки `html-taglib`
- [x] 3.3 Реализовать xhtml-режим (флаг, самозакрывающиеся элементы, `checked="checked"`/`selected="selected"`) и IT: вывод с включённым и выключенным режимом; проверить сценарий спеки `html-taglib` (xhtml)
- [x] 3.4 Зафиксировать IT: JSP с `html:javascript` даёт ошибку обработки (тег отложен до фазы 3); проверить сценарий спеки

## 4. Библиотека struts-logic

- [x] 4.1 Портировать 17 тегов `org.apache.struts.taglib.logic` (iterate, equal/notEqual, greaterEqual/lessEqual/greaterThan/lessThan, match/notMatch, present/notPresent, empty/notEmpty, messagesPresent/messagesNotPresent, redirect, forward); проверить компиляцию модуля
- [x] 4.2 Написать IT: iterate (коллекция, indexId, offset/length), сравнения по параметру и свойству, match с location, present/notPresent (в т.ч. атрибут приложения — как в Welcome.jsp эталона), empty, messagesPresent, redirect по forward (302) и forward; проверить соответствие сценариям спеки `logic-taglib`

## 5. Библиотека struts-nested

- [x] 5.1 Портировать 40 тегов `org.apache.struts.taglib.nested` (включая NestedPropertyHelper/NestedReferenceHelper) файл-в-файл; проверить компиляцию модуля
- [x] 5.2 Написать IT на собственных страницах: nested:form + nested:text с полным путём свойства, nested:iterate + nested:write по элементам списка, nested:root/nested:reference, эквивалентность вывода базовым тегам; проверить соответствие сценариям спеки `nested-taglib`

## 6. Мульти-модульность и path-mapped

- [x] 6.1 Расширить `ActionServlet.initModuleConfig`: разбор init-params `config/<prefix>` в карту модулей (ошибка старта с именем файла при битом/отсутствующем файле модуля); проверить юнит-тестами (модуль по `config/exercise`, ошибка с именем файла)
- [x] 6.2 Внедрить выбор модуля в `StrutsHandlerMapping` (по префиксу пути запроса, иначе модуль по умолчанию) и передавать модуль в конвейер/адаптер (атрибут запроса); проверить IT: запрос `/exercise/…` обслуживается маппингами модуля `/exercise`, запрос без префикса — модулем по умолчанию
- [x] 6.3 Добавить в `StrutsHandlerMapping` режим `servletMapping` (`*.do` / `/action/*`): path-mapped маршрутизация с отбрасыванием префикса; проверить IT: запрос `/action/Welcome` обслуживается маппингом `/Welcome`

## 7. DynaActionForm и dispatch-экшены

- [x] 7.1 Портировать минимальный `DynaActionForm` (DynaBean поверх HashMap, свойство из `<form-property>`, initial-значения) и ветку создания в `FormBeanConfig.createActionForm`; проверить юнит-тестами: свойства по конфигурации, initial, свежий экземпляр на запрос
- [x] 7.2 Проверить IT: populate динамической формы параметрами запроса и валидация по флагу маппинга на динамической форме; проверить сценарии спеки `dynamic-form-beans`
- [x] 7.3 Портировать subset extras в core (`org.apache.struts.extras.actions`: DispatchAction, LookupDispatchAction, MappingDispatchAction, EventDispatchAction, ActionDispatcher, EventActionDispatcher, ForwardAction, IncludeAction); проверить юнит/IT: dispatch по параметру, LookupDispatch по кнопке из бандла, MappingDispatch по parameter маппинга, EventDispatch по событию, ActionDispatcher без наследования, unspecified при отсутствии параметра; проверить сценарии спеки `dispatch-actions`

## 8. Портирование apps/examples и blank

- [x] 8.1 Создать модуль `spring-struts1-apps/examples` (war): web.xml с модулями config, config/exercise, config/dispatch (без upload/validator), struts-config-файлы и исходники приложения из эталона без правок, JSP exercise/dispatch — из эталона; проверить, что war собирается `./mvnw -pl spring-struts1-apps/examples -am package`
- [x] 8.2 Переключить `blank` на оригинальные JSP из эталона (html:html/bean:message/logic:notPresent) вместо адаптации фазы 1; проверить, что IT blank зелёные на оригинальных страницах
- [x] 8.3 Написать IT прогона examples на embedded Tomcat 11: все exercise-страницы bean-*/html-*/logic-* отвечают 200 с ключевыми маркерами структуры HTML (атрибуты, вложенность), сценарии dispatch (actionDispatcher, eventAction, lookup, mapping) выполняются POST-запросами с выбором метода; проверить, что тесты зелёные. Примечание: html-link.jsp и bean-include.jsp ссылаются на исключённые в 8.1 модули /upload и /validator, поэтому в IT исключены с комментарием (поведение их тегов покрыто IT модуля taglib)

## 9. Сквозная проверка фазы 2

- [x] 9.1 Полный `./mvnw verify` зелёный; пройтись по критериям приёмки фазы 2 в ROADMAP.md (TLD/URI идентичны, examples рендерится и проходит exercise-taglib и dispatch, структура HTML совместима) и убедиться, что каждый покрыт тестом; проверить итоговый отчёт прогона

## Workflow follow-up

- Синхронизировать спеки и заархивировать change (`/opsx:sync`, `/opsx:archive`) после ревью.
- Проверить заархивированный результат (`openspec list`).
