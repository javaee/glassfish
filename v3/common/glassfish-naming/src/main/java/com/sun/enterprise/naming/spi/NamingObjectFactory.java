package com.sun.enterprise.naming.spi;

import org.jvnet.hk2.annotations.Contract;

import org.glassfish.api.naming.NamingObjectProxy;

import javax.naming.Context;
import javax.naming.NamingException;

@Contract
public interface NamingObjectFactory
    extends NamingObjectProxy {

    /**
     * Tells if the result of create() is cacheable. If so
     * the naming manager will replace this object factory with
     * the object itself.
     *
     * @return true if the result of create() can be cached
     */
    public boolean isCreateResultCacheable();

}
