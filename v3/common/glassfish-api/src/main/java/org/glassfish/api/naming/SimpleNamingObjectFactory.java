package org.glassfish.api.naming;

import org.glassfish.api.naming.NamingObjectFactory;
import org.glassfish.api.naming.NamingUtils;
import org.jvnet.hk2.annotations.Contract;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.component.PerLookup;

import javax.naming.Context;

@Contract
@Scoped(PerLookup.class)
public class SimpleNamingObjectFactory
        implements NamingObjectFactory {

    @Inject
    NamingUtils namingUtils;
    
    private String name;

    private Object value;

    private boolean cloneOnCreate;

    public SimpleNamingObjectFactory(String name, Object value) {
        this(name, value, false);
    }

    public SimpleNamingObjectFactory(String name, Object value, boolean cloneOnCreate) {
        this.name = name;
        this.value = value;
        this.cloneOnCreate = cloneOnCreate;
    }

    public boolean isCreateResultCacheable() {
        return true;
    }

    public Object create(Context ic) {
        return (cloneOnCreate)
                ? namingUtils.makeCopyOfObject(value)
                : value;
    }
}
