package com.oracle.javaee7.samples.batch.chunk;

import javax.ejb.Remote;

@Remote
public interface Sless {

    public long submitJob();

    public String getJobExitStatus(long executionId);

    public boolean wasEjbCreateCalled();

}

