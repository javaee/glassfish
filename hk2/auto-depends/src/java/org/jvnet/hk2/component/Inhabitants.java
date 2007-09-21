package org.jvnet.hk2.component;

import com.sun.hk2.component.ScopedInhabitant;
import com.sun.hk2.component.SingletonInhabitant;
import org.jvnet.hk2.annotations.Scoped;

/**
 * Factory for {@link Inhabitant}.
 * @author Kohsuke Kawaguchi
 */
public class Inhabitants {
    /**
     * Creates a {@link Inhabitant} by looking at annotations of the given type.
     */
    public static <T> Inhabitant<T> create(Class<T> c, Habitat habitat, MultiMap<String,String> metadata) {
        return wrapByScope(c, Wombs.create(c,habitat,metadata), habitat);
    }

    /**
     * Creates a {@link Inhabitant} by wrapping {@link Womb} to handle scoping right.
     */
    public static <T> Inhabitant<T> wrapByScope(Class<T> c, Womb<T> womb, Habitat habitat) {
        Scoped scoped = c.getAnnotation(Scoped.class);
        if(scoped==null)
            return new SingletonInhabitant<T>(womb); // treated as singleton

        Class<? extends Scope> scopeClass = scoped.value();

        // those two scopes are so common and different that they deserve
        // specialized code optimized for them.
        if(scopeClass== PerLookup.class)
            return womb;
        if(scopeClass== Singleton.class)
            return new SingletonInhabitant<T>(womb);

        // other general case
        Scope scope = habitat.getByType(scopeClass);
        if(scope==null)
            throw new ComponentException("Failed to look up %s for %s",scopeClass,c);
        return new ScopedInhabitant<T>(womb,scope);
    }

}
