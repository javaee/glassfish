/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.enterprise.universal;

import com.sun.enterprise.universal.i18n.LocalStringsImpl;

/**
 * Convert a msec duration into weeks, hours, minutes, seconds
 * @author bnevins
 * Thread Safe.  
 * Immutable
 */
public class Duration {
    public Duration(long msec) {
        long msecLeftover = msec;
        
        numWeeks = msecLeftover / MSEC_PER_WEEK;
        msecLeftover -= numWeeks * MSEC_PER_WEEK;
        
        numDays = msecLeftover / MSEC_PER_DAY;
        msecLeftover -= numDays * MSEC_PER_DAY;
        
        numHours = msecLeftover / MSEC_PER_HOUR;
        msecLeftover -= numHours * MSEC_PER_HOUR;
        
        numMinutes = msecLeftover / MSEC_PER_MINUTE;
        msecLeftover -= numMinutes * MSEC_PER_MINUTE;
        
        numSeconds = msecLeftover / MSEC_PER_SECOND;
        msecLeftover -= numSeconds * MSEC_PER_SECOND;

        numMilliSeconds = msecLeftover;
    }

    @Override
    public String toString() {
        String s = "";
        
        if(numWeeks > 0)
            s = strings.get("weeks", numWeeks, numDays, numHours, numMinutes, numSeconds);
        else if(numDays > 0)
            s = strings.get("days", numDays, numHours, numMinutes, numSeconds);
        else if(numHours > 0)
            s = strings.get("hours", numHours, numMinutes, numSeconds);
        else if(numMinutes > 0)
            s = strings.get("minutes", numMinutes, numSeconds);
        else
            s = strings.get("milliseconds", numMilliSeconds + numSeconds * MSEC_PER_SECOND);
        
        return s;
    }

    public final long numWeeks;
    public final long numDays;
    public final long numHours;
    public final long numMinutes;
    public final long numSeconds;
    public final long numMilliSeconds;

    // possibly useful constants
    public final static long MSEC_PER_SECOND = 1000; 
    public final static long MSEC_PER_MINUTE = 60 * MSEC_PER_SECOND; 
    public final static long MSEC_PER_HOUR = MSEC_PER_MINUTE * 60; 
    public final static long MSEC_PER_DAY = MSEC_PER_HOUR * 24;
    public final static long MSEC_PER_WEEK = MSEC_PER_DAY * 7;
    
    private final LocalStringsImpl strings = new LocalStringsImpl(Duration.class);
}
