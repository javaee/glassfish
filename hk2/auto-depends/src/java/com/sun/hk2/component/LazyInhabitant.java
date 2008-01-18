package com.sun.hk2.component;

import org.jvnet.hk2.component.ComponentException;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.component.Inhabitant;
import org.jvnet.hk2.component.Inhabitants;
import org.jvnet.hk2.component.MultiMap;
import org.jvnet.hk2.component.Womb;
import org.jvnet.hk2.component.Wombs;

/**
 * @author Kohsuke Kawaguchi
 */
public class LazyInhabitant<T> extends AbstractInhabitantImpl<T> {
    private final String typeName;
    /**
     * Real {@link Inhabitant} object. Lazily created.
     */
    private volatile Inhabitant<T> real;
    /**
     * Lazy reference to {@link ClassLoader}.
     */
    private final Holder<ClassLoader> classLoader;

    protected final Habitat habitat;

    private final MultiMap<String,String> metadata;

    public LazyInhabitant(Habitat habitat, Holder<ClassLoader> cl, String typeName, MultiMap<String,String> metadata) {
        assert metadata!=null;
        this.habitat = habitat;
        this.classLoader = cl;
        this.typeName = typeName;
        this.metadata = metadata;
    }

    public String typeName() {
        return typeName;
    }

    public Class<T> type() {
        fetch();
        return real.type();
    }

    public MultiMap<String,String> metadata() {
        return metadata;
    }

    @SuppressWarnings("unchecked")
    private void fetch() {
        if(real!=null)  return;

        try {
            Class<T> c = (Class<T>) classLoader.get().loadClass(typeName);
            real = Inhabitants.wrapByScope(c,createWomb(c),habitat);
        } catch (ClassNotFoundException e) {
            throw new ComponentException("Failed to load "+typeName+" from "+classLoader,e);
        }
    }

    /**
     * Creates {@link Womb} for instantiating objects.
     */
    protected Womb<T> createWomb(Class<T> c) {
        return Wombs.create(c,habitat,metadata);
    }

    public T get(Inhabitant onBehalfOf) throws ComponentException {
        fetch();
        return real.get(onBehalfOf);
    }

    public void release() {
        if(real!=null)
            real.release();
    }
}
