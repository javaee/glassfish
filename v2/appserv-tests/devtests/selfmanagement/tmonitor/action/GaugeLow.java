package com.sun.s1peqe.selfmanagement.tmonitor.action;
                                                                                                                                               
import javax.management.*;
import java.io.*;

public class GaugeLow implements NotificationListener, 
     com.sun.s1peqe.selfmanagement.tmonitor.action.GaugeLowMBean {
        private final String JMX_MONITOR_GAUGE_LOW = "jmx.monitor.gauge.low";
        public GaugeLow() {
        }
        
        public synchronized void handleNotification(Notification notification,
                        Object handback) {
          try {
                 FileWriter out = new FileWriter(new File("/space/selfmanagementResult.txt"),true);
                 if(notification != null) {
                     if(notification.getType().equals(JMX_MONITOR_GAUGE_LOW)) {
                         out.write("Gauge Monitor Test - Low - PASSED\n");
                     }
                 } else {
                     out.write("Gauge Monitor Test - Low - FAILED\n");
                 }
                 out.flush();
                 out.close();
        } catch (Exception ex) { }
        }
}

