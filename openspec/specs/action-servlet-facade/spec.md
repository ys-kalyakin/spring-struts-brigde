# action-servlet-facade Specification

## Purpose

Сохраняет совместимость с legacy-кодом Struts 1: работающий фасад `ActionServlet` с init-params и жизненный цикл `PlugIn` (init при старте, destroy при остановке).

## Requirements

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

### Requirement: Жизненный цикл PlugIn
Плагины из конфигурации SHALL инициализироваться при старте приложения (в порядке объявления) и останавливаться при его остановке; плагин, зарегистрированный как Spring-бин, SHALL создаваться из контекста, остальные — прямым созданием.

#### Scenario: Init и destroy плагина
- **WHEN** приложение с объявленным плагином стартует и затем останавливается
- **THEN** init плагина вызван при старте и destroy — при остановке

#### Scenario: Плагин как Spring-бин
- **WHEN** класс плагина зарегистрирован в Spring как бин
- **THEN** плагин создаётся из контекста и его зависимости внедрены
