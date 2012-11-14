package com.acme;

import javax.annotation.Resource;
import javax.ejb.Schedule;
import javax.ejb.ScheduleExpression;
import javax.ejb.Schedules;
import javax.ejb.Singleton;
import javax.ejb.Stateless;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;
import java.util.HashSet;

@Stateless
public class StlesTimerBean {
    @Resource
    TimerService ts;

    @Schedules({
            @Schedule(second = "*/5", minute = "*", hour = "*", info = "StlesTimerBean.timer01.p"),
            @Schedule(second = "*/5", minute = "*", hour = "*", info = "StlesTimerBean.timer01.t", persistent = false)
    })
    private void scheduledTimeout(Timer t) {
        test(t, "StlesTimerBean.timer01");
    }

    @Timeout
    private void programmaticTimeout(Timer t) {
        test(t, "StlesTimerBean.timer02");
    }

    private void test(Timer t, String name) {
        if (((String) t.getInfo()).startsWith(name)) {
            System.err.println("In StlesTimerBean:timeout___ " + t.getInfo() + " - persistent: " + t.isPersistent());
        } else {
            throw new RuntimeException("Wrong " + t.getInfo() + " timer was called");
        }

    }

    public void createProgrammaticTimer() {
        System.err.println("In StlesTimerBean:createProgrammaticTimer__ ");
        for (Timer t : ts.getTimers()) {
            if (t.getInfo().toString().contains("StlesTimerBean.timer02")) {
                System.err.println("programmatic timers are already created for StlesTimerBean");
                return;
            }
        }
        ts.createTimer(1000, 5000, "StlesTimerBean.timer02.p");
        ScheduleExpression scheduleExpression = new ScheduleExpression().minute("*").hour("*").second("*/5");
        ts.createCalendarTimer(scheduleExpression, new TimerConfig("StlesTimerBean.timer02.t", false));
    }
}
