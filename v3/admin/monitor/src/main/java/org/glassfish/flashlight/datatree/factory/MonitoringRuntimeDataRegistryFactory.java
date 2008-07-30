/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.glassfish.flashlight.datatree.factory;

import org.glassfish.flashlight.MonitoringRuntimeDataRegistry;
import org.glassfish.flashlight.impl.MonitoringRuntimeDataRegistryImpl;

/**
 * To be used in the non GlassFish context - more so for testing purposes. 
 * In GlassFish an @Inject should be used.
 * 
 * @author Harpreet Singh
 */
public class MonitoringRuntimeDataRegistryFactory {
    
    public static MonitoringRuntimeDataRegistry getInstance (){
    
        return new MonitoringRuntimeDataRegistryImpl ();
    }

}
