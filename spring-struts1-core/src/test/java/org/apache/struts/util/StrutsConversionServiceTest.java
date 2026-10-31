package org.apache.struts.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Calendar;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.core.convert.ConversionFailedException;

/**
 * Unit tests of the converter matrix of
 * {@link StrutsConversionService}: the semantics of the commons-beanutils
 * default registry (zero values instead of errors for the primitive-backed
 * types, errors for types without converters), as pinned by the populate
 * fixture tests.
 */
class StrutsConversionServiceTest {

    private Object convert(Object value, Class<?> targetType) {
        return StrutsConversionService.convertOrFail(value, targetType);
    }

    private ConversionFailedException convertFails(String value,
        Class<?> targetType) {
        return assertThrows(ConversionFailedException.class,
            () -> convert(value, targetType));
    }

    @Test
    void booleanLiteralSet() {
        assertEquals(Boolean.TRUE, convert("true", Boolean.class));
        assertEquals(Boolean.TRUE, convert("YES", Boolean.class));
        assertEquals(Boolean.TRUE, convert("y", Boolean.class));
        assertEquals(Boolean.TRUE, convert("On", Boolean.class));
        assertEquals(Boolean.TRUE, convert("1", Boolean.class));
        assertEquals(Boolean.FALSE, convert("false", Boolean.class));
        assertEquals(Boolean.FALSE, convert("NO", Boolean.class));
        assertEquals(Boolean.FALSE, convert("n", Boolean.class));
        assertEquals(Boolean.FALSE, convert("Off", Boolean.class));
        assertEquals(Boolean.FALSE, convert("0", Boolean.class));
    }

    @Test
    void invalidBooleanGivesFalse() {
        assertEquals(Boolean.FALSE, convert("maybe", Boolean.class));
        assertEquals(Boolean.FALSE, convert("", Boolean.class));
    }

    @Test
    void numbersParseTrimmedAndSigned() {
        assertEquals(Integer.valueOf(30), convert(" 30 ", Integer.class));
        assertEquals(Integer.valueOf(5), convert("+5", Integer.class));
        assertEquals(Byte.valueOf((byte) -5), convert("-5", Byte.class));
        assertEquals(Short.valueOf((short) 8), convert("8", Short.class));
        assertEquals(Long.valueOf(60L), convert("60", Long.class));
        assertEquals(Float.valueOf(2.5f), convert("2.5", Float.class));
        assertEquals(Double.valueOf(-4.25), convert("-4.25", Double.class));
    }

    @Test
    void invalidAndOverflowingNumbersGiveZeroValue() {
        assertEquals(Integer.valueOf(0), convert("abc", Integer.class));
        assertEquals(Integer.valueOf(0), convert("", Integer.class));
        assertEquals(Integer.valueOf(0), convert("0x1F", Integer.class));
        assertEquals(Integer.valueOf(0), convert("99999999999999",
            Integer.class));
        assertEquals(Long.valueOf(0L), convert("99999999999999999999",
            Long.class));
        assertEquals(Double.valueOf(0.0), convert("abc", Double.class));
        assertEquals(Float.valueOf(0.0f), convert("", Float.class));
        assertEquals(Byte.valueOf((byte) 0), convert("x", Byte.class));
        assertEquals(Short.valueOf((short) 0), convert("x", Short.class));
    }

    @Test
    void charactersTakeFirstCharOrSpace() {
        assertEquals(Character.valueOf('A'), convert("A", Character.class));
        assertEquals(Character.valueOf('9'), convert("9", Character.class));
        assertEquals(Character.valueOf(' '), convert("", Character.class));
    }

    @Test
    void bigDecimalAndBigIntegerParseOrFail() {
        assertEquals(new BigDecimal("3.14159"),
            convert("3.14159", BigDecimal.class));
        assertEquals(new BigInteger("12345678901234567890"),
            convert("12345678901234567890", BigInteger.class));
        convertFails("abc", BigDecimal.class);
        convertFails("", BigDecimal.class);
        convertFails("abc", BigInteger.class);
        convertFails("", BigInteger.class);
    }

    @Test
    void sqlDateSqlTimeSqlTimestampParseJdbcFormatsOrFail() {
        assertEquals(java.sql.Date.valueOf("2026-10-30"),
            convert("2026-10-30", java.sql.Date.class));
        assertEquals(java.sql.Time.valueOf("12:30:00"),
            convert("12:30:00", java.sql.Time.class));
        assertEquals(java.sql.Timestamp.valueOf("2026-10-30 12:30:00"),
            convert("2026-10-30 12:30:00", java.sql.Timestamp.class));
        convertFails("nope", java.sql.Date.class);
        convertFails("nope", java.sql.Time.class);
        convertFails("nope", java.sql.Timestamp.class);
    }

    @Test
    void classFileUrlConvertOrFail() throws Exception {
        assertSame(String.class, convert("java.lang.String", Class.class));
        assertEquals(new java.io.File("/tmp/x"),
            convert("/tmp/x", java.io.File.class));
        assertEquals("http://example.com/",
            convert("http://example.com/", java.net.URL.class).toString());
        convertFails("no.such.Class", Class.class);
        convertFails("::bad", java.net.URL.class);
    }

    @Test
    void typesWithoutConverterFail() {
        convertFails("2026-10-30", java.util.Date.class);
        convertFails("MONDAY", ScalarDay.class);
        convertFails("now", Calendar.class);
        convertFails("a", List.class);
    }

    enum ScalarDay { MONDAY }

    @Test
    void arrayTargetConvertsComponentwiseWithZeroValues() {
        org.junit.jupiter.api.Assertions.assertArrayEquals(
            new int[] {1, 0},
            (int[]) convert(new String[] {"1", "abc"}, int[].class));
        org.junit.jupiter.api.Assertions.assertArrayEquals(
            new String[] {"a", "b"},
            (String[]) convert(new String[] {"a", "b"}, String[].class));
    }

    @Test
    void scalarTargetTakesFirstArrayElement() {
        assertEquals(Integer.valueOf(5),
            convert(new String[] {"5", "6"}, Integer.class));
        assertEquals("first",
            convert(new String[] {"first", "second"}, String.class));
    }

    @Test
    void identityConversionsAreNoOps() {
        assertEquals("as-is", convert("as-is", String.class));
        assertSame(Boolean.TRUE, convert(Boolean.TRUE, Boolean.class));
    }

    @Test
    void nullValueConvertsToNull() {
        assertEquals(null, StrutsConversionService.convertOrFail(null,
            Integer.class));
        assertTrue(true);
    }
}
