package com.acme.ejb32.timer.getalltimers;

import javax.annotation.Resource;
import javax.ejb.Schedule;
import javax.ejb.Schedules;
import javax.ejb.Singleton;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

@Singleton
public class SingletonTimeoutEJB {
    @Resource
    TimerService ts;

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void createTimerForTimeout() {
        ts.createIntervalTimer(1, 1000*5, new TimerConfig("Sglt.timeout", true));
        ts.createIntervalTimer(1, 1000*5, new TimerConfig("Sglt.timeout.nonpersist", false));
    }

    @Timeout
    private void timeout(Timer t) {
        System.out.println("SingletonTimeoutEJB.timeout for " + t.getInfo().toString());
    }

    @Schedules({
            @Schedule(second="*/5", minute="*", hour="*", info="Sglt.schedule.anno"),
            @Schedule(second="*/5", minute="*", hour="*", info="Sglt.schedule.anno.nonpersist", persistent = false)
    })
    private void schedule(Timer t) {
        System.out.println("SingletonTimeoutEJB.schedule for " + t.getInfo().toString());
    }

}
