package com.oracle.javaee7.samples.batch.cmt.chunk;

import javax.ejb.Remote;
import java.util.Collection;
import java.util.Map;
import java.util.List;

@Remote
public interface JobSubmitter {

    public String nextId();

    public long submitJob(String jobName);

    public Collection<String> listJobs(boolean useLongFormat);

    public Collection<String> listJobExecutions(boolean useLongFormat, long... executinIds);

    public Map<String, String> toMap(long executionId);

    public String getJobExitStatus(long executionId);

    public List<Long> getAllExecutionIds(String jobName);

}

