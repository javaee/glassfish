package com.oracle.javaee7.samples.batch.cmt.chunk;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Properties;
import java.util.Set;
import java.util.HashSet;
import java.util.Collection;
import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;

import javax.inject.Inject;
import javax.annotation.PostConstruct;

import javax.batch.operations.JobOperator;
import javax.batch.runtime.BatchRuntime;
import javax.batch.runtime.JobExecution;
import javax.batch.runtime.JobInstance;
import com.ibm.jbatch.spi.TaggedJobExecution;

import javax.ejb.Stateless;
import javax.ejb.EJB;

@Stateless
public class JobSubmitterBean
    implements JobSubmitter {

    //@Inject
    IdGenerator idGenerator;

    public String nextId() {
        return idGenerator != null ? idGenerator.nextId() : "-1";
    }

    public long submitJob(String jobName) {
	try {
	  JobOperator jobOperator = BatchRuntime.getJobOperator();

	  Properties props = new Properties();
	  for (int i=0; i<3; i++)
		props.put(jobName + "-Key-" + i, jobName+"-Value-" + i);
	  return  jobOperator.start(jobName, props);
        } catch (Exception ex) {
	  throw new RuntimeException(ex);
	}
    }

    public String getJobExitStatus(long executionId) {
	try {
	  JobOperator jobOperator = BatchRuntime.getJobOperator();
	  JobExecution je =  jobOperator.getJobExecution(executionId);
	  return je.getExitStatus();
	} catch (Exception ex) {
	  throw new RuntimeException(ex);
	}

    }

    public Collection<String> listJobs(boolean useLongFormat) {
	try {
	  JobOperator jobOperator = BatchRuntime.getJobOperator();
	  Set<String> jobs = new HashSet<String>();
	  if (!useLongFormat) {
	      for (String jobName : jobOperator.getJobNames()) {
	          StringBuilder sb = new StringBuilder();
		  sb.append(jobName).append(" ").append(jobOperator.getJobInstanceCount(jobName));
		  jobs.add(sb.toString());
	     }
          } else {
	     int index = 0;
	     for (String jobName : jobOperator.getJobNames()) {
		List<JobInstance> exe = jobOperator.getJobInstances(jobName, 0, Integer.MAX_VALUE - 1);
		if (exe != null) {
		   for (JobInstance ji : exe) {
		      for (JobExecution je : jobOperator.getJobExecutions(ji)) {
	                  StringBuilder sb = new StringBuilder();
                          try {
                              sb.append(index++).append(" ").append(jobName).append(" ").append(((TaggedJobExecution) je).getTagName())
			      	.append(" ").append(je.getBatchStatus()).append(" ").append(je.getExitStatus());
	                      jobs.add(sb.toString());
                          } catch (Exception ex) {
	                      jobs.add("Exception : " + sb.toString());
                          }
                      }
                   }
		}
	     }
	   }

	   return jobs;
	} catch (Exception ex) {
	  throw new RuntimeException(ex);
	}
     }

    public Map<String, String> toMap(long executionId) {
       HashMap<String, String> map = new HashMap<String, String>();
       try {
	  JobOperator jobOperator = BatchRuntime.getJobOperator();
	  JobExecution je = jobOperator.getJobExecution(executionId);
          map.put("jobName", ""+je.getJobName());
          map.put("appName", ""+((TaggedJobExecution) je).getTagName());

	  try {
            map.put("instanceCount", ""+jobOperator.getJobInstanceCount(je.getJobName()));
          } catch (Exception ex) {}

          map.put("instanceID", ""+jobOperator.getJobInstance(je.getExecutionId()).getInstanceId());
          map.put("executionID", ""+je.getBatchStatus());
          map.put("batchStatus", ""+je.getBatchStatus());
          map.put("exitStatus", ""+je.getExitStatus());
          map.put("startTime", ""+je.getStartTime().getTime());
          map.put("endTime", ""+je.getEndTime().getTime());
       } catch (Exception ex) {
          map.put("EXCEPTION", ex.toString());
       }

       return map;
    }
	
    public Collection<String> listJobExecutions(boolean useLongFormat, long... executinIds) {
	Set<String> jobs = new HashSet<String>();
	return jobs;
    }

    public List<Long> getAllExecutionIds(String jobName) {
	List<Long> list = new LinkedList<Long>();
	try {
        if (jobName != null)
           getAllExecutionIds(jobName, list);
	else {
	   JobOperator jobOperator = BatchRuntime.getJobOperator();
	   for (String jn : jobOperator.getJobNames())
              getAllExecutionIds(jn, list);
	}
        } catch (Exception ex) {

	}
	return list;
    }

    private void getAllExecutionIds(String jobName, List<Long> list)
    	throws Exception {
	JobOperator jobOperator = BatchRuntime.getJobOperator();
	List<JobInstance> exe = jobOperator.getJobInstances(jobName, 0, Integer.MAX_VALUE - 1);
	if (exe != null) {
	   for (JobInstance ji : exe) {
	      for (JobExecution je : jobOperator.getJobExecutions(ji)) {
                 list.add(je.getExecutionId());
              }
           }
	}
     }

}
