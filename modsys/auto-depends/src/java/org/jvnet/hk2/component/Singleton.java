package org.jvnet.hk2.component;

import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;
import com.sun.hk2.component.ScopeInstance;

/**
 * Singleton scope.
 *
 * @author Kohsuke Kawaguchi
 */
@Service(scope=Singleton.class)
public class Singleton extends Scope {
    @Inject
    public ComponentManager manager;

    // ComponentManager knows Singleton and doesn't call this method
    // for efficiency, but nevertheless it is implemented correctly.
    @Override
    public ScopeInstance current() {
        return manager.singletonScope;
    }
}
