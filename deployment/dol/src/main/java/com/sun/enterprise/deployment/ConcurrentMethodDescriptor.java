package com.sun.enterprise.deployment;

import java.util.concurrent.TimeUnit;

public class ConcurrentMethodDescriptor extends Descriptor {

    private MethodDescriptor method;

    private Boolean writeLock = null;

    private TimeoutValueDescriptor accessTimeout;

    public void setAccessTimeout(TimeoutValueDescriptor t) {
        accessTimeout = t;
    }

    public void setConcurrentMethod(MethodDescriptor m) {
        method = m;
    }

    public MethodDescriptor getConcurrentMethod() {
        return method;
    }

    public void setWriteLock(boolean flag) {
        writeLock = flag;
    }

    public boolean hasLockMetadata() {
        return (writeLock != null);
    }

    public boolean isWriteLocked() {
        return writeLock;
    }

    public boolean isReadLocked() {
        return !writeLock;
    }

    public boolean hasAccessTimeout() {
        return (accessTimeout != null);
    }
    
    public long getAccessTimeoutValue() {
        return accessTimeout.getValue();
    }

    public TimeUnit getAccessTimeoutUnit() {
        return accessTimeout.getUnit();
    }


}