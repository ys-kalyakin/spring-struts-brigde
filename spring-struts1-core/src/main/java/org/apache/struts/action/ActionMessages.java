package org.apache.struts.action;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * A class that encapsulates messages. Messages can be either global or they
 * are specific to a particular bean property.
 *
 * <p>Ported from weblegacy/struts1 1.5.x (org.apache.struts.action.ActionMessages).</p>
 */
public class ActionMessages implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * The "property name" marker to use for global messages, as opposed to
     * those related to a specific bean property.
     */
    public static final String GLOBAL_MESSAGE =
        "org.apache.struts.action.GLOBAL_MESSAGE";

    /**
     * The accumulated set of <code>ActionMessage</code> objects (represented
     * as an ArrayList) for each property, keyed by property name.
     */
    protected Map<String, List<ActionMessage>> messages =
        new HashMap<>();

    /**
     * The current number of this component being used?
     */
    protected int count = 0;

    /**
     * Have the messages been retrieved from this object?
     */
    protected boolean accessed = false;

    /**
     * Add a message to the set of messages for the specified property.
     */
    public void add(String property, ActionMessage message) {
        List<ActionMessage> list = messages.computeIfAbsent(property,
            k -> Collections.synchronizedList(new ArrayList<>()));
        list.add(message);
        count++;
    }

    /**
     * Adds the messages from the given <code>ActionMessages</code> object to
     * this set of messages.
     */
    public void add(ActionMessages messages) {
        for (String property : messages.messages.keySet()) {
            for (ActionMessage message : messages.messages.get(property)) {
                add(property, message);
            }
        }
    }

    /**
     * Clear all messages recorded by this object.
     */
    public void clear() {
        messages.clear();
        count = 0;
        accessed = false;
    }

    /**
     * Return the set of all recorded messages, regardless of property.
     */
    public Iterator<ActionMessage> get() {
        accessed = true;
        List<ActionMessage> all = new ArrayList<>(count);
        for (List<ActionMessage> list : messages.values()) {
            all.addAll(list);
        }
        return all.iterator();
    }

    /**
     * Return the set of messages related to a specific property.
     */
    public Iterator<ActionMessage> get(String property) {
        accessed = true;
        List<ActionMessage> list = messages.get(property);
        return (list == null) ? Collections.<ActionMessage>emptyList()
            .iterator() : list.iterator();
    }

    /**
     * Return <code>true</code> if there are no messages recorded in this
     * collection.
     */
    public boolean isEmpty() {
        return count == 0;
    }

    /**
     * Return the set of property names for which at least one message has
     * been recorded.
     */
    public Iterator<String> properties() {
        accessed = true;
        return new ArrayList<>(messages.keySet()).iterator();
    }

    /**
     * Return the number of messages recorded for all properties.
     */
    public int size() {
        return count;
    }

    /**
     * Return the number of messages associated with the specified property.
     */
    public int size(String property) {
        List<ActionMessage> list = messages.get(property);
        return (list == null) ? 0 : list.size();
    }

    /**
     * Indicates if the messages have been accessed.
     */
    public boolean isAccessed() {
        return accessed;
    }

    @Override
    public String toString() {
        return "ActionMessages[count=" + count + "]";
    }
}
