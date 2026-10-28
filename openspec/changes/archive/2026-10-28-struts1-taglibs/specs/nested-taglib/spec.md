# Spec Delta

## Purpose

Тэг-библиотека `struts-nested` (URI `http://struts.apache.org/tags-nested`): вложенные обёртки над bean/html/logic, ведущие относительный путь свойств по уровню вложенности — с TLD, именами и поведением эталона weblegacy.

## ADDED Requirements

### Requirement: Идентичность TLD, URI и имён тегов
TLD библиотеки SHALL быть идентична эталонной weblegacy (`struts-nested.tld`, 40 тегов): тот же URI `http://struts.apache.org/tags-nested`, те же имена тегов и атрибуты; декларация в JSP — `<%@ taglib uri="http://struts.apache.org/tags-nested" prefix="nested" %>`.

#### Scenario: Подключение и компиляция JSP
- **WHEN** JSP декларирует библиотеку по URI `http://struts.apache.org/tags-nested` и использует `nested:form` с вложенными тегами
- **THEN** JSP компилируется и теги обрабатываются без дополнительных объявлений

### Requirement: Относительные пути вложенности
Каждый вложенный тег с атрибутом `property` SHALL строить полное имя свойства как конкатенацию путей всех открытых вложенных уровней (`nested:nest`, `nested:form`, `nested:iterate`) и собственного `property`: виджеты рендерятся с полным именем свойства, значения берутся из соответствующего объекта цепочки.

#### Scenario: Свойство внутри итерации
- **WHEN** бин в области содержит список элементов со свойством `name`, и JSP содержит `nested:iterate property="items"` с `nested:write property="name"` внутри
- **THEN** каждое значение выведено из свойства `name` текущего элемента списка

#### Scenario: Виджет с полным путём
- **WHEN** JSP содержит `nested:form action="/Edit.do"` c `nested:text property="person.name"`
- **THEN** выведено `<input type="text" name="person.name" value="...">` со значением из свойства `person.name` формы

### Requirement: Эквивалентность рендеринга базовым тегам
Вложенные аналоги тегов (`nested:write`, `nested:define`, `nested:text`, `nested:select`, `nested:iterate`, `nested:present` и остальные) SHALL рендерить и вести себя так же, как одноимённые теги bean/html/logic, отличаясь только разрешением относительных путей; `nested:root` и `nested:reference` SHALL позволять ссылаться на корневой бин и текущий уровень вложенности.

#### Scenario: Ссылка на корневой бин
- **WHEN** внутри вложенности JSP содержит `nested:write property="rootProperty"` на `nested:root` с `name="bean"`
- **THEN** значение взято из свойства корневого бина, а не из цепочки вложенности
