package org.apache.struts.config.impl;

import org.apache.struts.config.ModuleConfig;
import org.apache.struts.config.ModuleConfigFactory;

/**
 * A factory for creating {@link ModuleConfig} instances
 * ({@link ModuleConfigImpl}).
 *
 * <p>Ported from weblegacy/struts1 1.5.x
 * (org.apache.struts.config.impl.DefaultModuleConfigFactory).</p>
 */
public class DefaultModuleConfigFactory extends ModuleConfigFactory {

    @Override
    public ModuleConfig createModuleConfig(String prefix) {
        return new ModuleConfigImpl(prefix);
    }
}
