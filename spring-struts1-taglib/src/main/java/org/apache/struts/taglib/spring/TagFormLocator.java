package org.apache.struts.taglib.spring;

import org.apache.struts.taglib.TagUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.PageContext;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.struts.taglib.html.Constants;
import org.apache.struts.util.ModuleUtils;

import org.apache.struts.config.ModuleConfig;

/**
 * Spring component of the tag libraries: locates beans — most notably the
 * form bean of the current action — in the page scopes, converts scope
 * names and resolves module configurations. The application may override
 * the <code>tagFormLocator</code> bean to change these lookups for every
 * tag.
 */
public class TagFormLocator {

    /**
     * The message resources of the tag libraries, used for exception texts.
     */
    protected static final org.apache.struts.util.MessageResources messages =
        org.apache.struts.util.MessageResources.getMessageResources(
            "org.apache.struts.taglib.LocalStrings");

    /** Lowercase JSP scope names to their PageContext constants. */
    private static final Map<String, Integer> scopes = new HashMap<>();

    static {
        scopes.put("page", PageContext.PAGE_SCOPE);
        scopes.put("request", PageContext.REQUEST_SCOPE);
        scopes.put("session", PageContext.SESSION_SCOPE);
        scopes.put("application", PageContext.APPLICATION_SCOPE);
    }

    /**
     * Locate and return the specified bean, from an optionally specified
     * scope, in the specified page context. If no such bean is found, return
     * <code>null</code> instead.
     */
    public Object lookup(PageContext pageContext, String name,
        String scopeName) throws JspException {
        if (scopeName == null) {
            return pageContext.findAttribute(name);
        }
        try {
            return pageContext.getAttribute(name, getScope(scopeName));
        } catch (JspException e) {
            TagUtils.getInstance().saveException(pageContext, e);
            throw e;
        }
    }

    /**
     * Locate and return the specified property of the specified bean, from
     * an optionally specified scope, in the specified page context.
     */
    public Object lookup(PageContext pageContext, String name,
        String property, String scope) throws JspException {
        // Look up the requested bean, and return if requested
        Object bean = lookup(pageContext, name, scope);

        if (bean == null) {
            JspException e;
            if (scope == null) {
                e = new JspException(
                    messages.getMessage("lookup.bean.any", name));
            } else {
                e = new JspException(messages.getMessage("lookup.bean", name,
                    scope));
            }
            TagUtils.getInstance().saveException(pageContext, e);
            throw e;
        }

        if (property == null) {
            return bean;
        }

        // Locate and return the specified property
        try {
            return PropertyUtils.getProperty(bean, property);
        } catch (IllegalAccessException e) {
            TagUtils.getInstance().saveException(pageContext, e);
            throw new JspException(messages.getMessage("lookup.access",
                property, name), e);
        } catch (IllegalArgumentException e) {
            TagUtils.getInstance().saveException(pageContext, e);
            throw new JspException(messages.getMessage("lookup.argument",
                property, name), e);
        } catch (InvocationTargetException e) {
            Throwable t = e.getTargetException();
            if (t == null) {
                t = e;
            }
            TagUtils.getInstance().saveException(pageContext, t);
            throw new JspException(messages.getMessage("lookup.target",
                property, name), e);
        } catch (NoSuchMethodException e) {
            TagUtils.getInstance().saveException(pageContext, e);

            String beanName = name;

            // Name defaults to Constants.BEAN_KEY if no name is specified by
            // an input tag. Thus lookup the bean under the key and use
            // its class name for the exception message.
            if (Constants.BEAN_KEY.equals(name)) {
                Object obj = pageContext.findAttribute(Constants.BEAN_KEY);
                if (obj != null) {
                    beanName = obj.getClass().getName();
                }
            }

            throw new JspException(messages.getMessage("lookup.method",
                property, beanName), e);
        }
    }

    /**
     * Convert the scope name into its corresponding PageContext constant
     * value.
     */
    public int getScope(String scopeName) throws JspException {
        Integer scope = scopes.get(scopeName.toLowerCase());
        if (scope == null) {
            throw new JspException(messages.getMessage("lookup.scope", scope));
        }
        return scope;
    }

    /**
     * Return the default ModuleConfig object if it exists, null if
     * otherwise.
     */
    public ModuleConfig getModuleConfig(PageContext pageContext) {
        return getModuleConfig(null, pageContext);
    }

    /**
     * Return the specified ModuleConfig object for the given prefix if it
     * exists, otherwise a NullPointerException will be thrown.
     */
    public ModuleConfig getModuleConfig(String module,
        PageContext pageContext) {
        ModuleConfig config = ModuleUtils.getInstance().getModuleConfig(
            module, (HttpServletRequest) pageContext.getRequest(),
            pageContext.getServletContext());
        if (config == null) {
            throw new NullPointerException(
                "Module '" + module + "' not found.");
        }
        return config;
    }
}
