package com.sun.s1peqe.selfmanagement.ttimer.action;
                                                                                                                                               
import javax.management.*;
import java.io.*;
import java.util.Timer;
import java.util.TimerTask;
                                                                                                                                               
public class TimerOccurrence implements NotificationListener, 
    com.sun.s1peqe.selfmanagement.ttimer.action.TimerOccurrenceMBean {
    
    private final String JMX_TIMER = "timer";
    private static final int TIMER_NO_OF_OCCURRENCES = 3;
    protected static int incOccurrences = 0;

    public TimerOccurrence(){ 
        new NotifThread(this, TIMER_NO_OF_OCCURRENCES).start();
    }
    public int getNumberOfOccurrences() {
        return this.incOccurrences;
    }

    public synchronized void handleNotification(Notification notification,
        Object handback) {
        try {
            if(notification != null) {
                if(notification.getType().equals(JMX_TIMER)) {
                    incOccurrences++;
                }
            }
        } catch (Exception ex) { }
    }
}

class NotifThread extends Thread {
    private int expectedOccurrences;
    private TimerOccurrence timerMBean;
    NotifThread(TimerOccurrence t, int n) {
        this.expectedOccurrences = n;
        this.timerMBean = t;
    }
   
    public void run() {
        try {
            System.out.println("Now going to sleep for 40 secs...");
            sleep(60000);
            FileWriter out = new FileWriter(new File("/space/selfmanagementResult.txt"),true);
            if(timerMBean.getNumberOfOccurrences() != expectedOccurrences) {
                out.write("Timer Event - Test FAILED\n");
            } else {
                out.write("Timer Event - Test PASSED\n");
            }
            out.flush();
            out.close();
        } catch(InterruptedException ex) {
        } catch(Exception ex) {}

    }

}

