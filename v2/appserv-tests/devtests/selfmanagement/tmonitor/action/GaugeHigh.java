package com.sun.s1peqe.selfmanagement.tmonitor.action;
                                                                                                                                               
import javax.management.*;
import java.io.*;

public class GaugeHigh implements NotificationListener, com.sun.s1peqe.selfmanagement.tmonitor.action.GaugeHighMBean {
        private final String JMX_MONITOR_GAUGE_HIGH = "jmx.monitor.gauge.high";
        public GaugeHigh() {
        }
                                                                                                                                               
        public synchronized void handleNotification(Notification notification,
                        Object handback) {
          try {
                 FileWriter out = new FileWriter(new File("/space/selfmanagementResult.txt"),true);
                 if(notification != null) {
                     if(notification.getType().equals(JMX_MONITOR_GAUGE_HIGH)) {
                         out.write("Gauge Monitor Test - High - PASSED\n");
                     }
                 } else {
                     out.write("Gauge Monitor Test - High - FAILED\n");
                 }
                 out.flush();
                 out.close();
        } catch (Exception ex) { }
        }
}

