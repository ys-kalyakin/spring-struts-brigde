# Spec Delta: html-taglib

## MODIFIED Requirements

### Requirement: Отложенный валидаторный тег
Тег `html:javascript` реализуется в фазе валидации: он SHALL генерировать клиентские скрипты commons-validator по правилам validation.xml (поведение определено в спеке `struts-validator`); TLD-объявление тега остаётся без изменений.

#### Scenario: Использование тега javascript
- **WHEN** JSP использует `<html:javascript formName="..."/>`, и validation.xml объявляет правила этой формы
- **THEN** страница содержит сгенерированный JavaScript валидации формы, а не ошибку обработки
