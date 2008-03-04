package com.sun.enterprise.container.common.impl;

import com.sun.enterprise.container.common.spi.util.ComponentEnvManager;
import com.sun.enterprise.naming.spi.NamingObjectFactory;
import org.glassfish.api.invocation.InvocationManager;

import javax.naming.Context;
import javax.naming.NamingException;


public class FactoryForEntityManagerFactoryWrapper
    implements NamingObjectFactory {

    InvocationManager invMgr;

    ComponentEnvManager compEnvMgr;

    private String unitName;

    public FactoryForEntityManagerFactoryWrapper(String unitName,
        InvocationManager invMgr, ComponentEnvManager compEnvMgr) {
        this.unitName = unitName;
        this.invMgr = invMgr;
        this.compEnvMgr = compEnvMgr;
    }

    public boolean isCreateResultCacheable() {
        return false;
    }

    public Object create(Context ic)
        throws NamingException {

        return new EntityManagerFactoryWrapper(unitName);
    }

}
