# bean-taglib Specification

## Purpose

Тэг-библиотека `struts-bean` (URI `http://struts.apache.org/tags-bean`): вывод значений бинов и сообщений, определение переменных и доступ к данным запроса и окружения — с TLD, именами и поведением эталона weblegacy.

## Requirements

### Requirement: Идентичность TLD, URI и имён тегов
TLD библиотеки SHALL быть идентична эталонной weblegacy (`struts-bean.tld`, 11 тегов): тот же URI `http://struts.apache.org/tags-bean`, те же имена тегов и атрибуты. JSP SHALL подключать библиотеку декларацией `<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>` без правок web.xml.

#### Scenario: Подключение и компиляция JSP
- **WHEN** JSP декларирует библиотеку по URI `http://struts.apache.org/tags-bean` и использует `bean:write`
- **THEN** JSP компилируется и тег обрабатывается без дополнительных объявлений

### Requirement: Вывод значений тегом bean:write
`bean:write` SHALL выводить значение бина из области по `name`/`property` (с учётом `scope`); HTML-разметка в значении SHALL экранироваться по умолчанию и выводиться как есть при `filter="false"`; `format`/`formatKey` применяют форматирование.

#### Scenario: Значение свойства бина
- **WHEN** в запросе лежит бин со свойством `name = "Ivan"`, и JSP содержит `<bean:write name="bean" property="name"/>`
- **THEN** на страницу выведено `Ivan`

#### Scenario: Экранирование по умолчанию
- **WHEN** значение свойства содержит `<b>`, и JSP содержит `<bean:write name="bean" property="html"/>`
- **THEN** на странице выведен экранированный текст `&lt;b&gt;`, а при `filter="false"` — исходная разметка

### Requirement: Сообщения тегом bean:message
`bean:message` SHALL выводить сообщение по `key` (или по свойству, заданному `name`/`property`) из бандла — по умолчанию или указанного атрибутом `bundle`, с подстановкой `arg0..arg3` и локалью по семантике движка.

#### Scenario: Сообщение по ключу
- **WHEN** бандл содержит `welcome.heading=Welcome!`, и JSP содержит `<bean:message key="welcome.heading"/>`
- **THEN** на странице выведено `Welcome!`

#### Scenario: Сообщение с аргументами
- **WHEN** JSP содержит `<bean:message key="greeting" arg0="Ivan"/>` и шаблон `greeting=Hello, {0}!`
- **THEN** на странице выведено `Hello, Ivan!`

### Requirement: Определение переменных тегом bean:define
`bean:define` SHALL копировать значение (по `name`/`property`/`value`) в переменную JSP и атрибут области: под указанным `id`, в области `toScope`, с типом значения исходного свойства.

#### Scenario: Определение и вывод переменной
- **WHEN** JSP содержит `<bean:define id="theName" name="bean" property="name"/>` и затем `<bean:write name="theName"/>`
- **THEN** переменная определена и на странице выведено её значение

### Requirement: Доступ к данным запроса и окружения
Теги `bean:header`, `bean:cookie`, `bean:parameter` SHALL определять переменную из заголовка запроса, cookie или параметра запроса (с `value` по умолчанию и поддержкой `multiple`); `bean:size` SHALL выводить размер коллекции/массива/карты; `bean:include` SHALL включать содержимое ресурса в переменную; `bean:resource` SHALL загружать содержимое ресурса веб-приложения; `bean:struts` SHALL предоставлять доступ к элементам конфигурации (form-bean, forward, mapping).

#### Scenario: Параметр запроса и его размер
- **WHEN** запрос содержит параметр `color=blue`, и JSP содержит `<bean:parameter id="color" name="color"/>` и `<bean:write name="color"/>`
- **THEN** на странице выведено `blue`

#### Scenario: Заголовок запроса
- **WHEN** JSP содержит `<bean:header id="lang" name="Accept-Language"/>` и `<bean:write name="lang"/>`
- **THEN** на странице выведено значение заголовка запроса

#### Scenario: Включение ресурса
- **WHEN** JSP содержит `<bean:include id="page" href="/some/page.jsp"/>` и `<bean:write name="page" filter="false"/>`
- **THEN** переменная содержит отрендеренное содержимое включённой страницы
