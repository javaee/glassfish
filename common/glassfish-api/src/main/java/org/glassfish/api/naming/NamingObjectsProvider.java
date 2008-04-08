package org.glassfish.api.naming;

import org.jvnet.hk2.annotations.Contract;

/**
 * Implementors of this contract wish to be called when the NamingManager is instanciated
 * so they have an opportunity to register named objects to the naming manager.
 *
 * Providers of naming objects should not initialize themselves the naming manager, but
 * should wait until a consumer of the naming manager (someone requesting a resource or
 * injection) instantiates it which will trigger the implementors of this interface
 * instanciation.
 *
 * @author Jerome Dochez
 */
@Contract
public interface NamingObjectsProvider {

    
}
