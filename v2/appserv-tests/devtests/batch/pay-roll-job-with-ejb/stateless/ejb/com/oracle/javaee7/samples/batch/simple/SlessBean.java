package com.oracle.javaee7.samples.batch.simple;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Properties;

import javax.annotation.PostConstruct;

import javax.batch.operations.JobOperator;
import javax.batch.runtime.BatchRuntime;
import javax.ejb.Stateless;
import javax.ejb.EJB;

@Stateless
public class SlessBean
    implements Sless {

    boolean ejbCreateCalled = false;

    public void ejbCreate() {
        this.ejbCreateCalled = true;
    }

    public long submitJob() {
	try {
        StringBuilder xmlBuf = new StringBuilder();
	java.net.URL resource = this.getClass().getResource("/META-INF/batch-jobs/PayRollJob.xml");
	if (resource != null) {
	  BufferedReader br = new BufferedReader(new InputStreamReader(resource.openStream()));
	  for (String line = br.readLine(); line != null; line = br.readLine()) {
	    xmlBuf.append(line);
	  }
	  JobOperator jobOperator = BatchRuntime.getJobOperator();

	  Properties props = new Properties();
	  for (int i=0; i<9; i++)
		props.put(i, i);
	  return  jobOperator.start(xmlBuf.toString(), props);
	} else {
	  throw new IllegalArgumentException("XML not found");
	}
        } catch (Exception ex) {
		throw new RuntimeException(ex);
		}
    }

    public boolean wasEjbCreateCalled() {
	return ejbCreateCalled;
    }

														            
}
