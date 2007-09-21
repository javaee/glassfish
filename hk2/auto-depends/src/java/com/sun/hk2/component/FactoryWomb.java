package com.sun.hk2.component;

import org.jvnet.hk2.component.ComponentException;
import org.jvnet.hk2.component.Factory;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.component.MultiMap;

/**
 * Creates an object from {@link Factory}.
 *
 * @author Kohsuke Kawaguchi
 */
public class FactoryWomb<T> extends AbstractWombImpl<T> {
    private final Class<? extends Factory> factory;
    private final Habitat habitat;

    public FactoryWomb(Class<T> type, Class<? extends Factory> factory, Habitat habitat, MultiMap<String,String> metadata) {
        super(type,metadata);
        assert factory!=null;
        assert habitat!=null;
        this.factory = factory;
        this.habitat = habitat;
    }

    public T create() throws ComponentException {
        Factory f = habitat.getByType(factory);
        if(f==null)
           throw new ComponentException("Failed to look up %s for creating %s",f,type);
        return (T) f.getObject();
    }
}
