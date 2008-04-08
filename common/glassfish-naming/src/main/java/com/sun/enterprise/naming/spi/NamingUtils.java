package com.sun.enterprise.naming.spi;

import org.jvnet.hk2.annotations.Contract;

import java.io.OutputStream;

@Contract
public interface NamingUtils {

    public NamingObjectFactory createSimpleNamingObjectFactory(String name, Object value);

    public NamingObjectFactory createLazyNamingObjectFactory(String name, String jndiName,
        boolean cacheResult);

    public NamingObjectFactory createCloningNamingObjectFactory(String name, Object value);

    public NamingObjectFactory createCloningNamingObjectFactory(String name,
        NamingObjectFactory delegate);

    public NamingObjectFactory createDelegatingNamingObjectFactory(String name,
        NamingObjectFactory delegate, boolean cacheResult);

    public Object makeCopyOfObject(Object obj);

    public OutputStream getMailLogOutputStream();

}
