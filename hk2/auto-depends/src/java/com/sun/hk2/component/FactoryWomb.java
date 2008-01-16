package com.sun.hk2.component;

import org.jvnet.hk2.component.ComponentException;
import org.jvnet.hk2.component.Factory;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.component.Inhabitant;
import org.jvnet.hk2.component.MultiMap;

/**
 * Creates an object from {@link Factory}.
 *
 * @author Kohsuke Kawaguchi
 */
public class FactoryWomb<T> extends AbstractWombImpl<T> {
    private final Inhabitant<? extends Factory> factory;
    private final Habitat habitat;

    public FactoryWomb(Class<T> type, Class<? extends Factory> factory, Habitat habitat, MultiMap<String,String> metadata) {
        this(type,habitat.getInhabitantByType(factory),habitat,metadata);
    }

    public FactoryWomb(Class<T> type, Inhabitant<? extends Factory> factory, Habitat habitat, MultiMap<String,String> metadata) {
        super(type,metadata);
        assert factory!=null;
        assert habitat!=null;
        this.factory = factory;
        this.habitat = habitat;
    }

    public T create() throws ComponentException {
        T t = type.cast(factory.get().getObject());
        inject(habitat,t);
        return t;
    }
}
