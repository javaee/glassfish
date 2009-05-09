/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.glassfish.flashlight.statistics.impl;

import org.glassfish.flashlight.statistics.*;
import java.util.concurrent.atomic.AtomicLong;
import org.glassfish.flashlight.datatree.impl.AbstractTreeNode;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.PerLookup;

/**
 * @author Harpreet Singh
 */
@Service(name = "counter")
@Scoped(PerLookup.class)
public class CounterImpl extends AbstractTreeNode implements Counter {

    /** DEFAULT_UPPER_BOUND is maximum value Long can attain */
    public static final long DEFAULT_MAX_BOUND = java.lang.Long.MAX_VALUE;
    /** DEFAULT_LOWER_BOUND is same as DEFAULT_VALUE i.e. 0 */
    public static final long DEFAULT_VALUE = java.math.BigInteger.ZERO.longValue();
    public static final long DEFAULT_MIN_BOUND = DEFAULT_VALUE;
    /** DEFAULT_VALUE of any statistic is 0 */
    protected static final String NEWLINE = System.getProperty("line.separator");
    private AtomicLong count = new AtomicLong(0);
    long max = 0;
    long min = 0;
    private AtomicLong lastSampleTime = new AtomicLong();
    private String NAME = "counter";
    private String DESCRIPTION = "Counter CountStatistic";
    private String UNIT = java.lang.Long.class.toString();

    private long startTime = 0;

    public CounterImpl() {
        startTime = System.currentTimeMillis();
    }

    public long getCount() {
        return count.get();
    }

    public void setCount(long count) {
        if (count > max) {
            max = count;
        } else {
            min = count;
        }
        this.count.set(count);
    }
    // TBD: remove reference to getSampleTime -> extremely inefficient implementation
    // Will have to be replaced by Timer implemenation
    public void increment() {
        long cnt = this.count.incrementAndGet();
        if (cnt > max) {
            max = cnt;
        // Remove this after refactoring to Timer Impl. This is inefficient
        }
        this.lastSampleTime.set(getSampleTime ());
    }

    //automatically add the increment to cnt
    public void  increment(long delta) {
        long cnt = this.count.addAndGet(delta);
        if(cnt > max) {
            max = cnt;
        }
        this.lastSampleTime.set(getSampleTime());
    }
    
    public void decrement() {
        long cnt = this.count.decrementAndGet();
        if (cnt < min) {
            min = cnt;
        }
    }

    public void setReset(boolean reset) {
        if (reset) {
            this.count.set(0);
        }
    }

    @Override
    public Object getValue() {
        return getCount();
    }

    public String getUnit() {
        return this.UNIT;
    }

    public String getDescription() {
        return this.DESCRIPTION;
    }

    public long getStartTime() {
        return this.startTime;
    }

    public long getLastSampleTime() {
        return this.lastSampleTime.longValue();
    }

    /*
     * TBD
     * This is an inefficient implementation. Should schedule a Timer task
     * that gets a timeout event every 30s or so and updates this value
     */
    private long getSampleTime() {
        return System.currentTimeMillis();

    }
}
