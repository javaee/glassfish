package org.jvnet.hk2.config;

import java.lang.reflect.Proxy;

/**
 * Transfomer can transform a source object into a destination object, usually providing a different
 * view of the original object.
 *
 * @author Jerome Dochez
 */
public interface Transformer {

    /**
     * Returns a different view of the source object.
     *
     * @param source the source object to transform
     * @return the transformed view
     */
    public <T extends ConfigBeanProxy> T transform(T source);
}
