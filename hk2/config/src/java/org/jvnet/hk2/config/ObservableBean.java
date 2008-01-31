package org.jvnet.hk2.config;

/**
 * Implementation of a @Configured object can optionally fire configuration change
 * events when it's mutated.
 *
 * @author Jerome Dochez
 */
public interface ObservableBean {

    /**
     * Add a new listener to configuration changes.
     *
     * @param listener new listener
     */
    public void addListener(ConfigListener listener);

    /**
     * Remove a listener
     *
     * @param listener to remove
     * @return true if listener removal was successful.
     */
    public boolean removeListener(ConfigListener listener);
}
