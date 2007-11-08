package com.sun.s1peqe.selfmanagement.tlifecycle.action;
                                                                                                                                               
import javax.management.*;
import java.io.*;
                                                                                                                                               
public class LifecycleTermination 
    implements NotificationListener, 
    com.sun.s1peqe.selfmanagement.tlifecycle.action.LifecycleTerminationMBean {
    
    private final String JMX_LIFECYCLE_TERMINATION = "lifecycle.termination";
    public LifecycleTermination() {
    }
                                                                                                                                               
    public synchronized void handleNotification(Notification notification,
        Object handback) {
        try {
            FileWriter out = new FileWriter(new File("/space/selfmanagementResult.txt"),true);
            if(notification != null) {
                if(notification.getType().equals(JMX_LIFECYCLE_TERMINATION)) {
                    out.write("Lifecycle Termination Event - Test PASSED\n");
                }
            } else {
                out.write("Lifecycle Event - Test FAILED\n");
            }
            out.flush();
            out.close();
        } catch (Exception ex) { }
    }
}

