package org.jvnet.hk2.component;

import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Scoped;
import com.sun.hk2.component.ScopeInstance;

import java.util.HashMap;

/**
 * {@link Scope} local to each invocation.
 *
 * <p>
 * Components in this scope will create new instances every time someone asks for it. 
 *
 * @author Kohsuke Kawaguchi
 */
@Service @Scoped(Singleton.class)
public class PerLookup extends Scope {
    @Override
    public ScopeInstance current() {
        return new ScopeInstance(new HashMap());
    }
}
