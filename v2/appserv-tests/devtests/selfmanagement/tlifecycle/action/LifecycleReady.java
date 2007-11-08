package com.sun.s1peqe.selfmanagement.tlifecycle.action;
                                                                                                                                               
import javax.management.*;
import java.io.*;
                                                                                                                                               
public class LifecycleReady implements NotificationListener, 
    com.sun.s1peqe.selfmanagement.tlifecycle.action.LifecycleReadyMBean {
    
    private final String JMX_LIFECYCLE_READY = "lifecycle.ready";
    public LifecycleReady() {
    }
                                                                                                                                              
    public synchronized void handleNotification(Notification notification,
        Object handback) {
        try {
            FileWriter out = new FileWriter(new File("/space/selfmanagementResult.txt"),true);
            if(notification != null) {
                if(notification.getType().equals(JMX_LIFECYCLE_READY)) {
                    out.write("Lifecycle Ready Event - Test PASSED\n");
                }
            } else {
                out.write("Lifecycle Ready Event - Test FAILED\n");
            }
            out.flush();
            out.close();
        } catch (Exception ex) { }
    }
}

