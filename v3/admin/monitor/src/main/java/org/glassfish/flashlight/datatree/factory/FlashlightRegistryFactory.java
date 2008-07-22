/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.glassfish.flashlight.datatree.factory;

import org.glassfish.flashlight.FlashlightRegistry;
import org.glassfish.flashlight.FlashlightRegistryImpl;

/**
 *
 * @author hsingh
 */
public class FlashlightRegistryFactory {
    
    public static FlashlightRegistry getInstance (){
    
        return new FlashlightRegistryImpl ();
    }

}
