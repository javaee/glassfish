/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.glassfish.flashlight.statistics.impl;

import org.glassfish.flashlight.datatree.impl.AbstractTreeNode;
import org.glassfish.flashlight.statistics.*;

/**
 * @author Harpreet Singh
 */
public abstract class TimeStatsAbstractImpl extends AbstractTreeNode
        implements TimeStats {

    protected long entryTime = 0;
    protected long exitTime = 0;
    private long totalTime = 0;
    private long lastSampleTime = 0;
    protected long startTime = 0;
    long time = 0;
    long low = -1;
    long high = -1;
    long sum = 0;
    int count = 0;
    protected static final String NEWLINE = System.getProperty("line.separator");

    public long getTime() {
        return time;
    }

    abstract public void entry();

    abstract public void exit();

    protected void postEntry() {
        if (startTime == 0) {
            startTime = entryTime;
        }
        this.setLastSampleTime(entryTime);
    }

    public void postExit() {
        time = exitTime - entryTime;
        totalTime += time;
        setLowAndHigh(time);
        count++;
    }

    private void setLowAndHigh(long time) {
        if (low == -1 && high == -1) {
            low = high = time;
        }
        if (time < low) {
            low = time;
        }
        if (time > high) {
            high = time;
        }
    }

    public long getMinimumTime() {
        return low;
    }

    public long getMaximumTime() {
        return high;
    }

    // only for testing purposes.
    public void setTime(long time) {
        //  System.err.println ("setTime only for Testing purposes");
        this.time = time;
        setLowAndHigh(time);
    }

    public void setReset(boolean reset) {
        entryTime = 0;
        time = 0;
        low = 0;
        high = 0;
        count = 0;

    }

    public long getTimesCalled() {
        return count;
    }
    // Implementations for TimeStatistic
    public long getCount() {
        return getTimesCalled();
    }

    public long getMaxTime() {
        return getMaximumTime();
    }

    public long getMinTime() {
        return getMinimumTime();
    }

    public long getTotalTime() {
        return totalTime;
    }

    public long getLastSampleTime() {
        return this.lastSampleTime;
    }

    public long getStartTime() {
        return this.startTime;
    }

    private void setLastSampleTime(long time) {
        this.lastSampleTime = time;
    }
}
