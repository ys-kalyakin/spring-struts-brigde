package org.apache.struts.util;

import java.io.File;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Set;

import org.springframework.core.convert.ConversionFailedException;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.ConditionalGenericConverter;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;
import org.springframework.core.convert.support.GenericConversionService;

/**
 * The engine's own conversion service for populating form beans: a fixed,
 * immutable converter set reproducing the semantics of the default
 * commons-beanutils registry (as pinned by the populate fixture tests) so
 * that the request path no longer depends on the static
 * <code>ConvertUtils</code> registry.
 *
 * <p>Semantics preserved from the beanutils default registry:</p>
 * <ul>
 * <li>Booleans accept the literals true/yes/y/on/1 and false/no/n/off/0
 * case-insensitively; any other input yields <code>false</code>.</li>
 * <li>Numbers are trimmed and parsed decimally; invalid input, the empty
 * string and overflow yield the zero value of the target type (hex
 * notation is not supported).</li>
 * <li>Characters take the first character; the empty string yields a
 * space.</li>
 * <li><code>BigDecimal</code>/<code>BigInteger</code>, JDBC-formatted
 * <code>java.sql</code> date/time types, <code>Class</code> and
 * <code>URL</code> throw on unparseable input.</li>
 * <li>Types without converters in the beanutils registry
 * (<code>java.util.Date</code>, enums, <code>Calendar</code>, collections)
 * have no converter here either and fail to convert.</li>
 * <li>An array of parameter values fills a scalar property with its first
 * element and an array property component-wise; a single value fills an
 * array property with a one-element array; anything converts to
 * <code>String</code> through <code>toString</code>.</li>
 * </ul>
 */
final class StrutsConversionService extends GenericConversionService {

    /**
     * The shared immutable instance used by the populate path.
     */
    static final StrutsConversionService INSTANCE =
        new StrutsConversionService();

    private StrutsConversionService() {
        addConverter(new StringToBooleanConverter());
        addConverterFactory(new StringToNumberConverterFactory());
        addConverter(new StringToCharacterConverter());
        addConverter(new StringToSqlDateConverter());
        addConverter(new StringToSqlTimeConverter());
        addConverter(new StringToSqlTimestampConverter());
        addConverter(new StringToClassConverter());
        addConverter(new StringToFileConverter());
        addConverter(new StringToUrlConverter());
        addConverter(new ArrayToArrayConverter());
        addConverter(new ArrayToScalarConverter());
        addConverter(new ScalarToArrayConverter());
        addConverter(new AnyToStringConverter());
    }

    /**
     * Convert the given value to the given target type, mapping any
     * conversion failure (including the absence of a converter) to a
     * {@link ConversionFailedException} so callers can treat "no converter"
     * and "bad input" alike. Primitive targets are converted to their
     * wrapper types.
     */
    static Object convertOrFail(Object value, Class<?> targetType) {
        return convertOrFail(value,
            TypeDescriptor.valueOf(
                value != null ? value.getClass() : Object.class),
            TypeDescriptor.valueOf(boxedType(targetType)));
    }

    /**
     * Convert the given value to the given target descriptor, mapping any
     * conversion failure (including the absence of a converter) to a
     * {@link ConversionFailedException}.
     */
    static Object convertOrFail(Object value, TypeDescriptor sourceType,
        TypeDescriptor targetType) {
        if (value == null) {
            return null;
        }
        try {
            return INSTANCE.convert(value, sourceType, targetType);
        } catch (ConversionFailedException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new ConversionFailedException(sourceType, targetType,
                value, e);
        }
    }

    static Class<?> boxedType(Class<?> type) {
        if (!type.isPrimitive()) {
            return type;
        }
        if (type == boolean.class) {
            return Boolean.class;
        }
        if (type == byte.class) {
            return Byte.class;
        }
        if (type == short.class) {
            return Short.class;
        }
        if (type == int.class) {
            return Integer.class;
        }
        if (type == long.class) {
            return Long.class;
        }
        if (type == float.class) {
            return Float.class;
        }
        if (type == double.class) {
            return Double.class;
        }
        return Character.class;
    }

    /**
     * Booleans: the classic Struts literal set with <code>false</code> as
     * the missing value.
     */
    private static class StringToBooleanConverter
        implements Converter<String, Boolean> {

        private static final Set<String> TRUE_VALUES =
            Set.of("true", "yes", "y", "on", "1");

        @Override
        public Boolean convert(String source) {
            String value = source.toLowerCase();
            if (TRUE_VALUES.contains(value)) {
                return Boolean.TRUE;
            }
            return Boolean.FALSE;
        }
    }

    /**
     * Characters: the first character of the string, space when empty.
     */
    private static class StringToCharacterConverter
        implements Converter<String, Character> {

        @Override
        public Character convert(String source) {
            return source.isEmpty() ? Character.valueOf(' ')
                : Character.valueOf(source.charAt(0));
        }
    }

    /**
     * Numbers: decimal parsing with the zero value of the target type as
     * the missing value for the primitive-backed wrappers, exceptions for
     * the arbitrary-precision types.
     */
    private static class StringToNumberConverterFactory
        implements ConverterFactory<String, Number> {

        @Override
        public <T extends Number> Converter<String, T> getConverter(
            Class<T> targetType) {
            return new StringToNumber<>(targetType);
        }
    }

    private static class StringToNumber<T extends Number>
        implements Converter<String, T> {

        private static final Set<Class<?>> ZERO_DEFAULT_TYPES = Set.of(
            Byte.class, Short.class, Integer.class, Long.class,
            Float.class, Double.class);

        private final Class<T> targetType;

        StringToNumber(Class<T> targetType) {
            this.targetType = targetType;
        }

        @Override
        public T convert(String source) {
            String value = source.trim();
            try {
                return parse(value);
            } catch (RuntimeException e) {
                if (ZERO_DEFAULT_TYPES.contains(targetType)) {
                    return zeroValue();
                }
                throw new ConversionFailedException(
                    TypeDescriptor.valueOf(String.class),
                    TypeDescriptor.valueOf(targetType), source, e);
            }
        }

        @SuppressWarnings("unchecked")
        private T parse(String value) {
            if (targetType == Byte.class) {
                return (T) Byte.valueOf(Byte.parseByte(value));
            }
            if (targetType == Short.class) {
                return (T) Short.valueOf(Short.parseShort(value));
            }
            if (targetType == Integer.class) {
                return (T) Integer.valueOf(Integer.parseInt(value));
            }
            if (targetType == Long.class) {
                return (T) Long.valueOf(Long.parseLong(value));
            }
            if (targetType == Float.class) {
                return (T) Float.valueOf(Float.parseFloat(value));
            }
            if (targetType == Double.class) {
                return (T) Double.valueOf(Double.parseDouble(value));
            }
            if (targetType == BigDecimal.class) {
                return (T) new BigDecimal(value);
            }
            if (targetType == BigInteger.class) {
                return (T) new BigInteger(value);
            }
            throw new IllegalArgumentException(
                "No number conversion to " + targetType.getName());
        }

        @SuppressWarnings("unchecked")
        private T zeroValue() {
            if (targetType == Byte.class) {
                return (T) Byte.valueOf((byte) 0);
            }
            if (targetType == Short.class) {
                return (T) Short.valueOf((short) 0);
            }
            if (targetType == Integer.class) {
                return (T) Integer.valueOf(0);
            }
            if (targetType == Long.class) {
                return (T) Long.valueOf(0L);
            }
            if (targetType == Float.class) {
                return (T) Float.valueOf(0.0f);
            }
            return (T) Double.valueOf(0.0);
        }
    }

    /**
     * <code>java.sql.Date</code>: JDBC format, exceptions on bad input.
     */
    private static class StringToSqlDateConverter
        implements Converter<String, java.sql.Date> {

        @Override
        public java.sql.Date convert(String source) {
            try {
                return java.sql.Date.valueOf(source);
            } catch (RuntimeException e) {
                throw failed(source, java.sql.Date.class, e);
            }
        }
    }

    /**
     * <code>java.sql.Time</code>: JDBC format, exceptions on bad input.
     */
    private static class StringToSqlTimeConverter
        implements Converter<String, java.sql.Time> {

        @Override
        public java.sql.Time convert(String source) {
            try {
                return java.sql.Time.valueOf(source);
            } catch (RuntimeException e) {
                throw failed(source, java.sql.Time.class, e);
            }
        }
    }

    /**
     * <code>java.sql.Timestamp</code>: JDBC format, exceptions on bad
     * input.
     */
    private static class StringToSqlTimestampConverter
        implements Converter<String, java.sql.Timestamp> {

        @Override
        public java.sql.Timestamp convert(String source) {
            try {
                return java.sql.Timestamp.valueOf(source);
            } catch (RuntimeException e) {
                throw failed(source, java.sql.Timestamp.class, e);
            }
        }
    }

    /**
     * <code>Class</code>: load by name, exceptions on unknown classes.
     */
    private static class StringToClassConverter
        implements Converter<String, Class<?>> {

        @Override
        public Class<?> convert(String source) {
            try {
                return Class.forName(source);
            } catch (RuntimeException | ClassNotFoundException e) {
                throw failed(source, Class.class, e);
            }
        }
    }

    /**
     * <code>File</code>: path construction.
     */
    private static class StringToFileConverter
        implements Converter<String, File> {

        @Override
        public File convert(String source) {
            return new File(source);
        }
    }

    /**
     * <code>URL</code>: exceptions on malformed URLs.
     */
    private static class StringToUrlConverter
        implements Converter<String, URL> {

        @Override
        public URL convert(String source) {
            try {
                return new URL(source);
            } catch (MalformedURLException e) {
                throw failed(source, URL.class, e);
            }
        }
    }

    /**
     * An array of parameter values fills an array property component-wise,
     * converting every element to the component type (invalid elements
     * yield the zero value of the component type, beanutils semantics).
     */
    private static class ArrayToArrayConverter
        implements ConditionalGenericConverter {

        @Override
        public Set<ConvertiblePair> getConvertibleTypes() {
            return null;
        }

        @Override
        public boolean matches(TypeDescriptor sourceType,
            TypeDescriptor targetType) {
            return sourceType.isArray() && targetType.isArray()
                && !targetType.getObjectType()
                    .isAssignableFrom(sourceType.getObjectType());
        }

        @Override
        public Object convert(Object source, TypeDescriptor sourceType,
            TypeDescriptor targetType) {
            int length = Array.getLength(source);
            Class<?> componentType =
                targetType.getObjectType().getComponentType();
            TypeDescriptor elementSource = TypeDescriptor.valueOf(
                source.getClass().getComponentType());
            TypeDescriptor elementDescriptor = TypeDescriptor.valueOf(
                boxedType(componentType));
            Object array = Array.newInstance(componentType, length);
            for (int i = 0; i < length; i++) {
                Object element = Array.get(source, i);
                Array.set(array, i, element == null ? null
                    : convertOrFail(element, elementSource,
                        elementDescriptor));
            }
            return array;
        }
    }

    /**
     * An array of parameter values fills a scalar property with its first
     * element, converted to the property type (beanutils semantics).
     */
    private static class ArrayToScalarConverter
        implements ConditionalGenericConverter {

        @Override
        public Set<ConvertiblePair> getConvertibleTypes() {
            return null;
        }

        @Override
        public boolean matches(TypeDescriptor sourceType,
            TypeDescriptor targetType) {
            return sourceType.isArray() && !targetType.isArray()
                && !targetType.getObjectType()
                    .isAssignableFrom(sourceType.getObjectType());
        }

        @Override
        public Object convert(Object source, TypeDescriptor sourceType,
            TypeDescriptor targetType) {
            int length = Array.getLength(source);
            if (length == 0) {
                return null;
            }
            Object element = Array.get(source, 0);
            return convertOrFail(element,
                TypeDescriptor.valueOf(element.getClass()), targetType);
        }
    }

    /**
     * A single value fills an array property with a one-element array,
     * converting the element to the component type (beanutils semantics).
     */
    private static class ScalarToArrayConverter
        implements ConditionalGenericConverter {

        @Override
        public Set<ConvertiblePair> getConvertibleTypes() {
            return null;
        }

        @Override
        public boolean matches(TypeDescriptor sourceType,
            TypeDescriptor targetType) {
            return targetType.isArray() && !sourceType.isArray()
                && !targetType.getObjectType()
                    .isAssignableFrom(sourceType.getObjectType());
        }

        @Override
        public Object convert(Object source, TypeDescriptor sourceType,
            TypeDescriptor targetType) {
            Class<?> componentType =
                targetType.getObjectType().getComponentType();
            TypeDescriptor elementDescriptor = TypeDescriptor.valueOf(
                boxedType(componentType));
            Object element = convertOrFail(source,
                TypeDescriptor.valueOf(source.getClass()), elementDescriptor);
            Object array = Array.newInstance(componentType, 1);
            Array.set(array, 0, element);
            return array;
        }
    }

    /**
     * Anything converts to <code>String</code> through
     * <code>toString</code> (the beanutils <code>StringConverter</code>).
     */
    private static class AnyToStringConverter
        implements ConditionalGenericConverter {

        @Override
        public Set<ConvertiblePair> getConvertibleTypes() {
            return Collections.singleton(
                new ConvertiblePair(Object.class, String.class));
        }

        @Override
        public boolean matches(TypeDescriptor sourceType,
            TypeDescriptor targetType) {
            return targetType.getObjectType() == String.class
                && !sourceType.isArray();
        }

        @Override
        public Object convert(Object source, TypeDescriptor sourceType,
            TypeDescriptor targetType) {
            return String.valueOf(source);
        }
    }

    private static ConversionFailedException failed(String source,
        Class<?> targetType, Exception cause) {
        return new ConversionFailedException(
            TypeDescriptor.valueOf(String.class),
            TypeDescriptor.valueOf(targetType), source, cause);
    }
}
