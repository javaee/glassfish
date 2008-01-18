package com.sun.hk2.component;

import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.component.Inhabitant;

import java.util.Collection;
import java.util.Collections;

/**
 * Partial implementation of {@link Inhabitant} that defines methods whose
 * semantics is fixed by {@link Habitat}.
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class AbstractInhabitantImpl<T> implements Inhabitant<T>  {
    private Collection<Inhabitant> companions;

    public final T get() {
        return get(this);
    }

    public Inhabitant lead() {
        return null;
    }

    public final Collection<Inhabitant> companions() {
        if(companions==null)    return Collections.emptyList();
        else                    return companions;
    }

    public final void setCompanions(Collection<Inhabitant> companions) {
        this.companions = companions;
    }
}
