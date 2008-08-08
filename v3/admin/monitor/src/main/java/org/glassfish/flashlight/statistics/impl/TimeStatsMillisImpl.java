/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.glassfish.flashlight.statistics.impl;

import org.glassfish.flashlight.statistics.TimeStatsMillis;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.PerLookup;

/**
 *
 * @author Harpreet Singh
 */
@Service (name="timeStatsMillis")
@Scoped (PerLookup.class)
public class TimeStatsMillisImpl extends TimeStatsAbstractImpl
    implements TimeStatsMillis {
    
    private String NAME = "timeStatsMillis";
    private String UNIT = "Milli seconds";
    private String DESCRIPTION = "TimeStatistic Milli Seconds";
    
    public TimeStatsMillisImpl (){
        super.setName(NAME);
        super.setEnabled(true);
    }
    @Override
    public void entry (){
        super.postEntry (System.currentTimeMillis());
    }
    
    @Override
    public void exit() {
        super.postExit (System.currentTimeMillis());
      }
    @Override 
    public Object getValue (){
        return getTime ();
    }

    public String getUnit() {
        return this.UNIT;
    }

    public String getDescription() {
        return this.DESCRIPTION;
    }
    public String toString() {
        return "Statistic " + getClass().getName() + NEWLINE +
                "Name: " + getName() + NEWLINE +
                "Description: " + getDescription() + NEWLINE +
                "Unit: " + getUnit() + NEWLINE +
                "LastSampleTime: " + getLastSampleTime() + NEWLINE +
                "StartTime: " + getStartTime();
    }
}
