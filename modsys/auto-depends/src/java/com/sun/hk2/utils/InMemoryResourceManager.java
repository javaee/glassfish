package com.sun.hk2.utils;

import org.jvnet.hk2.component.ResourceLocator;
import org.jvnet.hk2.component.ResourceManager;

import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

/**
 * {@link ResourceManager} implementation that stores values in memory
 * by using {@link ConcurrentHashMap}.
 * 
 * @author Kohsuke Kawaguchi
 */
public final class InMemoryResourceManager implements ResourceManager {
    // give a hint to JIT that they can inline methods.
    private final ConcurrentHashMap<ResourceLocator,Object> components = new ConcurrentHashMap<ResourceLocator,Object>();

    public <T> void add(ResourceLocator<T> resourceInfo, T value) {
        components.put(resourceInfo,value);
    }

    @SuppressWarnings("unchecked")
    public <T> T lookup(ResourceLocator<T> resourceInfo) {
        Object value = components.get(resourceInfo);
        assert resourceInfo.getType().isInstance(value);
        return (T) value;
    }

    // TODO: consider creating a map if this needs to be faster
    @SuppressWarnings("unchecked")
    public <T> Collection<T> lookupAll(Class<T> type) {
        List r = new ArrayList();
        for (Entry<ResourceLocator,Object> e : components.entrySet()) {
            if(e.getKey().getType()==type)
                r.add(e.getValue());
        }

        return r;
    }

    public void remove(Object value) {
        components.values().remove(value);
    }
}
