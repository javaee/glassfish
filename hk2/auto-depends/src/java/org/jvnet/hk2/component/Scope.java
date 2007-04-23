package org.jvnet.hk2.component;

import org.jvnet.hk2.annotations.Contract;
import org.jvnet.hk2.annotations.Service;
import com.sun.hk2.component.ScopeInstance;

/**
 * 
 *
 * @author Kohsuke Kawaguchi
 * @see Service#scope() 
 */
@Contract
public abstract class Scope {
    public abstract ScopeInstance current();
}
