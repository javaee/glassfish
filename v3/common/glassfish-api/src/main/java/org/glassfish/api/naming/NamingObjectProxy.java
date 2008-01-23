package org.glassfish.api.naming;

import org.jvnet.hk2.annotations.Contract;

import javax.naming.Context;
import javax.naming.NamingException;

/**
 * A proxy object that can be bound to GlassfishNamingManager. Concrete
 *  implementation of this contract will take appropriate action when
 *  the proxy is lookedup. Typically, this can be used to lazily
 *  instantiate an Object at lookup time than at bind time.
 *
 * Again, it is upto the implementation to cache the result (inside
 *  the proxy implementation so that subsequent lookup can obtain the
 *  same cacheed object. Or the implementation can choose to return
 *  different object every time.
 *
 * @author Mahesh Kannan
 *
 */

@Contract
public interface NamingObjectProxy {

    /**
     * Create and return an object.
     *
     * @return an object
     */
    public Object getObject(Context ic)
            throws NamingException;

}
