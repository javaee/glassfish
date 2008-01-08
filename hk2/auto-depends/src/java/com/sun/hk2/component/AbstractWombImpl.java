package com.sun.hk2.component;

import org.jvnet.hk2.component.ComponentException;
import org.jvnet.hk2.component.MultiMap;
import org.jvnet.hk2.component.Womb;
import org.jvnet.hk2.component.InjectionManager;
import org.jvnet.hk2.component.PostConstruct;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.annotations.Inject;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Array;
import java.util.Collection;

/**
 * @author Kohsuke Kawaguchi
 */
public abstract class AbstractWombImpl<T> implements Womb<T> {
    protected final Class<T> type;
    private final MultiMap<String,String> metadata;

    public AbstractWombImpl(Class<T> type, MultiMap<String,String> metadata) {
        this.type = type;
        this.metadata = metadata;
    }

    public final String typeName() {
        return type.getName();
    }

    public final Class<T> type() {
        return type;
    }

    public final T get() throws ComponentException {
        T o = create();
        initialize(o);
        return o;
    }

    public void initialize(T t) throws ComponentException {
        // default is no-op
    }

    public void release() {
        // Womb creates a new instance every time,
        // so there's nothing to release here.
    }

    public MultiMap<String, String> metadata() {
        return metadata;
    }

    /**
     * Performs resource injection on the given instance from the given habitat.
     *
     * <p>
     * This method is an utility method for subclasses for performing injection.
     */
    protected void inject(final Habitat habitat, T t) {
        (new InjectionManager<Inject>() {
            public boolean isOptional(Inject annotation) {
                return annotation.optional();
            }

            /**
             * Obtains the value to inject, based on the type and {@link Inject} annotation.
             */
            @SuppressWarnings("unchecked")
            protected Object getValue(AnnotatedElement target, Class type) throws ComponentException {
                if (type.isArray()) {
                    Class<?> ct = type.getComponentType();

                    Collection instances;
                    if(habitat.isContract(ct))
                        instances = habitat.getAllByContract(ct);
                    else
                        instances = habitat.getAllByType(ct);
                    return instances.toArray((Object[]) Array.newInstance(ct, instances.size()));
                } else {
                    if(habitat.isContract(type))
                        // service lookup injection
                        return habitat.getComponent(type, target.getAnnotation(Inject.class).name());

                    // ideally we should check if type has @Service or @Configured

                    // component injection
                    return habitat.getByType(type);
                }
            }
        }).inject(t, Inject.class);

        // postContruct call if any
        if(t instanceof PostConstruct)
            ((PostConstruct)t).postConstruct();
    }
}
