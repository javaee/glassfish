package com.oracle.javaee7.samples.batch.simple;

import javax.ejb.Remote;

@Remote
public interface Sless {

    public long submitJob();

    public boolean wasEjbCreateCalled();

}

