package com.acme.ejb32.timer.getalltimers;


import javax.ejb.*;
import javax.annotation.Resource;

import java.util.Map;
import java.util.Set;
import java.util.Collection;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Date;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.concurrent.atomic.AtomicBoolean;

@Stateless
public class StlesTimeoutEJB implements StlesTimeout {

    @Resource
    private TimerService timerSvc;

    static Set<String> expected_infos = new HashSet<String>();

    /*
    * this doesn't include "Sglt.timeout" and "Sglt.timeout.nonpersist", instead,
    * it includes "Stles.timeout.cancel" and "Stles.timeout.nonpersist" because programmatic
    * timers of stateless bean are tested here. and "Stles.timeout.cancel" will be cancelled
    * during timeout.
    */
    static {
        expected_infos.add("Stles.schedule.anno");
        expected_infos.add("Stles.schedule.anno.nonpersist");
        expected_infos.add("Stles.timeout.cancel");
        expected_infos.add("Stles.timeout.nonpersist");
        expected_infos.add("Sglt.schedule.anno");
        expected_infos.add("Sglt.schedule.anno.nonpersist");
    }

    private static Set<String> errors = new HashSet<String>();

    public void verify() {
        verifyAllTimers();

        try {
            // waiting for cancellation
            Thread.sleep(15000);
        } catch (InterruptedException e) {
            throw new EJBException(e);
        }

        expected_infos.remove("Stles.timeout.cancel");
        verifyAllTimers();
    }

    private void verifyAllTimers() {

        Collection<Timer> ts = timerSvc.getAllTimers();
        for(Timer t : ts) {
            String info = "" + t.getInfo();
            if (!expected_infos.contains(info)) {
                errors.add(info);
            }
        }

        if (ts.size() != expected_infos.size()) {
            StlesNonTimeoutEJB.printTimerInfos(ts);
            throw new EJBException("timerSvc.getAllTimers().size() = "
                    + ts.size() + " but we expect " + expected_infos.size());
        }

        if (!errors.isEmpty()) {
            StringBuffer sb = new StringBuffer();
            for (String e : errors) {
                sb.append("" + e).append(", ");
            }
            throw new EJBException("Timers SHOULD NOT found for infos: " + sb.toString() );
        }
    }

    @Override
    public void createProgrammaticTimers() {
        System.out.println("SltesTimeoutEJB.createTimerForTimeout");
        ScheduleExpression scheduleExpression = new ScheduleExpression().second("*").minute("*").hour("*");
        Timer t = timerSvc.createCalendarTimer(scheduleExpression, new TimerConfig("Stles.timeout.cancel", true));
        t = timerSvc.createIntervalTimer(1, 1000*10, new TimerConfig("Stles.timeout.nonpersist", false));
        System.out.println("SltesTimeoutEJB.createTimerForTimeout finished");
    }
    @Timeout
    public void timeout(Timer t) {
        System.out.println("SltesTimeoutEJB.timeout");
        String info = t.getInfo().toString();
        if (info.contains("cancel")) {
            t.cancel();
            System.out.println(info + " is cancelled");
        }
    }

    @Schedules({
            @Schedule(second="*/5", minute="*", hour="*", info="Stles.schedule.anno"),
            @Schedule(second="*/5", minute="*", hour="*", info="Stles.schedule.anno.nonpersist", persistent = false)
    })
    private void schedule(Timer t) {
        System.out.println("StlesTimeoutEJB.schedule for " + t.getInfo().toString());
    }

}
