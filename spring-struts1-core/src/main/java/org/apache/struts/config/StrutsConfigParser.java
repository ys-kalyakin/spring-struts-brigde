package org.apache.struts.config;

import java.net.URL;

import jakarta.servlet.ServletException;

import org.apache.commons.digester.Digester;

/**
 * Parses a single <code>struts-config</code> resource into a
 * {@link ModuleConfig}. Extracted from the <code>ActionServlet</code> facade
 * so the engine registrar — which runs earlier, during Spring's bean
 * definition registration phase — reads the configuration with exactly the
 * same DTD set and parsing rules as the facade.
 */
public final class StrutsConfigParser {

    /**
     * The set of registered public identifiers and corresponding local
     * resource names of the configuration DTDs we know about.
     */
    private static final String[] REGISTRATIONS = {
        "-//Apache Software Foundation//DTD Struts Configuration 1.0//EN",
        "/org/apache/struts/resources/struts-config_1_0.dtd",
        "-//Apache Software Foundation//DTD Struts Configuration 1.1//EN",
        "/org/apache/struts/resources/struts-config_1_1.dtd",
        "-//Apache Software Foundation//DTD Struts Configuration 1.2//EN",
        "/org/apache/struts/resources/struts-config_1_2.dtd",
        "-//Apache Software Foundation//DTD Struts Configuration 1.3//EN",
        "/org/apache/struts/resources/struts-config_1_3.dtd",
        "-//Apache Software Foundation//DTD Struts Configuration 1.4//EN",
        "/org/apache/struts/resources/struts-config_1_4.dtd"
    };

    private StrutsConfigParser() {
    }

    /**
     * Parse the configuration resource at the given URL into the module
     * configuration. A missing or unparsable file fails with the resource
     * named in the error.
     */
    public static void parse(URL url, ModuleConfig mconfig,
        boolean validating) throws ServletException {
        Digester digester = new Digester();
        digester.push(mconfig);
        digester.setClassLoader(Thread.currentThread()
            .getContextClassLoader());
        digester.setValidating(validating);
        digester.setNamespaceAware(true);
        digester.addRuleSet(new ConfigRuleSet());
        for (int i = 0; i < REGISTRATIONS.length; i += 2) {
            URL dtdUrl = StrutsConfigParser.class
                .getResource(REGISTRATIONS[i + 1]);
            if (dtdUrl != null) {
                digester.register(REGISTRATIONS[i], dtdUrl.toString());
            }
        }
        try {
            digester.parse(url);
        } catch (Exception e) {
            throw new ServletException(
                "Cannot parse configuration resource " + url, e);
        }
    }
}
