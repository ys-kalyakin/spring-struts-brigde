package org.apache.struts.extras.actions;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.struts.Globals;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.config.MessageResourcesConfig;
import org.apache.struts.config.ModuleConfig;
import org.apache.struts.util.MessageResources;

/**
 * <p>An abstract <strong>Action</strong> that dispatches to the subclass
 * mapped <code>execute</code> method. This is useful in cases where an HTML
 * form has multiple submit buttons with the same name. The button name is
 * specified by the <code>parameter</code> property of the corresponding
 * ActionMapping. To configure the use of this action in your
 * <code>struts-config.xml</code> file, create an entry like this:</p>
 *
 * <pre>
 *   &lt;action path="/test"
 *           type="org.example.MyAction"
 *           name="MyForm"
 *          scope="request"
 *          input="/test.jsp"
 *      parameter="method"/&gt;
 * </pre>
 *
 * <p>which will use the value of the request parameter named "method" to
 * locate the corresponding key in ApplicationResources. For example, you
 * might have the following ApplicationResources.properties:</p>
 *
 * <pre>
 *    button.add=Add Record
 *    button.delete=Delete Record
 * </pre>
 *
 * <p>Your subclass must implement both getKeyMethodMap and the methods
 * defined in the map.</p>
 *
 * <p><strong>Notes</strong> - If duplicate values exist for the keys
 * returned by getKeys, only the first one found will be returned. If no
 * corresponding key is found then an exception will be thrown. You can
 * override the method <code>unspecified</code> to provide a custom handler.
 * If the submit was cancelled (a <code>html:cancel</code> button was
 * pressed), the custom handler <code>cancelled</code> will be used
 * instead.</p>
 *
 * <p>Ported to the spring-struts-bridge engine with the classic Struts 1.3
 * API.</p>
 */
public abstract class LookupDispatchAction extends DispatchAction {

    private static final Logger log =
        LoggerFactory.getLogger(LookupDispatchAction.class);

    /**
     * Reverse lookup map from resource value to resource key. A concurrent
     * map: locale lookups do not serialize concurrent requests to the same
     * action.
     */
    protected Map<Locale, Map<String, String>> localeMap =
        new ConcurrentHashMap<>();

    /**
     * Resource key to method name lookup.
     */
    protected Map<String, String> keyMethodMap = null;

    /**
     * This is the first time this Locale is used so build the reverse
     * lookup Map. Search for message keys in all configured
     * MessageResources for the current module.
     *
     * @param request    The HTTP request we are processing
     * @param userLocale The locale for this request
     * @return The reverse lookup map for the specified locale.
     */
    private Map<String, String> initLookupMap(HttpServletRequest request,
        Locale userLocale) {
        Map<String, String> lookupMap = new HashMap<>();

        this.keyMethodMap = this.getKeyMethodMap();

        ModuleConfig moduleConfig =
            (ModuleConfig) request.getAttribute(Globals.MODULE_KEY);

        MessageResourcesConfig[] mrc =
            moduleConfig.findMessageResourcesConfigs();

        // Look through all module's MessageResources
        for (int i = 0; i < mrc.length; i++) {
            MessageResources resources =
                this.getResources(request, mrc[i].getKey());

            if (resources == null) {
                continue;
            }

            // Look for key in MessageResources
            for (String key : this.keyMethodMap.keySet()) {
                String text = resources.getMessage(userLocale, key);

                // Found key and haven't added to Map yet, so add the text
                if ((text != null) && !lookupMap.containsKey(text)) {
                    lookupMap.put(text, key);
                }
            }
        }

        return lookupMap;
    }

    /**
     * Provides the mapping from resource key to method name.
     *
     * @return Resource key / method name map.
     */
    protected abstract Map<String, String> getKeyMethodMap();

    /**
     * Lookup the method name corresponding to the client request's locale.
     *
     * @param request The HTTP request we are processing
     * @param keyName The parameter name to use as the properties key
     * @param mapping The ActionMapping used to select this instance
     * @return The method's localized name.
     * @throws ServletException if keyName cannot be resolved
     */
    protected String getLookupMapName(HttpServletRequest request,
        String keyName, ActionMapping mapping) throws ServletException {
        // Based on this request's Locale get the lookupMap
        Map<String, String> lookupMap =
            this.localeMap.computeIfAbsent(this.getLocale(request),
                userLocale -> this.initLookupMap(request, userLocale));

        // Find the key for the resource
        String key = lookupMap.get(keyName);

        if (key == null) {
            String message =
                messages.getMessage("dispatch.resource", mapping.getPath());

            log.error("{} '{}'", message, keyName);
            throw new ServletException(message);
        }

        // Find the method name
        String methodName = keyMethodMap.get(key);

        if (methodName == null) {
            String message =
                messages.getMessage("dispatch.lookup", mapping.getPath(),
                    key);

            throw new ServletException(message);
        }

        return methodName;
    }

    /**
     * Returns the method name, given a parameter's value: the value of the
     * request parameter (typically the label of the pressed submit button)
     * is translated through the message resources bundle.
     */
    @Override
    protected String getMethodName(ActionMapping mapping, ActionForm form,
        HttpServletRequest request, HttpServletResponse response,
        String parameter) throws Exception {
        // Identify the method name to be dispatched to.
        // dispatchMethod() will call unspecified() if name is null
        String keyName = request.getParameter(parameter);

        if ((keyName == null) || (keyName.length() == 0)) {
            return null;
        }

        String methodName = getLookupMapName(request, keyName, mapping);

        return methodName;
    }
}
