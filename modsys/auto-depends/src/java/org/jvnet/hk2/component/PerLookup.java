package org.jvnet.hk2.component;

import org.jvnet.hk2.annotations.Service;
import com.sun.hk2.component.ScopeInstance;

/**
 * 
 * @author Kohsuke Kawaguchi
 */
@Service(scope=Singleton.class)
public class PerLookup extends Scope {
    @Override
    public ScopeInstance current() {
        return new ScopeInstance();
    }
}
