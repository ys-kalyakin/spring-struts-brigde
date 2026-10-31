package org.apache.struts.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import org.apache.struts.fixtures.ScalarForm;

import jakarta.servlet.ServletException;

/**
 * Fixture tests of the scalar conversion surface of
 * {@link RequestUtils#populate}: these pin the observable behavior of the
 * current implementation (commons-beanutils 1.11 default registry
 * semantics: zero-values instead of errors for primitives, errors for
 * types without converters) so the replacement conversion service can be
 * verified against the same expectations.
 */
class PopulateScalarConversionFixtureTest {

    private ScalarForm populated(String... params) throws ServletException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("POST");
        for (int i = 0; i + 1 < params.length; i += 2) {
            request.setParameter(params[i], params[i + 1]);
        }
        ScalarForm form = new ScalarForm();
        RequestUtils.populate(form, request);
        return form;
    }

    private ScalarForm populatedWith(ScalarForm form, String... params)
        throws ServletException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("POST");
        for (int i = 0; i + 1 < params.length; i += 2) {
            request.setParameter(params[i], params[i + 1]);
        }
        RequestUtils.populate(form, request);
        return form;
    }

    private ServletException populateFails(String... params) {
        return assertThrows(ServletException.class, () -> populated(params));
    }

    @Test
    void booleanLiteralsSetTrue() throws Exception {
        ScalarForm form = populated("booleanPrimitive", "yes",
            "booleanWrapper", "On");
        assertEquals(true, form.isBooleanPrimitive());
        assertEquals(Boolean.TRUE, form.getBooleanWrapper());
    }

    @Test
    void numericAndUppercaseBooleanLiteralsSetTrue() throws Exception {
        ScalarForm form = populated("booleanPrimitive", "1",
            "booleanWrapper", "TRUE");
        assertEquals(true, form.isBooleanPrimitive());
        assertEquals(Boolean.TRUE, form.getBooleanWrapper());
    }

    @Test
    void booleanLiteralsSetFalse() throws Exception {
        ScalarForm form = populated("booleanPrimitive", "no",
            "booleanWrapper", "Off");
        assertEquals(false, form.isBooleanPrimitive());
        assertEquals(Boolean.FALSE, form.getBooleanWrapper());
    }

    @Test
    void zeroAndFalseBooleanLiteralsSetFalse() throws Exception {
        ScalarForm form = populated("booleanPrimitive", "0",
            "booleanWrapper", "FALSE");
        assertEquals(false, form.isBooleanPrimitive());
        assertEquals(Boolean.FALSE, form.getBooleanWrapper());
    }

    @Test
    void invalidBooleanGivesFalseWithoutError() throws Exception {
        ScalarForm form = new ScalarForm();
        form.setBooleanPrimitive(true);
        form.setBooleanWrapper(Boolean.TRUE);

        populatedWith(form, "booleanPrimitive", "maybe", "booleanWrapper", "");

        assertEquals(false, form.isBooleanPrimitive(),
            "invalid literal assigns the zero value");
        assertEquals(Boolean.FALSE, form.getBooleanWrapper());
    }

    @Test
    void numbersParseIntoPrimitivesAndWrappers() throws Exception {
        ScalarForm form = populated("bytePrimitive", "5", "byteWrapper", "6",
            "shortPrimitive", "-7", "shortWrapper", "8", "intPrimitive", "30",
            "intWrapper", "-40", "longPrimitive", "50", "longWrapper", "60",
            "floatPrimitive", "1.5", "floatWrapper", "2.5", "doublePrimitive",
            "3.14", "doubleWrapper", "-4.25");
        assertEquals((byte) 5, form.getBytePrimitive());
        assertEquals(Byte.valueOf((byte) 6), form.getByteWrapper());
        assertEquals((short) -7, form.getShortPrimitive());
        assertEquals(Short.valueOf((short) 8), form.getShortWrapper());
        assertEquals(30, form.getIntPrimitive());
        assertEquals(Integer.valueOf(-40), form.getIntWrapper());
        assertEquals(50L, form.getLongPrimitive());
        assertEquals(Long.valueOf(60L), form.getLongWrapper());
        assertEquals(1.5f, form.getFloatPrimitive());
        assertEquals(Float.valueOf(2.5f), form.getFloatWrapper());
        assertEquals(3.14, form.getDoublePrimitive());
        assertEquals(Double.valueOf(-4.25), form.getDoubleWrapper());
    }

    @Test
    void numbersAreTrimmedAndSigned() throws Exception {
        ScalarForm form = populated("intPrimitive", " 30 ", "intWrapper", "+5");
        assertEquals(30, form.getIntPrimitive());
        assertEquals(Integer.valueOf(5), form.getIntWrapper());
    }

    @Test
    void hexAndOverflowGiveZeroValueWithoutError() throws Exception {
        ScalarForm form = populated("intPrimitive", "0x1F",
            "intWrapper", "99999999999999", "longWrapper",
            "99999999999999999999");
        assertEquals(0, form.getIntPrimitive());
        assertEquals(Integer.valueOf(0), form.getIntWrapper());
        assertEquals(Long.valueOf(0L), form.getLongWrapper());
    }

    @Test
    void invalidAndEmptyNumbersGiveZeroValueWithoutError() throws Exception {
        ScalarForm form = new ScalarForm();
        form.setIntPrimitive(11);
        form.setIntWrapper(11);
        form.setDoublePrimitive(3.5);

        populatedWith(form, "intPrimitive", "abc", "intWrapper", "",
            "doublePrimitive", "2o");

        assertEquals(0, form.getIntPrimitive());
        assertEquals(Integer.valueOf(0), form.getIntWrapper());
        assertEquals(0.0, form.getDoublePrimitive());
    }

    @Test
    void emptyStringIntoStringStaysEmpty() throws Exception {
        ScalarForm form = new ScalarForm();
        form.setString("preset");

        populatedWith(form, "string", "");

        assertEquals("", form.getString());
    }

    @Test
    void characterTakesFirstChar() throws Exception {
        ScalarForm form = populated("charPrimitive", "A", "charWrapper", "9");
        assertEquals('A', form.getCharPrimitive());
        assertEquals(Character.valueOf('9'), form.getCharWrapper());
    }

    @Test
    void emptyCharacterGivesSpaceWithoutError() throws Exception {
        ScalarForm form = new ScalarForm();
        form.setCharPrimitive('x');
        form.setCharWrapper('y');

        populatedWith(form, "charPrimitive", "", "charWrapper", "");

        assertEquals(' ', form.getCharPrimitive(),
            "an empty string assigns the converter default (space)");
        assertEquals(Character.valueOf(' '), form.getCharWrapper());
    }

    @Test
    void bigDecimalAndBigIntegerParse() throws Exception {
        ScalarForm form = populated("bigDecimal", "3.14159",
            "bigInteger", "12345678901234567890");
        assertEquals("3.14159", form.getBigDecimal().toPlainString());
        assertEquals("12345678901234567890",
            form.getBigInteger().toString());
    }

    @Test
    void invalidBigDecimalAndBigIntegerFail() {
        populateFails("bigDecimal", "abc");
        populateFails("bigInteger", "");
    }

    @Test
    void sqlDateTimeTimestampParseJdbcFormats() throws Exception {
        ScalarForm form = populated("sqlDate", "2026-10-30", "sqlTime",
            "12:30:00", "sqlTimestamp", "2026-10-30 12:30:00");
        assertEquals(java.sql.Date.valueOf("2026-10-30"), form.getSqlDate());
        assertEquals(java.sql.Time.valueOf("12:30:00"), form.getSqlTime());
        assertEquals(java.sql.Timestamp.valueOf("2026-10-30 12:30:00"),
            form.getSqlTimestamp());
    }

    @Test
    void invalidSqlDateFails() {
        populateFails("sqlDate", "nope");
    }

    @Test
    void classFileUrlParse() throws Exception {
        ScalarForm form = populated("type", "java.lang.String", "file",
            "/tmp/x", "url", "http://example.com/");
        assertEquals(java.lang.String.class, form.getType());
        assertEquals(new java.io.File("/tmp/x"), form.getFile());
        assertEquals("http://example.com/", form.getUrl().toString());
    }

    @Test
    void invalidClassAndUrlFail() {
        populateFails("type", "no.such.Class");
        populateFails("url", "::bad");
    }

    @Test
    void dateHasNoConverterAndFails() {
        populateFails("date", "2026-10-30");
    }

    @Test
    void enumHasNoConverterAndFails() {
        populateFails("day", "MONDAY");
    }

    @Test
    void calendarHasNoConverterAndFails() {
        populateFails("calendar", "2026-10-30");
    }

    @Test
    void partialFillIsNotRolledBackOnError() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("POST");
        request.setParameter("string", "good");
        request.setParameter("bigDecimal", "abc");
        ScalarForm form = new ScalarForm();

        assertThrows(ServletException.class,
            () -> RequestUtils.populate(form, request));

        assertEquals("good", form.getString(),
            "properties assigned before the failure stay assigned");
    }
}
