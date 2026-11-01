package org.apache.struts.util;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.nio.charset.StandardCharsets;

import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.LazyDynaBean;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import org.apache.struts.Globals;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.config.impl.ModuleConfigImpl;
import org.apache.struts.fixtures.MultipartForm;
import org.apache.struts.validator.LazyValidatorForm;
import org.apache.struts.validator.ValidatorForm;

/**
 * Populate-level fixtures of the reserved-property-name rule
 * (CVE-2006-1547, CVE-2016-1182): parameters named after framework-internal
 * form state are skipped before conversion, so they neither reach setters
 * nor leak into lazy forms, on regular and multipart requests alike. Before
 * the reserved-name rule these parameters failed populate with a conversion
 * error (typed properties) or added junk entries (lazy forms); the remaining
 * parameters must keep populating in both cases.
 */
class PopulateReservedNamesFixtureTest {

    @Test
    void reservedNameOnTypedPropertyDoesNotFailPopulate() {
        // 'servlet' is setServlet(ActionServlet): without the rule the
        // string value has no converter and populate fails with
        // "BeanUtils.populate"; with the rule it is skipped silently
        ValidatorForm form = new ValidatorForm();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("POST");
        request.setParameter("servlet", "not.an.ActionServlet");
        request.setParameter("page", "2");

        assertDoesNotThrow(() -> RequestUtils.populate(form, request));

        assertEquals(2, form.getPage(),
            "the remaining parameters still populate");
    }

    @Test
    void validatorInternalStateIsNotReachableFromRequest() {
        ValidatorForm form = new ValidatorForm();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("POST");
        request.setParameter("validatorResults", "x");
        request.setParameter("resultValueMap", "x");
        request.setParameter("multipartRequestHandler", "x");

        assertDoesNotThrow(() -> RequestUtils.populate(form, request));

        assertNull(form.getValidatorResults());
        assertNull(form.getResultValueMap());
        assertNull(form.getMultipartRequestHandler());
    }

    @Test
    void reservedNamesAddNothingToALazyForm() throws Exception {
        LazyValidatorForm form = new LazyValidatorForm();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("POST");
        request.setParameter("validatorResults", "x");
        request.setParameter("dynaClass", "x");
        request.setParameter("multipartRequestHandler", "x");
        request.setParameter("name", "Alice");

        RequestUtils.populate(form, request);

        assertEquals("Alice", form.get("name"),
            "undeclared non-reserved names are still added");
        assertFalse(hasProperty(form, "validatorResults"));
        assertFalse(hasProperty(form, "dynaClass"));
        assertFalse(hasProperty(form, "multipartRequestHandler"));
    }

    @Test
    void dynaClassIsNotAddedToADynaBean() {
        LazyDynaBean dyna = new LazyDynaBean();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("POST");
        request.setParameter("dynaClass", "x");
        request.setParameter("anything", "1");

        assertDoesNotThrow(() -> RequestUtils.populate(dyna, request));

        assertFalse(hasProperty(dyna, "dynaClass"));
        assertEquals("1", dyna.get("anything"));
    }

    @Test
    void multipartFieldWithReservedNameIsIgnored() throws Exception {
        MockHttpServletRequest request = rawMultipartRequest();
        request.setAttribute(Globals.MULTIPART_KEY,
            "org.apache.struts.upload.CommonsMultipartRequestHandler");

        MultipartForm form = new MultipartForm();
        RequestUtils.populate(form, request);

        assertEquals("hello", form.getTheText(),
            "the regular text fields still populate");
        assertNotNull(form.getTheFile());
        assertNotNull(form.getMultipartRequestHandler(),
            "the engine-assigned handler remains attached");
        assertEquals("f1.txt", form.getTheFile().getFileName());
    }

    private static final String BOUNDARY = "engine-boundary";

    private MockHttpServletRequest rawMultipartRequest() {
        StringBuilder body = new StringBuilder();
        body.append("--").append(BOUNDARY).append("\r\n")
            .append("Content-Disposition: form-data; "
                + "name=\"multipartRequestHandler\"\r\n")
            .append("\r\n")
            .append("junk\r\n")
            .append("--").append(BOUNDARY).append("\r\n")
            .append("Content-Disposition: form-data; name=\"theText\"\r\n")
            .append("\r\n")
            .append("hello\r\n")
            .append("--").append(BOUNDARY).append("\r\n")
            .append("Content-Disposition: form-data; name=\"theFile\"; "
                + "filename=\"f1.txt\"\r\n")
            .append("Content-Type: text/plain\r\n")
            .append("\r\n")
            .append("content1\r\n")
            .append("--").append(BOUNDARY).append("--\r\n");

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("POST");
        request.setCharacterEncoding(StandardCharsets.UTF_8.name());
        request.setContentType(
            "multipart/form-data; boundary=" + BOUNDARY);
        request.setContent(body.toString().getBytes(StandardCharsets.UTF_8));
        request.setAttribute(Globals.MAPPING_KEY, new ActionMapping());
        request.setAttribute(Globals.MODULE_KEY, new ModuleConfigImpl(""));
        return request;
    }

    private boolean hasProperty(DynaBean dyna, String name) {
        for (var property : dyna.getDynaClass().getDynaProperties()) {
            if (property.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }
}
