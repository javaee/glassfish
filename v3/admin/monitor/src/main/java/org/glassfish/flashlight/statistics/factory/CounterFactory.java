/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.glassfish.flashlight.statistics.factory;

import org.glassfish.flashlight.statistics.*;
import org.glassfish.flashlight.statistics.impl.CounterImpl;

/**
 *
 * @author Harpreet Singh
 */
public class CounterFactory {
    

    public static Counter createCount (long... seed){
        Counter count; 
        if (seed.length == 0){
            count = new CounterImpl ();
        } else {
            count = new CounterImpl ();
            count.setCount(seed[0]);
        }
        count.setEnabled(true);
        return count;
    }
}
