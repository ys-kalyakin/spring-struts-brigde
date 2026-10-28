package org.apache.struts.util;

/**
 * Concrete factory for {@link PropertyMessageResources} instances backed by
 * a Spring <code>MessageSource</code>.
 *
 * <p>Ported from weblegacy/struts1 1.5.x
 * (org.apache.struts.util.PropertyMessageResourcesFactory).</p>
 */
public class PropertyMessageResourcesFactory extends MessageResourcesFactory {

    private static final long serialVersionUID = 1L;

    @Override
    public MessageResources createResources(String config) {
        return new PropertyMessageResources(this, config, this.returnNull);
    }
}
