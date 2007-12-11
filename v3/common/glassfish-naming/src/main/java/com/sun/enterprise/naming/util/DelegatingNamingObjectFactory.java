package com.sun.enterprise.naming.util;

import com.sun.enterprise.naming.spi.NamingObjectFactory;

import org.jvnet.hk2.annotations.Service;

import javax.naming.Context;
import javax.naming.NamingException;

@Service
public class DelegatingNamingObjectFactory
    implements NamingObjectFactory {

    private String name;

    private transient Object value;

    private boolean cacheResult;

    private NamingObjectFactory delegate;


    public DelegatingNamingObjectFactory(String name, NamingObjectFactory delegate, boolean cacheResult) {
        this.name = name;
        this.delegate = delegate;
        this.cacheResult = cacheResult;
    }

    public boolean isCreateResultCacheable() {
        return cacheResult;
    }

    public Object create(Context ic)
        throws NamingException {
        Object result = value;
        if (cacheResult) {
            if (value == null) {
                synchronized (this) {
                    if (value == null) {
                        value = delegate.create(ic);
                    }
                }
            } else {
                result = value;
            }
        } else {
            result = delegate.create(ic);
        }

        return result;
    }
}