package com.sun.s1peqe.selfmanagement.tmonitor.action;
                                                                                                                                               
import javax.management.*;
import java.io.*;

public class StringMatch implements NotificationListener, 
    com.sun.s1peqe.selfmanagement.tmonitor.action.StringMatchMBean {
        private final String JMX_MONITOR_STRING_MATCHES = "jmx.monitor.string.matches";
        public StringMatch() {
        }
        public synchronized void handleNotification(Notification notification,
                        Object handback) {
          try {
                 FileWriter out = new FileWriter(new File("/space/selfmanagementResult.txt"),true);
                 if(notification != null) {
                     if(notification.getType().equals(JMX_MONITOR_STRING_MATCHES)) {
                         out.write("String Monitor Test - Matches - PASSED\n");
                     }
                 } else {
                     out.write("String Monitor Test - Matches - FAILED\n");
                 }
                 out.flush();
                 out.close();
        } catch (Exception ex) { }
        }
}

