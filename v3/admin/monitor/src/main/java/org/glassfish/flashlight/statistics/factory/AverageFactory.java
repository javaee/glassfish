/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.glassfish.flashlight.statistics.factory;
import org.glassfish.flashlight.statistics.*;
import org.glassfish.flashlight.statistics.impl.AverageImpl;
import org.glassfish.flashlight.statistics.Average;
/**
 *
 * @author hsingh
 */
public class AverageFactory {
    
    public static Average createAverage (int bucketSize){
        Average average = new AverageImpl (bucketSize);
        return average;
    }
 
}
