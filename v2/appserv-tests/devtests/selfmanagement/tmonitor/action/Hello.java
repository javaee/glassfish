package com.sun.s1peqe.selfmanagement.tmonitor.action;

import javax.management.*;
import java.io.*;

public class Hello implements NotificationListener, com.sun.s1peqe.selfmanagement.tmonitor.action.HelloMBean {

        private final String JMX_MONITOR_COUNTER_THRESHOLD = "jmx.monitor.counter.threshold";
	public Hello() { 
        }
	public synchronized void handleNotification(Notification notification,
                        Object handback) {
          try {
                 FileWriter out = new FileWriter(new File("/space/selfmanagementResult.txt"));
                 if(notification != null) {
		     if(notification.getType().equals(JMX_MONITOR_COUNTER_THRESHOLD)) {
                         System.out.println("TEST PASSED");
                         out.write("Counter Monitor Test - PASSED\n");
                     }
                 } else {
                     System.out.println("TEST FAILED");
	             out.write("Counter Monitor Test - FAILED\n");
                 }
                 out.flush();
                 out.close();
        } catch (Exception ex) { }
	}
}
