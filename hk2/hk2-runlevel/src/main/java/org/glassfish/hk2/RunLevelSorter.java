package org.glassfish.hk2;

import org.glassfish.hk2.api.ActiveDescriptor;

/**
 * Contract for sorting descriptors for run level services.
 *
 * @author tbeerbower
 */
public interface RunLevelSorter {

    /**
     * Sort the given list of run level service descriptors.
     *
     * @param descriptors  the descriptors
     */
    public void sort(java.util.List<ActiveDescriptor<?>> descriptors);
}
