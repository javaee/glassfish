package com.sun.s1peqe.selfmanagement.tlifecycle.action;
                                                                                                                                               
import javax.management.*;
import java.io.*;
                                                                                                                                               
public class LifecycleShutdown implements NotificationListener, 
    com.sun.s1peqe.selfmanagement.tlifecycle.action.LifecycleShutdownMBean {
    
    private final String JMX_LIFECYCLE_SHUTDOWN = "lifecycle.shutdown";
    public LifecycleShutdown() {
    }
                                                                                                                                               
    public synchronized void handleNotification(Notification notification,
                Object handback) {
        try {
            FileWriter out = new FileWriter(new File("/space/selfmanagementResult.txt"),true);
            if(notification != null) {
                if(notification.getType().equals(JMX_LIFECYCLE_SHUTDOWN)) {
                    out.write("Lifecycle Shutdown Event - Test PASSED\n");
                }
            } else {
                out.write("Lifecycle Shutdown Event - Test FAILED\n");
            }
            out.flush();
            out.close();
        } catch (Exception ex) { }
    }
}

