package org.apache.struts.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.DynaProperty;
import org.apache.commons.beanutils.LazyDynaBean;
import org.apache.commons.beanutils.LazyDynaClass;

import org.springframework.beans.InvalidPropertyException;
import org.springframework.beans.NotWritablePropertyException;
import org.springframework.beans.NullValueInNestedPathException;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.core.convert.TypeDescriptor;

import org.apache.struts.util.StrutsExpressionParser.Segment;

import jakarta.servlet.ServletException;

/**
 * The populate implementation on top of Spring's {@link BeanWrapper} and
 * {@link StrutsConversionService}: assigns the collected request values to
 * the properties of a form bean, reproducing the observable behavior of
 * the former <code>BeanUtils.populate</code> call as pinned by the
 * populate fixture tests:
 *
 * <ul>
 * <li>unknown properties and null navigation targets are skipped
 * silently;</li>
 * <li>arrays of parameter values fill array properties component-wise and
 * scalar properties with their first element;</li>
 * <li>a mapped property of a plain bean receives the raw parameter value;
 * a mapped property of a DynaBean receives the raw parameter value
 * too;</li>
 * <li>an indexed mapped element (an existing <code>Map</code>) receives
 * the first parameter element, converted to the map value type;</li>
 * <li>an undeclared property of a lazy form is added with the first
 * parameter element as a String;</li>
 * <li>assignment proceeds until the first conversion failure, which
 * surfaces as a <code>ServletException</code> with the compatibility
 * message, leaving earlier assignments in place;</li>
 * <li>a parameter name that uses <code>class</code> as a property name is
 * skipped silently (CVE-2014-0114), as is a name with an unknown or
 * unreadable intermediate property of a nested path.</li>
 * </ul>
 */
final class FormPopulator {

    /**
     * The compatibility message of the former
     * <code>RequestUtils.populate</code> wrapping.
     */
    static final String POPULATE_ERROR_MESSAGE = "BeanUtils.populate";

    /**
     * Entry limits of the populate caches. Parameter names arrive with
     * requests, so the caches are capped: legitimate forms fit comfortably,
     * while floods of unique attacker-chosen names stop growing the maps.
     */
    private static final int MAX_CACHED_ENTRIES = 1024;

    /**
     * Parsed parameter-name segments, keyed by the raw parameter name: the
     * names of a form repeat on every request, so the parse runs once per
     * name instead of once per request.
     */
    private static final ConcurrentHashMap<String, List<Segment>>
        PARSED_NAMES = new ConcurrentHashMap<>();

    /**
     * Resolved indexed write methods (<code>set&lt;Name&gt;(int, T)</code>),
     * keyed by class name and method name; <code>Optional.empty()</code>
     * marks classes without such a method so the scan does not repeat.
     */
    private static final ConcurrentHashMap<String, Optional<Method>>
        INDEXED_WRITE_METHODS = new ConcurrentHashMap<>();

    private FormPopulator() {
    }

    /**
     * Assign the given property values (parameter names in beanutils
     * syntax mapped to their request values) to the given form bean.
     */
    static void populate(Object bean, Map<String, Object> properties)
        throws ServletException {
        for (Map.Entry<String, Object> entry : properties.entrySet()) {
            String name = entry.getKey();
            if (name == null) {
                continue;
            }
            try {
                populateProperty(bean, name, entry.getValue());
            } catch (RuntimeException e) {
                throw new ServletException(POPULATE_ERROR_MESSAGE, e);
            }
        }
    }

    private static void populateProperty(Object bean, String name,
        Object value) {
        List<Segment> segments = parsedSegments(name);
        if (segments.isEmpty()) {
            return;
        }
        if (hasClassAccess(segments)) {
            return;
        }

        Object target = bean;
        for (int i = 0; i < segments.size() - 1; i++) {
            target = navigate(target, segments.get(i));
            if (target == null) {
                return;
            }
        }
        assign(target, segments.get(segments.size() - 1), value);
    }

    /**
     * The parameter name navigates through the reserved property name
     * <code>class</code> (CVE-2014-0114): such access is ignored regardless
     * of its position — bare, nested, indexed or mapped. The
     * <code>RequestUtils</code> regex filter blocks the typical forms
     * before populate; this second layer covers the remaining forms (a bare
     * <code>class</code>, a trailing <code>x.class</code>, a mapped access)
     * and direct callers of populate. A property named <code>class</code>
     * cannot be populated by a request parameter.
     */
    private static boolean hasClassAccess(List<Segment> segments) {
        for (Segment segment : segments) {
            if ("class".equalsIgnoreCase(segment.getName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Resolve an intermediate segment: a property read of a DynaBean or a
     * plain bean.
     */
    private static Object navigate(Object target, Segment segment) {
        if (target instanceof DynaBean) {
            DynaBean dynaBean = (DynaBean) target;
            if (dynaBean.getDynaClass().getDynaProperty(segment.getName())
                == null) {
                return null;
            }
            return dynaBean.get(segment.getName());
        }
        BeanWrapper wrapper = wrapperOf(target);
        try {
            return wrapper.getPropertyValue(segment.getName());
        } catch (InvalidPropertyException e) {
            // an unknown or unreadable intermediate property: skipped
            // silently, as the populate spec requires
            return null;
        }
    }

    private static void assign(Object target, Segment segment,
        Object value) {
        if (target instanceof DynaBean) {
            assignToDynaBean((DynaBean) target, segment, value);
        } else {
            assignToBean(wrapperOf(target), segment, value);
        }
    }

    /**
     * Parse a parameter name into its segments through the cache: a
     * repeated name (the normal case — form fields repeat on every
     * request) is served from the parsed segments.
     */
    private static List<Segment> parsedSegments(String name) {
        List<Segment> cached = PARSED_NAMES.get(name);
        if (cached != null) {
            return cached;
        }
        List<Segment> parsed = StrutsExpressionParser.parse(name);
        if (PARSED_NAMES.size() < MAX_CACHED_ENTRIES) {
            List<Segment> winner = PARSED_NAMES.putIfAbsent(name, parsed);
            if (winner != null) {
                return winner;
            }
        }
        return parsed;
    }

    // ------------------------------------------------------ plain beans

    private static void assignToBean(BeanWrapper wrapper, Segment segment,
        Object value) {
        String name = segment.getName();

        TypeDescriptor descriptor = descriptorOf(wrapper, name);

        if (segment.isMapped()) {
            assignToMap(wrapper, segment, value);
            return;
        }

        if (segment.isIndexed()) {
            TypeDescriptor elementDescriptor = elementDescriptorOf(descriptor);
            if (elementDescriptor != null) {
                Object converted = convertFor(elementDescriptor, value);
                try {
                    wrapper.setPropertyValue(name + "[" + segment.getIndex()
                        + "]", converted);
                    return;
                } catch (NullValueInNestedPathException e) {
                    // a null element or container: skipped silently
                    return;
                } catch (NotWritablePropertyException e) {
                    // fall through to an indexed write method, if any
                }
            }
            invokeIndexedWriteMethod(wrapper.getWrappedInstance(), segment,
                value);
            return;
        }

        if (descriptor == null) {
            return;
        }

        Object converted = convertFor(descriptor, value);
        try {
            wrapper.setPropertyValue(name, converted);
        } catch (NotWritablePropertyException
            | NullValueInNestedPathException e) {
            // unknown property or null nested target: skipped silently
        }
    }

    /**
     * Mapped assignment to a plain bean: the top-level mapped property
     * receives the raw parameter value; an indexed mapped element (an
     * existing <code>Map</code>) receives the first parameter element
     * converted to the map value type.
     */
    private static void assignToMap(BeanWrapper wrapper, Segment segment,
        Object value) {
        String name = segment.getName();
        TypeDescriptor descriptor = descriptorOf(wrapper, name);
        if (descriptor == null) {
            return;
        }

        if (segment.isIndexed()) {
            Object element;
            try {
                element = wrapper.getPropertyValue(name + "["
                    + segment.getIndex() + "]");
            } catch (NotWritablePropertyException
                | NullValueInNestedPathException e) {
                return;
            }
            if (element instanceof Map) {
                TypeDescriptor elementType = descriptorOf(wrapper, name + "["
                    + segment.getIndex() + "]");
                TypeDescriptor valueType = (elementType != null
                    && elementType.getMapValueTypeDescriptor() != null)
                    ? elementType.getMapValueTypeDescriptor()
                    : TypeDescriptor.valueOf(Object.class);
                Object converted = convertFor(valueType, firstElement(value));
                @SuppressWarnings("unchecked")
                Map<Object, Object> map = (Map<Object, Object>) element;
                map.put(segment.getKey(), converted);
            }
            return;
        }

        try {
            Object map = wrapper.getPropertyValue(name);
            if (map instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<Object, Object> target = (Map<Object, Object>) map;
                target.put(segment.getKey(), value);
            }
        } catch (NotWritablePropertyException
            | NullValueInNestedPathException e) {
            // unknown property or null nested target: skipped silently
        }
    }

    /**
     * Assign through a classic JavaBeans indexed write method
     * (<code>set&lt;Name&gt;(int index, T value)</code>), the pattern
     * beanutils resolves for properties without an array or list field
     * (as in the weblegacy example forms). A missing method is skipped
     * silently; the method scan is cached per class and method name.
     */
    private static void invokeIndexedWriteMethod(Object target,
        Segment segment, Object value) {
        String methodName = "set" + Character.toUpperCase(
            segment.getName().charAt(0)) + segment.getName().substring(1);
        Method method = indexedWriteMethodOf(target.getClass(), methodName);
        if (method == null) {
            return;
        }
        Object converted = convertFor(TypeDescriptor.valueOf(
            StrutsConversionService.boxedType(
                method.getParameterTypes()[1])), value);
        try {
            method.invoke(target, segment.getIndex(), converted);
        } catch (IllegalAccessException e) {
            // not invocable: skipped silently
        } catch (InvocationTargetException e) {
            throw new IllegalStateException(e.getTargetException());
        }
    }

    /**
     * Return the indexed write method of the given name (two parameters,
     * the first an <code>int</code>) through the cache, or null when the
     * class declares none (the negative result is cached as well).
     */
    private static Method indexedWriteMethodOf(Class<?> clazz,
        String methodName) {
        String key = clazz.getName() + '#' + methodName;
        Optional<Method> cached = INDEXED_WRITE_METHODS.get(key);
        if (cached != null) {
            return cached.orElse(null);
        }
        Method found = null;
        for (Method method : clazz.getMethods()) {
            if (!method.getName().equals(methodName)
                || method.getParameterCount() != 2
                || !isIntParameter(method.getParameterTypes()[0])) {
                continue;
            }
            found = method;
            break;
        }
        if (INDEXED_WRITE_METHODS.size() < MAX_CACHED_ENTRIES) {
            INDEXED_WRITE_METHODS.putIfAbsent(key,
                Optional.ofNullable(found));
        }
        return found;
    }

    private static boolean isIntParameter(Class<?> type) {
        return type == int.class || type == Integer.class;
    }

    // -------------------------------------------------------- DynaBeans

    private static void assignToDynaBean(DynaBean dynaBean, Segment segment,
        Object value) {
        String name = segment.getName();
        DynaProperty property =
            dynaBean.getDynaClass().getDynaProperty(name);

        if (segment.isMapped()) {
            if (property == null) {
                if (isLazy(dynaBean)) {
                    dynaBean.set(name, segment.getKey(), value);
                }
                return;
            }
            dynaBean.set(name, segment.getKey(), value);
            return;
        }

        if (property == null) {
            if (isLazy(dynaBean)) {
                dynaBean.set(name, firstElement(value));
            }
            return;
        }

        if (segment.isIndexed()) {
            Object converted = StrutsConversionService.convertOrFail(value,
                componentType(property));
            try {
                dynaBean.set(name, segment.getIndex(), converted);
            } catch (RuntimeException e) {
                // out-of-range or non-indexed targets: skipped silently
            }
            return;
        }

        Object converted;
        Class<?> type = property.getType();
        if (type != null && type.isArray()) {
            converted = StrutsConversionService.convertOrFail(value, type);
        } else if (value instanceof Object[] && Object.class.equals(type)) {
            // the beanutils array-to-scalar rule also applies to the
            // Object placeholder auto-added for lazy properties: the
            // first element is stored as-is and re-declares the property
            Object[] array = (Object[]) value;
            converted = array.length > 0 ? array[0] : null;
        } else {
            converted = StrutsConversionService.convertOrFail(value, type);
        }
        try {
            dynaBean.set(name, converted);
        } catch (RuntimeException e) {
            // rejected assignment: skipped silently, as before
        }
    }

    private static boolean isLazy(DynaBean dynaBean) {
        return dynaBean instanceof LazyDynaBean
            || dynaBean.getDynaClass() instanceof LazyDynaClass;
    }

    private static Class<?> componentType(DynaProperty property) {
        Class<?> type = property.getType();
        if (type.isArray()) {
            return type.getComponentType();
        }
        return Object.class;
    }

    // ----------------------------------------------------------- shared

    /**
     * The first element of an array or list value, or the value itself.
     */
    private static Object firstElement(Object value) {
        if (value instanceof Object[]) {
            Object[] array = (Object[]) value;
            return array.length > 0 ? array[0] : null;
        }
        return value;
    }

    private static Object convertFor(TypeDescriptor descriptor,
        Object value) {
        TypeDescriptor source = TypeDescriptor.valueOf(
            value != null ? value.getClass() : Object.class);
        TypeDescriptor target = descriptor;
        if (target != null && target.getObjectType().isPrimitive()) {
            target = TypeDescriptor.valueOf(
                StrutsConversionService.boxedType(target.getObjectType()));
        }
        return StrutsConversionService.convertOrFail(value, source, target);
    }

    private static TypeDescriptor descriptorOf(BeanWrapper wrapper,
        String name) {
        try {
            return wrapper.getPropertyTypeDescriptor(name);
        } catch (InvalidPropertyException e) {
            return null;
        }
    }

    /**
     * The element type descriptor of an indexed target: the component type
     * for arrays, the generic element type for lists.
     */
    private static TypeDescriptor elementDescriptorOf(
        TypeDescriptor descriptor) {
        if (descriptor == null) {
            return null;
        }
        TypeDescriptor element = descriptor.getElementTypeDescriptor();
        if (element != null) {
            return element;
        }
        Class<?> type = descriptor.getObjectType();
        if (type.isArray()) {
            return TypeDescriptor.valueOf(
                StrutsConversionService.boxedType(type.getComponentType()));
        }
        return TypeDescriptor.valueOf(Object.class);
    }

    private static BeanWrapper wrapperOf(Object target) {
        BeanWrapper wrapper = new BeanWrapperImpl(target);
        return wrapper;
    }
}
