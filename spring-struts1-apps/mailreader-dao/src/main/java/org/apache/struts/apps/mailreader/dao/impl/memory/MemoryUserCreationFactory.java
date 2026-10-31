package org.apache.struts.apps.mailreader.dao.impl.memory;

import org.apache.commons.digester.Digester;
import org.apache.commons.digester.ObjectCreationFactory;
import org.apache.struts.apps.mailreader.dao.User;
import org.xml.sax.Attributes;



/**
 * Digester object creation factory for user instances.
 */
class MemoryUserCreationFactory implements ObjectCreationFactory {
    // Digester3: User

    MemoryUserCreationFactory(MemoryUserDatabase database) {
        this.database = database;
    }

    private MemoryUserDatabase database = null;

    private Digester digester = null;

    public Digester getDigester() {
        return (this.digester);
    }

    public void setDigester(Digester digester) {
        this.digester = digester;
    }

    public User createObject(Attributes attributes) {
        String username = attributes.getValue("username");
        User user = database.createUser(username);
        user.setFromAddress(attributes.getValue("fromAddress"));
        user.setFullName(attributes.getValue("fullName"));
        user.setPassword(attributes.getValue("password"));
        user.setReplyToAddress(attributes.getValue("replyToAddress"));
        return (user);
    }
}
