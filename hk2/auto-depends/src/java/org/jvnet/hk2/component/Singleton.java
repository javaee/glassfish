package org.jvnet.hk2.component;

import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Scoped;
import com.sun.hk2.component.ScopeInstance;

/**
 * Singleton scope.
 *
 * @author Kohsuke Kawaguchi
 */
@Service @Scoped(Singleton.class)
public class Singleton extends Scope {
    @Inject
    public Habitat habitat;

    /**
     * @deprecated
     *  Singleton instances are not stored in a single map.
     */
    @Override
    public ScopeInstance current() {
        throw new UnsupportedOperationException();
    }
}
