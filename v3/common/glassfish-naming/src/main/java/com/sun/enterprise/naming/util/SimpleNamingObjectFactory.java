package com.sun.enterprise.naming.util;

import com.sun.enterprise.naming.spi.NamingObjectFactory;
import org.jvnet.hk2.annotations.Service;

import javax.naming.Context;

@Service
public class SimpleNamingObjectFactory
    implements NamingObjectFactory {
    
    private String name;

    private Object value;

    public SimpleNamingObjectFactory(String name, Object value) {
        this.name = name;
        this.value = value;
    }

    public boolean isCreateResultCacheable() {
        return true;
    }

    public Object create(Context ic) {
        return value;
    }
}
