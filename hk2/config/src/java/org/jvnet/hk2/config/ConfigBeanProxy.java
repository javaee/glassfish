package org.jvnet.hk2.config;

/**
 * Marker interface that signifies that the interface
 * is meant to be used as a strongly-typed proxy to
 * {@link Dom}. 
 *
 * <p>
 * To obtain the Dom object, use {@link Dom#unwrap(ConfigBeanProxy)}.
 * This design allows the interfaces to be implemented by other code
 * outside DOM more easily.
 *
 * @author Kohsuke Kawaguchi
 * @see Dom#unwrap(ConfigBeanProxy)
 */
public interface ConfigBeanProxy {
}
