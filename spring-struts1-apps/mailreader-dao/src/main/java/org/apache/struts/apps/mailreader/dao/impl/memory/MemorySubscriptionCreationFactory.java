package org.apache.struts.apps.mailreader.dao.impl.memory;

import org.apache.commons.digester.Digester;
import org.apache.commons.digester.ObjectCreationFactory;
import org.apache.struts.apps.mailreader.dao.Subscription;
import org.apache.struts.apps.mailreader.dao.User;
import org.xml.sax.Attributes;



/**
 * Digester object creation factory for subscription instances.
 */
class MemorySubscriptionCreationFactory implements ObjectCreationFactory {
    // Digester3: Subscription

    private Digester digester = null;

    public Digester getDigester() {
        return (this.digester);
    }

    public void setDigester(Digester digester) {
        this.digester = digester;
    }

    public Subscription createObject(Attributes attributes) {
        String host = attributes.getValue("host");
        User user = (User) digester.peek();
        Subscription subscription = user.createSubscription(host);
        String autoConnect = attributes.getValue("autoConnect");
        if (autoConnect == null) {
            autoConnect = "false";
        }
        if ("true".equalsIgnoreCase(autoConnect) ||
            "yes".equalsIgnoreCase(autoConnect)) {
            subscription.setAutoConnect(true);
        } else {
            subscription.setAutoConnect(false);
        }
        subscription.setPassword(attributes.getValue("password"));
        subscription.setType(attributes.getValue("type"));
        subscription.setUsername(attributes.getValue("username"));
        return (subscription);
    }

}
