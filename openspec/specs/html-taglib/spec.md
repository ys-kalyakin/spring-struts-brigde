# html-taglib Specification

## Purpose

Тэг-библиотека `struts-html` (URI `http://struts.apache.org/tags-html`): формы и виджеты ввода, вывод ошибок и сообщений, построение URL (img/link/rewrite) и xhtml-режим — с TLD, именами и структурой HTML-вывода эталона weblegacy.

## Requirements

### Requirement: Идентичность TLD, URI и имён тегов
TLD библиотеки SHALL быть идентична эталонной weblegacy (`struts-html.tld`, 30 тегов): тот же URI `http://struts.apache.org/tags-html`, те же имена тегов и атрибуты; декларация в JSP — `<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>`.

#### Scenario: Подключение и компиляция JSP
- **WHEN** JSP декларирует библиотеку по URI `http://struts.apache.org/tags-html` и использует `html:form` с виджетами
- **THEN** JSP компилируется и теги обрабатываются без дополнительных объявлений

### Requirement: Форма тегом html:form
`html:form` SHALL рендерить элемент `<form>`: с `action` — URL действия (по маппингу, с контекстом приложения, методом `post` по умолчанию и кодированием URL); без `action` — форму без атрибута action; атрибуты `focus`, `onsubmit` и произвольные динамические атрибуты переносятся в вывод; вместе с формой рендерятся скрытые поля токенов, если используются.

#### Scenario: Форма по действию
- **WHEN** JSP содержит `<html:form action="/InputSubmit.do">` с `<html:text property="name"/>`
- **THEN** выведен `<form name="..." action="/ctx/InputSubmit.do" method="post">` c `<input type="text" name="name" value="...">`, где value берётся из формы в области

#### Scenario: Форма без действия
- **WHEN** JSP содержит `<html:form>` без атрибута action (как exercise-страница html-form)
- **THEN** выводится `<form>` без атрибута action, тело обрабатывается

### Requirement: Виджеты ввода
Теги `text`, `password`, `hidden`, `textarea`, `checkbox`, `multibox`, `radio`, `select`, `option`, `options`, `optionsCollection`, `submit`, `reset`, `button`, `cancel`, `image`, `file`, `textarea` SHALL рендерить соответствующие элементы с именем из `property`, значением из формы в области (или из `value`), атрибутами событий и стилей из одноимённых атрибутов тега; `html:errors`-относящиеся атрибуты `errorKey` и пр. соблюдаются.

#### Scenario: Текстовое поле со значением формы
- **WHEN** форма в области содержит `name = "Ivan"`, и JSP содержит `<html:text property="name"/>`
- **THEN** выведено `<input type="text" name="name" value="Ivan">`

#### Scenario: Селект с опциями
- **WHEN** JSP содержит `<html:select property="color">` с `<html:option value="blue">Blue</html:option>` и форма содержит `color = "blue"`
- **THEN** выведен `select` с выбранной опцией `blue` (атрибут `selected` на соответствующем option)

#### Scenario: Чекбокс из свойства
- **WHEN** JSP содержит `<html:checkbox property="active"/>` и свойство формы `active = true`
- **THEN** выведен `<input type="checkbox" name="active" checked="checked">` (атрибут checked по значению)

### Requirement: Вывод ошибок и сообщений
`html:errors` SHALL рендерить ошибки из запроса/сессии (`ActionErrors` под ключом движка): каждый ошибочный элемент обёрнут `errors.prefix`/`errors.suffix`, список — `errors.header`/`errors.footer` (по умолчанию `<UL>`/`<LI>`/`</LI>`/`</UL>`), текст — из бандла по ключу сообщения с аргументами; `property` фильтрует по свойству. `html:messages` SHALL рендерить произвольные `ActionMessages` с заданными header/footer/prefix/suffix и переменной `id`.

#### Scenario: Рендер списка ошибок
- **WHEN** в запросе лежат две ошибки (ключи `errors.required` с аргументом и `errors.invalid`), и JSP содержит `<html:errors/>`
- **THEN** выведен `<UL>` с двумя `<LI>`, текст каждого — сообщение из бандла с подставленным аргументом

#### Scenario: Ошибки конкретного свойства
- **WHEN** JSP содержит `<html:errors property="name"/>`
- **THEN** выведены только ошибки свойства `name`

### Requirement: Построение URL: img, link, rewrite
`html:link` SHALL строить `<a href>` по `forward` (из глобальных форвардов), `action` или `href` с контекстом приложения, добавлением параметров (`paramId`/`paramName` или вложенные `html:param`) и кодированием; `html:rewrite` SHALL выдавать только URL (без элемента `<a>`); `html:img` SHALL рендерить `<img>` с URL по контексту и параметрами; `html:base` SHALL рендерить `<base href>` по текущему запросу; `html:html` SHALL рендерить `<html>` с атрибутом `lang` из локали.

#### Scenario: Ссылка по форварду
- **WHEN** глобальный форвард `welcome` ведёт на `/Welcome.do`, и JSP содержит `<html:link forward="welcome">Go</html:link>`
- **THEN** выведено `<a href="/ctx/Welcome.do">Go</a>` (с контекстом приложения)

#### Scenario: Ссылка с параметром
- **WHEN** JSP содержит `<html:link href="/list.do" paramId="id" paramName="bean" paramProperty="key">…</html:link>` и бин содержит `key = 7`
- **THEN** выведен `<a href="/ctx/list.do?id=7">…</a>`

### Requirement: Xhtml-режим
При включённом xhtml-режиме (атрибут движка/тег `html:xhtml`) теги библиотеки SHALL рендерить валидный XHTML: самозакрывающиеся элементы (`<input ... />`), атрибуты значений в виде `selected="selected"`/`checked="checked"`, корректный вывод `javascript`-содержимого; при выключенном — классический HTML-вывод.

#### Scenario: Самозакрывающиеся элементы в xhtml
- **WHEN** xhtml-режим включён, и JSP содержит `<html:text property="name"/>`
- **THEN** выведено `<input type="text" name="name" value="..."/>` (самозакрывающийся элемент)

### Requirement: Отложенный валидаторный тег
Тег `html:javascript` (генерация скриптов commons-validator) SHALL быть отложен до фазы валидации (фаза 3): TLD объявляет его, но реализация в этой фазе отсутствует.

#### Scenario: Использование тега javascript
- **WHEN** JSP использует `<html:javascript formName="..."/>`
- **THEN** обработка JSP завершается ошибкой, указывающей на нереализованный тег (валидатор — фаза 3)
