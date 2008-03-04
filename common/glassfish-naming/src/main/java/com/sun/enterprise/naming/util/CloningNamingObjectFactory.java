package com.sun.enterprise.naming.util;

import com.sun.enterprise.naming.spi.NamingObjectFactory;
import com.sun.enterprise.naming.spi.NamingUtils;

import org.jvnet.hk2.annotations.Service;

import javax.naming.Context;
import javax.naming.NamingException;

@Service
public class CloningNamingObjectFactory
        implements NamingObjectFactory {

    private static NamingUtils namingUtils = new NamingUtilsImpl();

    private String name;

    private Object value;

    private NamingObjectFactory delegate;

    public CloningNamingObjectFactory(String name, Object value) {
        this.name = name;
        this.value = value;
    }

    public CloningNamingObjectFactory(String name, NamingObjectFactory delegate) {
        this.name = name;
        this.delegate = delegate;
    }

    public boolean isCreateResultCacheable() {
        return false;
    }

    public Object create(Context ic)
            throws NamingException {
        return (delegate != null)
                ? namingUtils.makeCopyOfObject(delegate.create(ic))
                : namingUtils.makeCopyOfObject(value);
    }
}