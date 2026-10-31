/*
 * $Id$
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.struts.taglib;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.util.Locale;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.JspWriter;
import jakarta.servlet.jsp.PageContext;
import jakarta.servlet.jsp.tagext.BodyContent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.struts.Globals;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.config.ModuleConfig;
import org.apache.struts.taglib.spring.ErrorsMessagesRenderer;
import org.apache.struts.taglib.spring.TagFormLocator;
import org.apache.struts.taglib.spring.TagMessageResolver;
import org.apache.struts.taglib.spring.TagServices;
import org.apache.struts.taglib.spring.TagUrlBuilder;
import org.apache.struts.util.MessageResources;
import org.apache.struts.util.ResponseUtils;

/**
 * Provides helper methods for JSP tags. The classic static API is a facade:
 * the shared logic lives in the Spring components of
 * {@link org.apache.struts.taglib.spring} — the message resolver, the URL
 * builder, the form locator and the errors/MESSAGES renderer — resolved for
 * the current request from the application context, with the engine default
 * implementations used when no bean is registered. Overriding a service
 * bean changes the behavior of every tag that uses it.
 *
 * @version $Rev$
 * @since Struts 1.2
 */
public class TagUtils {

    /**
     * Constant for
     * <a href="https://www.w3.org/TR/xhtml1/#docconf">XHTML 1.0</a>
     * conformance.
     *
     * @since Struts 1.4
     */
    public static final BigDecimal XHTML_1_0 = new BigDecimal("1.0");

    /**
     * Constant for
     * <a href="https://www.w3.org/TR/xhtml11/conformance.html">XHTML 1.1</a>
     * conformance.
     *
     * @since Struts 1.4
     */
    public static final BigDecimal XHTML_1_1 = new BigDecimal("1.1");

    /**
     * Constant for
     * <a href="https://www.w3.org/TR/xhtml2/conformance.html#s_conform">XHTML
     * 2.0</a> conformance.
     *
     * @since Struts 1.4
     */
    public static final BigDecimal XHTML_2_0 = new BigDecimal("2.0");

    /**
     * Constant for
     * <a href="https://html.spec.whatwg.org/#terminology">XHTML 5.0</a>
     * conformance.
     *
     * @since Struts 1.4
     */
    public static final BigDecimal XHTML_5_0 = new BigDecimal("5.0");

    /**
     * The Singleton instance.
     * @since 1.3.5 Changed to non-final so it may be overridden, use at your own risk (you've been warned!!)
     */
    private static TagUtils instance = new TagUtils();

    /**
     * The {@code Log} instance for this class.
     */
    private final Logger log =
        LoggerFactory.getLogger(TagUtils.class);

    /**
     * The message resources for this package. TODO We need to move the
     * relevant MESSAGES out of this properties file.
     */
    private static final MessageResources MESSAGES =
        MessageResources.getMessageResources(
            "org.apache.struts.taglib.LocalStrings");

    /**
     * Constructor for TagUtils.
     */
    protected TagUtils() {
        super();
    }

    /**
     * Returns the Singleton instance of TagUtils.
     */
    public static TagUtils getInstance() {
        return instance;
    }

    /**
     * Set the instance.
     * This blatantly violates the Singleton pattern, but then some say Singletons are an anti-pattern.
     * @since 1.3.5 Changed to non-final and added setInstance() so TagUtils
     * may be overridden, use at your own risk (you've been warned!!)
     * @param instance The instance to set.
     */
    public static void setInstance(TagUtils instance) {
        TagUtils.instance = instance;
    }

    private TagMessageResolver messageResolver(PageContext pageContext) {
        return TagServices.service(pageContext, TagMessageResolver.class,
            TagMessageResolver::new);
    }

    private TagFormLocator formLocator(PageContext pageContext) {
        return TagServices.service(pageContext, TagFormLocator.class,
            TagFormLocator::new);
    }

    private TagUrlBuilder urlBuilder(PageContext pageContext) {
        return TagServices.service(pageContext, TagUrlBuilder.class,
            TagUrlBuilder::new);
    }

    private ErrorsMessagesRenderer errorsRenderer(PageContext pageContext) {
        return TagServices.service(pageContext, ErrorsMessagesRenderer.class,
            ErrorsMessagesRenderer::new);
    }

    /**
     * Compute a set of query parameters that will be dynamically added to a
     * generated URL.  The returned Map is keyed by parameter name, and the
     * values are either null (no value specified), a String (single value
     * specified), or a String[] array (multiple values specified).  Parameter
     * names correspond to the corresponding attributes of the
     * <code>&lt;html:link&gt;</code> tag.  If no query parameters are
     * identified, return <code>null</code>.
     */
    public Map<String, Object> computeParameters(PageContext pageContext, String paramId,
        String paramName, String paramProperty, String paramScope, String name,
        String property, String scope, boolean transaction)
        throws JspException {
        return urlBuilder(pageContext).computeParameters(pageContext,
            paramId, paramName, paramProperty, paramScope, name, property,
            scope, transaction);
    }

    public String computeURL(PageContext pageContext, String forward,
        String href, String page, String action, String module, Map<String, ?> params,
        String anchor, boolean redirect)
        throws MalformedURLException {
        return this.computeURLWithCharEncoding(pageContext, forward, href,
            page, action, module, params, anchor, redirect, false);
    }

    /**
     * Compute a hyperlink URL based on the <code>forward</code>,
     * <code>href</code>, <code>action</code> or <code>page</code> parameter
     * that is not null. The returned URL will have already been passed to
     * <code>response.encodeURL()</code> for adding a session identifier.
     */
    public String computeURLWithCharEncoding(PageContext pageContext,
        String forward, String href, String page, String action, String module,
        Map<String, ?> params, String anchor, boolean redirect, boolean useLocalEncoding)
        throws MalformedURLException {
        return computeURLWithCharEncoding(pageContext, forward, href, page,
            action, module, params, anchor, redirect, true, useLocalEncoding);
    }

    public String computeURL(PageContext pageContext, String forward,
        String href, String page, String action, String module, Map<String, ?> params,
        String anchor, boolean redirect, boolean encodeSeparator)
        throws MalformedURLException {
        return computeURLWithCharEncoding(pageContext, forward, href, page,
            action, module, params, anchor, redirect, encodeSeparator, false);
    }

    /**
     * Compute a hyperlink URL based on the <code>forward</code>,
     * <code>href</code>, <code>action</code> or <code>page</code> parameter
     * that is not null. The returned URL will have already been passed to
     * <code>response.encodeURL()</code> for adding a session identifier.
     */
    public String computeURLWithCharEncoding(PageContext pageContext,
        String forward, String href, String page, String action, String module,
        Map<String, ?> params, String anchor, boolean redirect, boolean encodeSeparator,
        boolean useLocalEncoding)
        throws MalformedURLException {
        return urlBuilder(pageContext).computeURLWithCharEncoding(
            pageContext, forward, href, page, action, module, params, anchor,
            redirect, encodeSeparator, useLocalEncoding);
    }

    /**
     * URLencodes a string assuming the character encoding is UTF-8.
     */
    public String encodeURL(String url) {
        return encodeURL(url, "UTF-8");
    }

    /**
     * URLencodes a string with the given character encoding.
     */
    public String encodeURL(String url, String enc) {
        return ResponseUtils.encodeURL(url, enc);
    }

    /**
     * Filter the specified string for characters that are sensitive to HTML
     * interpreters, returning the string with these characters replaced by
     * the corresponding character entities.
     */
    public String filter(String value) {
        return ResponseUtils.filter(value);
    }

    /**
     * Return the form action converted into an action mapping path.
     */
    public String getActionMappingName(String action) {
        return new TagUrlBuilder().getActionMappingName(action);
    }

    /**
     * Return the form action converted into a server-relative URL.
     */
    public String getActionMappingURL(String action, PageContext pageContext) {
        return urlBuilder(pageContext)
            .getActionMappingURL(action, pageContext);
    }

    /**
     * Return the form action converted into a server-relative URL.
     */
    public String getActionMappingURL(String action, String module,
        PageContext pageContext, boolean contextRelative) {
        return urlBuilder(pageContext).getActionMappingURL(action, module,
            pageContext, contextRelative);
    }

    /**
     * Retrieves the value from request scope and if it isn't already an
     * <code>ActionMessages</code>, some classes are converted to one.
     */
    public ActionMessages getActionMessages(PageContext pageContext,
        String paramName) throws JspException {
        return errorsRenderer(pageContext).getActionMessages(pageContext,
            paramName);
    }

    /**
     * Return the default ModuleConfig object if it exists, null if
     * otherwise.
     */
    public ModuleConfig getModuleConfig(PageContext pageContext) {
        return formLocator(pageContext).getModuleConfig(pageContext);
    }

    /**
     * Return the specified ModuleConfig object for the given prefix if it
     * exists, otherwise a NullPointerException will be thrown.
     */
    public ModuleConfig getModuleConfig(String module, PageContext pageContext) {
        return formLocator(pageContext).getModuleConfig(module, pageContext);
    }

    /**
     * Converts the scope name into its corresponding PageContext constant
     * value.
     */
    public int getScope(String scopeName)
        throws JspException {
        return new TagFormLocator().getScope(scopeName);
    }

    /**
     * Look up and return current user locale, based on the specified
     * parameters.
     */
    public Locale getUserLocale(PageContext pageContext, String locale) {
        return messageResolver(pageContext).getUserLocale(pageContext,
            locale);
    }

    /**
     * Returns true if the custom tags are in XHTML mode.
     */
    public boolean isXhtml(PageContext pageContext) {
        String xhtml;
        try {
            xhtml = (String) lookup(pageContext, Globals.XHTML_KEY, null);
            return "true".equalsIgnoreCase(xhtml);
        } catch (JspException e) {
            log.error("Failed xhtml lookup", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Locate and return the specified bean, from an optionally specified
     * scope, in the specified page context.  If no such bean is found, return
     * <code>null</code> instead.  If an exception is thrown, it will have
     * already been saved via a call to <code>saveException()</code>.
     */
    public Object lookup(PageContext pageContext, String name, String scopeName)
        throws JspException {
        return formLocator(pageContext).lookup(pageContext, name, scopeName);
    }

    /**
     * Locate and return the specified property of the specified bean, from an
     * optionally specified scope, in the specified page context.  If an
     * exception is thrown, it will have already been saved via a call to
     * <code>saveException()</code>.
     */
    public Object lookup(PageContext pageContext, String name, String property,
        String scope) throws JspException {
        return formLocator(pageContext).lookup(pageContext, name, property,
            scope);
    }

    /**
     * Look up and return a message string, based on the specified
     * parameters.
     */
    public String message(PageContext pageContext, String bundle,
        String locale, String key)
        throws JspException {
        return messageResolver(pageContext).message(pageContext, bundle,
            locale, key);
    }

    /**
     * Look up and return a message string, based on the specified
     * parameters.
     */
    public String message(PageContext pageContext, String bundle,
        String locale, String key, Object[] args)
        throws JspException {
        return messageResolver(pageContext).message(pageContext, bundle,
            locale, key, args);
    }

    /**
     * <p>Return the context-relative URL that corresponds to the specified
     * <code>page</code> attribute value, calculated based on the
     * <code>pagePattern</code> property of the current module's {@link
     * ModuleConfig}.</p>
     */
    public String pageURL(HttpServletRequest request, String page,
        ModuleConfig moduleConfig) {
        return new TagUrlBuilder().pageURL(request, page, moduleConfig);
    }

    /**
     * Return true if a message string for the specified message key is
     * present for the specified <code>Locale</code> and bundle.
     */
    public boolean present(PageContext pageContext, String bundle,
        String locale, String key)
        throws JspException {
        return messageResolver(pageContext).present(pageContext, bundle,
            locale, key);
    }

    /**
     * Returns the appropriate MessageResources object for the current module
     * and the given bundle.
     */
    public MessageResources retrieveMessageResources(PageContext pageContext,
        String bundle, boolean checkPageScope)
        throws JspException {
        return messageResolver(pageContext).retrieveMessageResources(
            pageContext, bundle, checkPageScope);
    }

    /**
     * Save the specified exception as a request attribute for later use.
     */
    public void saveException(PageContext pageContext, Throwable exception) {
        pageContext.setAttribute(Globals.EXCEPTION_KEY, exception,
            PageContext.REQUEST_SCOPE);
    }

    /**
     * Write the specified text as the response to the writer associated with
     * this page.  <strong>WARNING</strong> - If you are writing body content
     * from the <code>doAfterBody()</code> method of a custom tag class that
     * implements <code>BodyTag</code>, you should be calling
     * <code>writePrevious()</code> instead.
     */
    public void write(PageContext pageContext, String text)
        throws JspException {
        JspWriter writer = pageContext.getOut();

        try {
            writer.print(text);
        } catch (IOException e) {
            saveException(pageContext, e);
            throw new JspException(MESSAGES.getMessage("write.io", e.toString()), e);
        }
    }

    /**
     * Write the specified text as the response to the writer associated with
     * the body content for the tag within which we are currently nested.
     */
    public void writePrevious(PageContext pageContext, String text)
        throws JspException {
        JspWriter writer = pageContext.getOut();

        if (writer instanceof BodyContent) {
            writer = ((BodyContent) writer).getEnclosingWriter();
        }

        try {
            writer.print(text);
        } catch (IOException e) {
            saveException(pageContext, e);
            throw new JspException(MESSAGES.getMessage("write.io", e.toString()), e);
        }
    }
}
