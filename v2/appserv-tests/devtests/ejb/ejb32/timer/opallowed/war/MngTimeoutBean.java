package com.acme.ejb32.timer.opallowed;

import javax.annotation.ManagedBean;
import javax.ejb.EJB;
import javax.ejb.Timer;
import javax.ejb.TimerHandle;

@ManagedBean("MngBean")
public class MngTimeoutBean {

    @EJB SingletonTimeout singletonTimeout;

    // this is to test the APIs allowed to be invoked
    // the return values are not cared.
    public void cancelTimer() {
        TimerHandle handle = singletonTimeout.createTimer("managedbean");
        Timer t = handle.getTimer();
        verifyTimerAndCancel(t);
    }

    private void verifyTimerAndCancel(Timer t) {
        t.getHandle();
        t.getInfo();
        t.getNextTimeout();
        t.getSchedule();
        t.getTimeRemaining();
        t.isCalendarTimer();
        t.isPersistent();
        t.cancel();
    }

}
