
package org.glassfish.embed.impl;

import com.sun.enterprise.v3.server.APIClassLoaderServiceImpl;

/**
 *
 * @author Byron Nevins
 */
public class EmbeddedAPIClassLoaderServiceImpl extends APIClassLoaderServiceImpl{

    @Override
    public void postConstruct() {
        // nothing to do!
    }

    @Override
    public ClassLoader getAPIClassLoader() {
        return getClass().getClassLoader();
    }
}
