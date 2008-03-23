package com.sun.ejb.containers;

/**
 * @author Mahesh Kannan
 *         Date: Mar 7, 2008
 */
public class EJBTimerService {

    public void timedObjectCount() {
    
    }

    public boolean postEjbTimeout(TimerPrimaryKey key) {
        return true;
    }

    public void taskExpired(TimerPrimaryKey key) {

    }

    public void destroyTimers(long containerId) {

    }

}
