package org.jvnet.hk2.config;

import java.beans.PropertyVetoException;
import java.beans.PropertyChangeEvent;

/**
 * Interceptor interface to be notified of read/write operations on a ConfigBean.
 * Interceptor can be configured using the optional T interface.
 *
 * @Author Jerome Dochez
 */
public interface ConfigBeanInterceptor<T> {

    /**
     * Interceptor can usually be configured, allowing for customizing how
     * the interceptor should behave or do.
     *
     * @return interface implementing the configuration capability of this
     * interceptor
     */
    public T getConfiguration();

    /**
     * Notification that an attribute is about to be changed
     *
     * @param evt information about the forthcoming change
     * @throws PropertyVetoException if the change is unacceptable
     */
    public void beforeChange(PropertyChangeEvent evt) throws PropertyVetoException;

    /**
     * Notification that an attribute has changed
     *
     * @param evt information about the change
     * @param timestamp time of the change
     */
    public void afterChange(PropertyChangeEvent evt, long timestamp);

    /**
     * Notification of an attribute read
     *
     * @param source  object owning the attribute
     * @param xmlName name of the attribute
     * @param value value of the attribute
     */
    public void readValue(ConfigBean source, String xmlName, Object value);
}
