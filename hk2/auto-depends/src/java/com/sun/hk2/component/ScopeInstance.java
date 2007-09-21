package com.sun.hk2.component;

import org.jvnet.hk2.component.Inhabitant;
import org.jvnet.hk2.component.PreDestroy;

import java.util.Map;

/**
 * A particular instanciation of a {@link org.jvnet.hk2.component.Scope}.
 *
 * <p>
 * For example, for the "request scope", an instance
 * of {@link ScopeInstance} is created for each request.
 * 
 * @author Kohsuke Kawaguchi
 * @see org.jvnet.hk2.component.Scope#current()
 */
public final class ScopeInstance implements PreDestroy {
    /**
     * Human readable scope instance name for debug assistance. 
     */
    public final String name;

    private final Map backend;

    public ScopeInstance(String name, Map backend) {
        this.name = name;
        this.backend = backend;
    }

    public ScopeInstance(Map backend) {
        this.name = super.toString();
        this.backend = backend;
    }
    
    public String toString() {
        return name;
    }

    public <T> T get(Inhabitant<T> inhabitant) {
        return (T) backend.get(inhabitant);
    }

    public <T> T put(Inhabitant<T> inhabitant, T value) {
        return (T) backend.put(inhabitant,value);
    }

    public void release() {
        synchronized(backend) {
            for (Object o : backend.values()) {
                if(o instanceof PreDestroy)
                    ((PreDestroy)o).preDestroy();
            }
            backend.clear();
        }
    }

    public void preDestroy() {
        release();
    }
}
