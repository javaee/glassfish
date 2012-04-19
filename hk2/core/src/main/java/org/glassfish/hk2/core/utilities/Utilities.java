package org.glassfish.hk2.core.utilities;

import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.DynamicConfiguration;
import org.glassfish.hk2.api.DynamicConfigurationService;
import org.glassfish.hk2.api.ServiceLocator;

/**
 * Core utilities.
 *
 * @author tbeerbower
 */
public class Utilities {

    /**
     * Add an alternate index to look up the given descriptor.
     *
     * @param locator     the service locator to associate this index with
     * @param descriptor  the descriptor that we are adding the index for
     * @param contract    the contract for the index
     * @param name        the name for the index
     * @param <T>         the descriptor type
     */
    public static <T> void addIndex(ServiceLocator locator, ActiveDescriptor<T> descriptor, Class<?> contract, String name) {
        DynamicConfigurationService dcs = locator.getService(DynamicConfigurationService.class);
        DynamicConfiguration config = dcs.createDynamicConfiguration();

        config.addActiveDescriptor(new AliasDescriptor<T>(locator, descriptor, contract, name));

        config.commit();
    }
}
