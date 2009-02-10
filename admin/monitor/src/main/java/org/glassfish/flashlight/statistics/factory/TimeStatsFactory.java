/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.glassfish.flashlight.statistics.factory;

import org.glassfish.flashlight.statistics.TimeStats;
import org.glassfish.flashlight.statistics.impl.TimeStatsMillisImpl;
import org.glassfish.flashlight.statistics.impl.TimeStatsNanosImpl;

/**
 *
 * @author Harpreet Singh
 */
public class TimeStatsFactory {

    public static TimeStats createTimeStatsMilli (){
        return new TimeStatsMillisImpl ();
    }
    
    public static TimeStats createTimeStatsNano (){
        return new TimeStatsNanosImpl ();
    }
}
