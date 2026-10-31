# Tag libraries

The Struts 1 tag libraries work on the Spring engine with the same URIs and TLDs — JSPs keep their `taglib` declarations unchanged. Each TLD is shipped in its artifact at `META-INF/tld/` under the classic URI.

| Library | URI | Artifact |
|---|---|---|
| struts-bean | `http://struts.apache.org/tags-bean` | `spring-struts1-taglib` |
| struts-html | `http://struts.apache.org/tags-html` | `spring-struts1-taglib` |
| struts-logic | `http://struts.apache.org/tags-logic` | `spring-struts1-taglib` |
| struts-nested | `http://struts.apache.org/tags-nested` | `spring-struts1-taglib` |
| struts-tiles | `http://struts.apache.org/tags-tiles` | `spring-struts1-tiles` |

A classic JSP declaration keeps working as is:

```jsp
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
```

## Validation

`spring-struts1-taglib` also bundles [commons-validator](https://commons.apache.org/proper/commons-validator/) with the classic `ValidatorPlugIn`: the `<plug-in className="org.apache.struts.validator.ValidatorPlugIn">` declaration in `struts-config.xml`, the validation resources, and the validator tags behave as in the classic stack.

## Which libraries do my pages need?

Add `spring-struts1-taglib` if your JSPs use bean/html/logic/nested tags, and `spring-struts1-tiles` if they use Tiles. The [modules](modules.md) page shows what each artifact carries; the [configuration](configuration.md) page covers wiring the engine itself.
