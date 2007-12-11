package com.sun.enterprise.naming.impl;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.NameClassPair;
import javax.naming.OperationNotSupportedException;
import java.util.Iterator;

/**
 * Created by IntelliJ IDEA.
 * User: maheshk
 * Date: Dec 7, 2007
 * Time: 1:15:30 AM
 * To change this template use File | Settings | File Templates.
 */ // Class for enumerating name/class pairs
class NamePairsEnum implements NamingEnumeration {
    GlassfishNamingManagerImpl nm;

    Iterator names;

    NamePairsEnum(GlassfishNamingManagerImpl nm, Iterator names) {
        this.nm = nm;
        this.names = names;
    }

    public boolean hasMoreElements() {
        return names.hasNext();
    }

    public boolean hasMore() throws NamingException {
        return hasMoreElements();
    }

    public Object nextElement() {
        if (names.hasNext()) {
            try {
                String name = (String) names.next();
                String className = nm.lookup(name).getClass().getName();
                return new NameClassPair(name, className);
            } catch (Exception ex) {
                throw new RuntimeException("Exception during lookup: " + ex);
            }
        } else
            return null;
    }

    public Object next() throws NamingException {
        return nextElement();
    }

    // New API for JNDI 1.2
    public void close() throws NamingException {
        throw new OperationNotSupportedException("close() not implemented");
    }
}
