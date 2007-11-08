package com.sun.s1peqe.selfmanagement.tmonitor.action;
                                                                                                                                               
import javax.management.*;
import java.io.*;

public class StringDiffer implements NotificationListener, 
    com.sun.s1peqe.selfmanagement.tmonitor.action.StringDifferMBean {
        private final String JMX_MONITOR_STRING_DIFFERS = "jmx.monitor.string.differs";
        public StringDiffer() {
        }
        public synchronized void handleNotification(Notification notification,
                        Object handback) {
          try {
                 FileWriter out = new FileWriter(new File("/space/selfmanagementResult.txt"),true);
                 if(notification != null) {
                     if(notification.getType().equals(JMX_MONITOR_STRING_DIFFERS)) {
                         out.write("String Monitor Test - Differs - PASSED\n");
                     }
                 } else {
                     out.write("String Monitor Test - Differs - FAILED\n");
                 }
                 out.flush();
                 out.close();
        } catch (Exception ex) { }
        }
}

