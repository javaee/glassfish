package com.oracle.javaee7.samples.batch.simple.batchlet;

import javax.ejb.Remote;

@Remote
public interface Sless {

    public long submitJob();

    public boolean wasEjbCreateCalled();

    public String getJobExitStatus(long executionId);

}

