package org.jvnet.hk2.component;

import com.sun.hk2.component.ScopeInstance;
import org.jvnet.hk2.annotations.Contract;

/**
 * 
 *
 * @author Kohsuke Kawaguchi
 * @see org.jvnet.hk2.annotations.Scoped#value() 
 */
@Contract
public abstract class Scope {
    public abstract ScopeInstance current();
}
