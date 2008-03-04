package org.glassfish.api.naming;

import org.jvnet.hk2.annotations.Contract;

/**
 * @author Mahesh Kannan
 *         Date: Feb 28, 2008
 */
@Contract
public interface NamedNamingObjectProxy {

    /**
     * Returns the name that will be used to publish this object in the naming maanager
     * @return the name to bind
     */
    public Object handle(String name);
    
}
