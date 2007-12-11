package com.sun.enterprise.naming.util;

import com.sun.enterprise.naming.spi.NamingObjectFactory;
import org.jvnet.hk2.annotations.Service;

import javax.naming.Context;
import javax.naming.NamingException;

@Service
public class JndiNamingObjectFactory
    implements NamingObjectFactory {

    private String name;

    private String jndiName;

    private transient Object value;

    private boolean cacheResult;

    public JndiNamingObjectFactory(String name, String jndiName, boolean cacheResult) {
        this.name = name;
        this.jndiName = jndiName;
        this.cacheResult = cacheResult;
    }

    public boolean isCreateResultCacheable() {
        return cacheResult;
    }

    public Object create(Context ic)
            throws NamingException {
        Object result = null;
        if (cacheResult) {
            if (value == null) {
                synchronized (this) {
                    if (value == null) {
                        value =ic.lookup(jndiName);
                    }
                }
            } else {
                result = value;
            }
        } else {
            result = ic.lookup(jndiName);
        }

        return result;
    }

}