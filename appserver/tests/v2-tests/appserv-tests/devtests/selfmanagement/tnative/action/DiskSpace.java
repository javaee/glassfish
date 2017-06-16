package com.sun.s1peqe.selfmanagement.tnative.action;

import javax.management.*;
import java.io.*;

public class DiskSpace implements NotificationListener, 
    com.sun.s1peqe.selfmanagement.tnative.action.DiskSpaceMBean {

    private final String JMX_MONITOR_COUNTER_THRESHOLD = "jmx.monitor.counter.threshold";
    public DiskSpace() { 
    }
    public synchronized void handleNotification(Notification notification,
        Object handback) {
        try {
            FileWriter out = new FileWriter(new File("/space/selfmanagementResult.txt"),true);
            if(notification != null) {
	        if(notification.getType().equals(JMX_MONITOR_COUNTER_THRESHOLD)) {
                    System.out.println("TEST PASSED");
                    out.write("Disk Free Monitor Test - PASSED\n");
                }
            } else {
                System.out.println("TEST FAILED");
	        out.write("Disk Free Monitor Test - FAILED\n");
            }
            out.flush();
            out.close();
        } catch (Exception ex) { }
    }
}
