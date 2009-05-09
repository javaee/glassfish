package com.sun.enterprise.deployment;

import java.util.concurrent.TimeUnit;

public class TimeoutValueDescriptor extends Descriptor {


    private long value;
    private TimeUnit unit;

    public void setValue(long l) {
        value = l;
    }

    public void setUnit(TimeUnit t) {
        unit = t;
    }

    public long getValue() {
        return value;
    }

    public TimeUnit getUnit() {
        return unit;
    }


}