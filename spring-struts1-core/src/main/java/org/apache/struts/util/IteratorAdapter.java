package org.apache.struts.util;

import java.util.Enumeration;
import java.util.Iterator;

/**
 * An <code>Iterator</code> adapter for <code>Enumeration</code> objects.
 *
 * <p>Ported from weblegacy/struts1 1.5.x (org.apache.struts.util.IteratorAdapter).</p>
 */
public class IteratorAdapter<E> implements Iterator<E> {

    private Enumeration<E> enumeration;

    public IteratorAdapter(Enumeration<E> enumeration) {
        this.enumeration = enumeration;
    }

    @Override
    public boolean hasNext() {
        return enumeration.hasMoreElements();
    }

    @Override
    public E next() {
        return enumeration.nextElement();
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException(
            "Method remove() not supported");
    }
}
