package org.apache.struts.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.DynaProperty;
import org.apache.commons.beanutils.LazyDynaBean;
import org.apache.commons.beanutils.LazyDynaClass;

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
 * message, leaving earlier assignments in place.</li>
 * </ul>
 */
final class FormPopulator {

    /**
     * The compatibility message of the former
     * <code>RequestUtils.populate</code> wrapping.
     */
    static final String POPULATE_ERROR_MESSAGE = "BeanUtils.populate";

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
        List<Segment> segments = StrutsExpressionParser.parse(name);
        if (segments.isEmpty()) {
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
        } catch (NotWritablePropertyException
            | NullValueInNestedPathException e) {
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
     * silently.
     */
    private static void invokeIndexedWriteMethod(Object target,
        Segment segment, Object value) {
        String methodName = "set" + Character.toUpperCase(
            segment.getName().charAt(0)) + segment.getName().substring(1);
        for (Method method : target.getClass().getMethods()) {
            if (!method.getName().equals(methodName)
                || method.getParameterCount() != 2
                || !isIntParameter(method.getParameterTypes()[0])) {
                continue;
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
            return;
        }
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
        } catch (NullValueInNestedPathException
            | NotWritablePropertyException e) {
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
