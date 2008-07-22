/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.glassfish.flashlight.statistics.factory;

import org.glassfish.flashlight.statistics.TimeStats;
import org.glassfish.flashlight.statistics.impl.TimeStatsMilli;
import org.glassfish.flashlight.statistics.impl.TimeStatsNano;

/**
 *
 * @author Harpreet Singh
 */
public class TimeStatsFactory {

    public static TimeStats createTimeStatsMilli (){
        return new TimeStatsMilli ();
    }
    
    public static TimeStats createTimeStatsNano (){
        return new TimeStatsNano ();
    }
}
