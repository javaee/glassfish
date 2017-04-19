package com.acme.ejb32.timer.opallowed;

import javax.annotation.Resource;
import javax.ejb.Schedule;
import javax.ejb.ScheduleExpression;
import javax.ejb.Schedules;
import javax.ejb.Singleton;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerConfig;
import javax.ejb.TimerHandle;
import javax.ejb.TimerService;

@Singleton
public class SingletonTimeoutEJB implements SingletonTimeoutLocal, SingletonTimeout {
    @Resource
    TimerService ts;
    
    Timer t;
    
    public TimerHandle createTimer(String info) {
        boolean created = false;
        for(Timer timer:ts.getTimers()) {
            if(timer.getInfo().equals(info)) {
                created = true;
                break;
            }
        }
        if(!created) {
            ScheduleExpression scheduleExpression = new ScheduleExpression().second("*/5").minute("*").hour("*");
            t = ts.createCalendarTimer(scheduleExpression, new TimerConfig(info, true));
        }
        return t.getHandle();
    }

    public Timer createLocalTimer(String info) {
        boolean created = false;
        for(Timer timer:ts.getTimers()) {
            if(timer.getInfo().equals(info)) {
                created = true;
                break;
            }
        }
        if(!created) {
            ScheduleExpression scheduleExpression = new ScheduleExpression().second("*/5").minute("*").hour("*");
            t = ts.createCalendarTimer(scheduleExpression, new TimerConfig(info, true));
        }
        return t;
    }

    @Override
    public void cancelFromHelper() {
        TimeoutHelper.cancelTimer(createTimer("helper"));
    }

    @Timeout
    public void timeout(Timer t) {
        String info = t.getInfo().toString();
        System.out.println(info + " is timeout");
    }

    @Schedules({
            @Schedule(second="*/5", minute="*", hour="*", info="Sglt.schedule.anno"),
    })
    private void schedule(Timer t) {
        System.out.println("SingletonTimeoutEJB.schedule for " + t.getInfo().toString());
    }

}
