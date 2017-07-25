/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

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
