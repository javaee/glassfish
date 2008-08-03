/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.glassfish.flashlight.statistics.impl;

import org.glassfish.flashlight.statistics.TimeStatsNanos;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.PerLookup;

/**
 *
 * @author Harpreet Singh
 */
@Service(name = "timeStatsNanos")
@Scoped(PerLookup.class)
public class TimeStatsNanosImpl extends TimeStatsAbstractImpl
        implements TimeStatsNanos {

    private String NAME = "timeStatsNanos";
    private String DESCRIPTION = "TimeStatistic Nano";
    private String UNIT = "Nano seconds";

    public TimeStatsNanosImpl() {
        super.setName(NAME);
        super.setEnabled(true);
    }

    @Override
    public void entry() {
        entryTime = System.nanoTime();
        super.postEntry();
    }

    @Override
    public void exit() {
        exitTime = System.nanoTime();
        super.postExit();
    }

    @Override
    public Object getValue() {
        return getTime();
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
