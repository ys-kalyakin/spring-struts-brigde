package org.apache.struts.util;

import java.lang.reflect.InvocationTargetException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;

import org.apache.commons.beanutils.BeanUtils;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionServlet;
import org.apache.struts.config.FormBeanConfig;

/**
 * General purpose utility methods related to processing a servlet request.
 *
 * <p>Ported from weblegacy/struts1 1.5.x (org.apache.struts.util.RequestUtils).</p>
 *
 * <p><strong>ConvertUtils isolation:</strong> this class is the single access
 * point of the engine for commons-beanutils population and the static
 * <code>ConvertUtils</code> registry. No other engine class is allowed to
 * touch beanutils converters directly.</p>
 */
public class RequestUtils {

    /**
     * Create and return a fully-qualified class instance of the given name,
     * using the given class loader.
     */
    public static Object applicationInstance(String className, ClassLoader cl)
        throws ClassNotFoundException, InstantiationException,
        IllegalAccessException, InvocationTargetException,
        NoSuchMethodException {
        Class<?> clazz = (cl != null) ? cl.loadClass(className)
            : Class.forName(className);
        return clazz.getDeclaredConstructor().newInstance();
    }

    /**
     * <p>Populate the properties of the specified JavaBean from the specified
     * request. Conversion is performed by commons-beanutils with its default
     * converter registry (compatible semantics with Struts 1).</p>
     */
    public static void populate(Object bean, HttpServletRequest request)
        throws ServletException {
        populate(bean, null, null, request);
    }

    /**
     * <p>Populate the properties of the specified JavaBean from the specified
     * request, optionally restricting the processed parameter names to those
     * with the given prefix and/or suffix (which are stripped from the
     * property names).</p>
     */
    public static void populate(Object bean, String prefix, String suffix,
        HttpServletRequest request) throws ServletException {
        java.util.Map<String, String[]> filtered =
            new java.util.LinkedHashMap<>();
        for (java.util.Map.Entry<String, String[]> entry : request
            .getParameterMap().entrySet()) {
            String name = entry.getKey();
            if (prefix != null) {
                if (!name.startsWith(prefix)) {
                    continue;
                }
                name = name.substring(prefix.length());
            }
            if (suffix != null) {
                if (!name.endsWith(suffix)) {
                    continue;
                }
                name = name.substring(0,
                    name.length() - suffix.length());
            }
            filtered.put(name, entry.getValue());
        }
        try {
            BeanUtils.populate(bean, filtered);
        } catch (Exception e) {
            throw new ServletException("BeanUtils.populate", e);
        }
    }

    /**
     * Create and return an <code>ActionForm</code> instance appropriate to
     * the information in the specified <code>FormBeanConfig</code>.
     */
    public static ActionForm createActionForm(FormBeanConfig config,
        ActionServlet servlet) throws ServletException {
        try {
            return config.createActionForm();
        } catch (IllegalStateException e) {
            throw new ServletException(e.getMessage(), e.getCause());
        }
    }
}
