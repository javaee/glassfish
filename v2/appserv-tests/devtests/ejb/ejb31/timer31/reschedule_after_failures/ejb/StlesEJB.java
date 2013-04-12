package com.sun.s1asdev.ejb31.timer.reschedule_after_failures;


import javax.ejb.*;
import javax.interceptor.InvocationContext;
import javax.annotation.Resource;

import java.util.Map;
import java.util.Set;
import java.util.Collection;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Date;
import java.util.Calendar;
import java.util.GregorianCalendar;

@Stateless
public class StlesEJB implements Stles {

    @Resource
    private TimerService timerSvc;

    private static volatile int i = 0;
    private static volatile boolean b = false;

    public void createTimers() throws Exception {

        Calendar now = new GregorianCalendar();
        int month = (now.get(Calendar.MONTH) + 1); // Calendar starts with 0
        int dayOfMonth = now.get(Calendar.DAY_OF_MONTH);

        System.out.println("createTimers(): creating timer with 8 sec ");
        ScheduleExpression se = new ScheduleExpression().second("*/8").minute("*").hour("*");
        TimerConfig tc = new TimerConfig("timer-8-sec", true);
        timerSvc.createCalendarTimer(se, tc);
    }

    public void verifyTimers() {
        if (!b) {
            throw new EJBException("Timer was not rescheduled!");
        }
    }

    @Timeout
    public void timeout(Timer t) {

        System.out.println("in StlesEJB:timeout "  + t.getInfo() + " - persistent: " + t.isPersistent());
        if (i < 2) {
            i++;
            throw new RuntimeException("Failing number " + i);
        }
        b = true;
    }

}
