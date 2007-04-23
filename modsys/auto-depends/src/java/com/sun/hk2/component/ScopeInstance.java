package com.sun.hk2.component;

import com.sun.hk2.utils.InMemoryResourceManager;
import org.jvnet.hk2.component.ResourceManager;

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
public class ScopeInstance {
    /**
     * Human readable scope instance name for debug assistance. 
     */
    public final String name;
    /**
     * Stores component instances bound to this scope.
     */
    public final ResourceManager store;

    public ScopeInstance(String name) {
        this.name = name;
        this.store = new InMemoryResourceManager();
    }

    public ScopeInstance(String name, ResourceManager resourceManager) {
        this.name = name;
        this.store = resourceManager;
    }

    public ScopeInstance() {
        this.name = super.toString();
        this.store = new InMemoryResourceManager();
    }

    public String toString() {
        return name;
    }
}
