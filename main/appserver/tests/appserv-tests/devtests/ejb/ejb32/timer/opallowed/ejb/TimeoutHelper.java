package com.acme.ejb32.timer.opallowed;

import javax.ejb.Timer;
import javax.ejb.TimerHandle;

public class TimeoutHelper {
    // this is to test the APIs allowed to be invoked
    // the return values are not cared
    public static void cancelTimer(TimerHandle th)  {
        if(th == null) {
            return;
        }
        Timer t = th.getTimer();
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
