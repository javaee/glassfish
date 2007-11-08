package com.sun.s1peqe.selfmanagement.tnative.action;

import javax.management.*;
import java.io.*;

public class CpuUtil implements NotificationListener, 
    com.sun.s1peqe.selfmanagement.tnative.action.CpuUtilMBean {

    private final String JMX_MONITOR_GAUGE_LOW = "jmx.monitor.gauge.low";
    private final String JMX_MONITOR_GAUGE_HIGH = "jmx.monitor.gauge.high";
    public CpuUtil() { 
    }
    public synchronized void handleNotification(Notification notification,
        Object handback) {
        try {
            FileWriter out = new FileWriter(new File("/space/selfmanagementResult.txt"),true);
            if(notification != null) {
	        if(notification.getType().equals(JMX_MONITOR_GAUGE_LOW) || 
                        notification.getType().equals(JMX_MONITOR_GAUGE_HIGH)) {
                    System.out.println("TEST PASSED");
                    out.write("Cpu util Monitor Test - PASSED\n");
                }
            } else {
                System.out.println("TEST FAILED");
	        out.write("Cpu util Monitor Test - FAILED\n");
            }
            out.flush();
            out.close();
        } catch (Exception ex) { }
    }
}
