# Spec Delta: action-servlet-facade

## MODIFIED Requirements

### Requirement: Фасад ActionServlet
`ActionServlet` SHALL работать как фасад совместимости: конфигурация задаётся init-param `config` из web.xml (классический WAR-режим) либо свойствами стартера `spring.struts1.*` (Boot-режим), а экшены через `action.getServlet()` получают работающий servlet, через который доступны конфигурация модуля и init-params. В Boot-режиме init-params фасада SHALL соответствовать свойствам стартера с той же семантикой.

#### Scenario: Init-param config из web.xml
- **WHEN** web.xml объявляет init-param `config` со значением `/WEB-INF/struts-config.xml`
- **THEN** конфигурация загружается из этого файла и `getServlet().getInitParameter("config")` возвращает то же значение

#### Scenario: Свойства стартера в Boot-режиме
- **WHEN** Boot-приложение задаёт `spring.struts1.config=/WEB-INF/struts-config.xml`
- **THEN** конфигурация загружается из этого ресурса и `getServlet().getInitParameter("config")` возвращает то же значение

#### Scenario: Ссылка на servlet в экшене
- **WHEN** execute экшена вызывает `action.getServlet()`
- **THEN** возвращается работающий ActionServlet текущего приложения
