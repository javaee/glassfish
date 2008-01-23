package org.glassfish.api.naming;

import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.PerLookup;

import javax.naming.Context;
import javax.naming.NamingException;

@Service
@Scoped(PerLookup.class)
public class NamingObjectProxy
        {

    private String name;

    private String jndiName;

    private boolean cacheResult;

    private Object value;

    public NamingObjectProxy(String name, String jndiName,
                             boolean cacheResult) {
        this.name = name;
        this.jndiName = jndiName;
        this.cacheResult = cacheResult;
    }

    public boolean isCreateResultCacheable() {
        return true;
    }

    public Object create(Context ic)
            throws NamingException {
        Object result = null;
        if (cacheResult && value != null) {
            result = value;
        }

        if (result == null) {
            result = ic.lookup(jndiName);
            if (cacheResult) {
                value = result;
            }
        }

        return result;
    }

}

