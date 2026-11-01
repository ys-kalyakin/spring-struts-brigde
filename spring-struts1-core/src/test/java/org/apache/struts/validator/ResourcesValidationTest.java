package org.apache.struts.validator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Iterator;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockServletConfig;
import org.springframework.mock.web.MockServletContext;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionServlet;
import org.apache.struts.config.FormBeanConfig;
import org.apache.struts.config.FormPropertyConfig;
import org.apache.struts.config.impl.ModuleConfigImpl;
import org.apache.struts.util.RequestUtils;

/**
 * Flow tests of the validator: the shipped validator-rules.xml plus a
 * fixture validation.xml drive validation through
 * <code>Resources.initValidator</code> and the form classes (required,
 * mask, minlength, intRange, validwhen, page filtering, DynaValidatorForm).
 */
class ResourcesValidationTest {

    private static final String PATHNAMES =
        "/org/apache/struts/validator/validator-rules.xml,"
            + "/org/apache/struts/validator/validation-test.xml";

    static MockServletContext servletContext;

    static ActionServlet servlet;

    @BeforeAll
    static void initServletAndPlugin() throws Exception {
        servletContext = new MockServletContext();
        servletContext = new MockServletContext() {
            @Override
            public java.net.URL getResource(String path)
                throws java.net.MalformedURLException {
                return "/WEB-INF/struts-config.xml".equals(path)
                    ? ResourcesValidationTest.class.getResource(
                        "/org/apache/struts/config/struts-config-a.xml")
                    : super.getResource(path);
            }
        };
        servlet = new ActionServlet();
        MockServletConfig config = new MockServletConfig(servletContext,
            "action");
        config.addInitParameter("config", "/WEB-INF/struts-config.xml");
        servlet.init(config);

        ValidatorPlugIn plugin = new ValidatorPlugIn();
        plugin.setPathnames(PATHNAMES);
        plugin.setStopOnFirstError(false);
        plugin.init(servlet, new ModuleConfigImpl(""));
    }

    private ActionMapping mapping(String formName, Integer acceptPage) {
        ActionMapping mapping = new ActionMapping();
        mapping.setAttribute(formName);
        mapping.setAcceptPage(acceptPage);
        return mapping;
    }

    private ActionErrors validate(ValidatorFixtureForm form,
        ActionMapping mapping) {
        form.setServlet(servlet);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addPreferredLocale(java.util.Locale.ENGLISH);
        return form.validate(mapping, request);
    }

    private static ActionMessage first(ActionErrors errors) {
        Iterator<ActionMessage> it = errors.get();
        return it.next();
    }

    private static boolean hasKey(ActionErrors errors, String key) {
        Iterator<ActionMessage> it = errors.get();
        while (it.hasNext()) {
            if (key.equals(it.next().getKey())) {
                return true;
            }
        }
        return false;
    }

    @Test
    void requiredRuleReportsErrorsRequiredWithFieldLabel() {
        ValidatorFixtureForm form = new ValidatorFixtureForm();
        form.setFirstName("");
        form.setZip("12345");

        ActionErrors errors = validate(form, mapping("testBean", null));

        assertTrue(errors.size() > 0);
        assertTrue(hasKey(errors, "errors.required"));
        // args are resolved through message resources like the reference:
        // without a bundle the classic ???locale.key??? format appears
        assertTrue(String.valueOf(first(errors).getValues()[0])
            .contains("prompt.firstName"));
    }

    @Test
    void maskAndMinlengthRulesApply() {
        ValidatorFixtureForm form = new ValidatorFixtureForm();
        form.setFirstName("Ivan123");
        form.setLastName("ab");

        ActionErrors errors = validate(form, mapping("testBean", null));

        assertTrue(hasKey(errors, "errors.invalid"),
            "mask violation must yield errors.invalid");
        assertTrue(hasKey(errors, "errors.minlength"),
            "minlength violation must yield errors.minlength");
    }

    @Test
    void intRangeRuleApplies() {
        ValidatorFixtureForm form = new ValidatorFixtureForm();
        form.setFirstName("Ivan");
        form.setZip("42");

        ActionErrors errors = validate(form, mapping("testBean", null));

        assertTrue(hasKey(errors, "errors.range"));
        // 10000 = min, 99999 = max substituted from the rule vars
        boolean found = false;
        Iterator<ActionMessage> it = errors.get();
        while (it.hasNext()) {
            ActionMessage message = it.next();
            if ("errors.range".equals(message.getKey())) {
                found = "10000".equals(message.getValues()[1])
                    && "99999".equals(message.getValues()[2]);
            }
        }
        // var substitution happens in commons-validator; the numbers reach
        // the message args even though they are treated as resource keys
        found = false;
        it = errors.get();
        while (it.hasNext()) {
            ActionMessage message = it.next();
            if ("errors.range".equals(message.getKey())) {
                found = String.valueOf(message.getValues()[1]).contains("10000")
                    && String.valueOf(message.getValues()[2]).contains("99999");
            }
        }
        assertTrue(found, "range args must carry min/max values");
    }

    @Test
    void validFieldsPassValidation() {
        ValidatorFixtureForm form = new ValidatorFixtureForm();
        form.setFirstName("Ivan");
        form.setLastName("Petrov");
        form.setZip("12345");
        form.setConfirm("clone");
        form.setMiddleInitial("I");

        ActionErrors errors = validate(form, mapping("testBean", null));
        assertEquals(0, errors.size(), "all valid fields must pass");
    }

    @Test
    void validwhenRuleApplies() {
        ValidatorFixtureForm form = new ValidatorFixtureForm();
        form.setFirstName(null); // expression requires *this* != null then
        form.setZip("12345");

        ActionErrors errors = validate(form, mapping("testBean", null));

        assertTrue(hasKey(errors, "errors.required"),
            "validwhen violation must be reported");
    }

    @Test
    void pageFilteringWithAcceptPage() {
        ValidatorFixtureForm form = new ValidatorFixtureForm();
        form.setFirstName("Ivan");
        form.setLastName("Petrov");
        form.setZip("12345");
        form.setConfirm("clone");
        // middleInitial is page="2"; form page 1 with acceptPage 1 must skip it
        ActionMapping mapping = mapping("testBean", 1);
        ActionErrors errors = validate(form, mapping);
        assertEquals(0, errors.size(), "page 2 field must be skipped");

        // without an acceptPage all fields are validated
        ValidatorFixtureForm full = new ValidatorFixtureForm();
        full.setFirstName("Ivan");
        full.setLastName("Petrov");
        full.setZip("12345");
        full.setConfirm("clone");
        ActionErrors all = validate(full, mapping("testBean", null));
        assertTrue(all.size() > 0, "page 2 field must be validated");
    }

    @Test
    void requestSuppliedPageCannotNarrowTheValidationScope() throws Exception {
        // middleInitial is page="2"; the mapping intends to validate page 2
        // (acceptPage=2). A request-supplied page=1 must not shrink the
        // validated scope below the mapping's acceptPage: the page-2 field
        // keeps being validated (CVE-2016-1182, determinePage clamp).
        ValidatorFixtureForm form = new ValidatorFixtureForm();
        form.setFirstName("Ivan");
        form.setLastName("Petrov");
        form.setZip("12345");
        form.setConfirm("clone");

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("POST");
        request.setParameter("page", "1");
        RequestUtils.populate(form, request);
        assertEquals(1, form.getPage(), "page stays populate-able");

        ActionErrors errors = validate(form, mapping("testBean", 2));
        assertTrue(errors.size() > 0,
            "the page-2 field is still validated: the request-supplied "
                + "page cannot narrow the scope");

        // a request-supplied page above acceptPage may only widen the
        // validated scope (classic behavior): every field is validated
        ValidatorFixtureForm widened = new ValidatorFixtureForm();
        widened.setFirstName("Ivan");
        widened.setLastName("Petrov");
        widened.setZip("12345");
        widened.setConfirm("clone");
        MockHttpServletRequest highPage = new MockHttpServletRequest();
        highPage.setMethod("POST");
        highPage.setParameter("page", "99");
        RequestUtils.populate(widened, highPage);

        ActionErrors widenedErrors = validate(widened, mapping("testBean", 1));
        assertTrue(widenedErrors.size() > 0,
            "a high page widens validation to all fields");
    }

    @Test
    void dynaValidatorFormValidatesByConfiguredName() throws Exception {
        ModuleConfigImpl module = new ModuleConfigImpl("");
        FormBeanConfig config = new FormBeanConfig();
        config.setName("dynaTestBean");
        config.setType("org.apache.struts.validator.DynaValidatorForm");
        config.addFormPropertyConfig(
            new FormPropertyConfig("firstName", "java.lang.String", null));
        config.addFormPropertyConfig(
            new FormPropertyConfig("zip", "java.lang.String", null));
        module.addFormBeanConfig(config);

        org.apache.struts.action.DynaActionForm form =
            (org.apache.struts.action.DynaActionForm) config.createActionForm();
        assertTrue(form instanceof DynaValidatorForm);
        form.set("firstName", "Ivan");
        ((org.apache.struts.action.ActionForm) form).setServlet(servlet);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addPreferredLocale(java.util.Locale.ENGLISH);

        // a key without rules validates nothing
        ActionErrors none = ((DynaValidatorForm) form)
            .validate(mapping("dynaTestBean", null), request);
        assertEquals(0, none.size());

        // the dyna form validates by the mapping attribute like a static form
        ActionErrors errors = ((DynaValidatorForm) form)
            .validate(mapping("testBean", null), request);
        assertTrue(errors.size() > 0, "dyna form must be validated by rules");
    }

    @Test
    void validatorActionFormUsesPathAsKey() {
        org.apache.struts.extras.validator.ValidatorActionForm form =
            new org.apache.struts.extras.validator.ValidatorActionForm();
        ActionMapping mapping = new ActionMapping();
        mapping.setAttribute("ignored");
        mapping.setPath("/save-registration");
        assertEquals("/save-registration",
            form.getValidationKey(mapping, null));
    }

    @Test
    void numericValidwhenComparesAsNumbers() throws Exception {
        ModuleConfigImpl module = new ModuleConfigImpl("");
        FormBeanConfig config = new FormBeanConfig();
        config.setName("numericWhenForm");
        config.setType("org.apache.struts.validator.DynaValidatorForm");
        config.addFormPropertyConfig(
            new FormPropertyConfig("max", "java.lang.String", "9"));
        config.addFormPropertyConfig(
            new FormPropertyConfig("min", "java.lang.String", "25"));
        // mirror the app form: extra dyna properties around the compared ones
        config.addFormPropertyConfig(
            new FormPropertyConfig("firstName", "java.lang.String", "Fred"));
        config.addFormPropertyConfig(
            new FormPropertyConfig("lastName", "java.lang.String", null));
        config.addFormPropertyConfig(
            new FormPropertyConfig("fieldA", "java.lang.String", null));
        config.addFormPropertyConfig(
            new FormPropertyConfig("fieldB", "java.lang.String", "XYZ"));
        config.addFormPropertyConfig(
            new FormPropertyConfig("address1", "java.lang.String", null));
        config.addFormPropertyConfig(
            new FormPropertyConfig("address2", "java.lang.String", null));
        config.addFormPropertyConfig(
            new FormPropertyConfig("city", "java.lang.String", "Boston"));
        config.addFormPropertyConfig(
            new FormPropertyConfig("zip", "java.lang.String", null));
        module.addFormBeanConfig(config);

        org.apache.struts.action.DynaActionForm form =
            (org.apache.struts.action.DynaActionForm) config.createActionForm();
        ((org.apache.struts.action.ActionForm) form).setServlet(servlet);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addPreferredLocale(java.util.Locale.ENGLISH);

        ActionErrors errors = ((DynaValidatorForm) form)
            .validate(mapping("numericWhenForm", null), request);
        assertTrue(errors.size() > 0,
            "9 > 25 must fail as a numeric comparison, got: " + errors.size());

        form.set("max", "30");
        ActionErrors ok = ((DynaValidatorForm) form)
            .validate(mapping("numericWhenForm", null), request);
        assertEquals(0, ok.size(), "30 > 25 must pass");
    }
    @Test
    void maskVarResolvesGlobalConstant() throws Exception {
        ModuleConfigImpl module = new ModuleConfigImpl("");
        FormBeanConfig config = new FormBeanConfig();
        config.setName("phoneForm");
        config.setType("org.apache.struts.validator.ValidatorForm");
        module.addFormBeanConfig(config);

        ValidatorFixtureForm form = new ValidatorFixtureForm();
        form.setServlet(servlet);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addPreferredLocale(java.util.Locale.ENGLISH);

        // phone is not a property of the fixture: use a subclass trick —
        // instead run via DynaValidatorForm
        org.apache.struts.action.DynaActionForm dyna;
        FormBeanConfig dynaConfig = new FormBeanConfig();
        dynaConfig.setName("phoneForm");
        dynaConfig.setType("org.apache.struts.validator.DynaValidatorForm");
        dynaConfig.addFormPropertyConfig(
            new FormPropertyConfig("phone", "java.lang.String", null));
        module.addFormBeanConfig(dynaConfig);
        dyna = (org.apache.struts.action.DynaActionForm) dynaConfig.createActionForm();
        ((org.apache.struts.action.ActionForm) dyna).setServlet(servlet);

        dyna.set("phone", "555-1234567");
        ActionErrors ok = ((DynaValidatorForm) dyna)
            .validate(mapping("phoneForm", null), request);
        assertEquals(0, ok.size(), "valid phone must pass the constant mask: "
            + (ok.size() > 0 ? ok.get().next().getKey() : ""));

        dyna.set("phone", "notaphone");
        ActionErrors bad = ((DynaValidatorForm) dyna)
            .validate(mapping("phoneForm", null), request);
        assertTrue(bad.size() > 0, "invalid phone must fail the mask");
    }
}
