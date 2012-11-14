package com.acme;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import javax.ejb.*;
import javax.annotation.Resource;

@Singleton
public class SgltTimerBean {
    private static Set<String> expected_infos = new HashSet<String>();
    private static Set<String> errors = new HashSet<String>();

    static {
        expected_infos.add("StlesTimerBean.timer01.p");
        expected_infos.add("StlesTimerBean.timer01.t");
        expected_infos.add("StlesTimerBean.timer02.p");
        expected_infos.add("StlesTimerBean.timer02.t");
        expected_infos.add("SgltTimerBean.timer01.p");
        expected_infos.add("SgltTimerBean.timer01.t");
        expected_infos.add("SgltTimerBean.timer02.p");
        expected_infos.add("SgltTimerBean.timer02.t");
    }

    @Resource
    TimerService ts;

    @Schedules({
            @Schedule(second = "*/5", minute = "*", hour = "*", info = "SgltTimerBean.timer01.p"),
            @Schedule(second = "*/5", minute = "*", hour = "*", info = "SgltTimerBean.timer01.t", persistent = false)
    })
    private void scheduledTimeout(Timer t) {
        test(t, "SgltTimerBean.timer01");
    }

    @Timeout
    private void programmaticTimeout(Timer t) {
        test(t, "SgltTimerBean.timer02");
    }

    private void test(Timer t, String name) {
        if (((String) t.getInfo()).startsWith(name)) {
            System.err.println("In SgltTimerBean:timeout___ " + t.getInfo() + " - persistent: " + t.isPersistent());
        } else {
            throw new RuntimeException("Wrong " + t.getInfo() + " timer was called");
        }

    }

    public void createProgrammaticTimer() {
        System.err.println("In SgltTimerBean:createProgrammaticTimer__ ");
        for (Timer t : ts.getTimers()) {
            if (t.getInfo().toString().contains("SgltTimerBean.timer02")) {
                System.err.println("programmatic timers are already created for SgltTimerBean");
                return;
            }
        }
        ts.createTimer(1000, 5000, "SgltTimerBean.timer02.p");
        ScheduleExpression scheduleExpression = new ScheduleExpression().minute("*").hour("*").second("*/5");
        ts.createCalendarTimer(scheduleExpression, new TimerConfig("SgltTimerBean.timer02.t", false));
    }

    public int countAllTimers(String param) {
        Collection<Timer> timers = ts.getAllTimers();
        System.err.println("In SgltTimerBean:allTimersFound___ " + timers);
        printTimers(timers);
        return verifyTimers(timers, param);
    }

    private void printTimers(Collection<Timer> timers) {
        StringBuffer sb = new StringBuffer("<");
        for(Timer t:timers){
            sb.append(t);
            sb.append(":");
            sb.append(t.getInfo());
            sb.append(", ");
        }
        sb.append(">") ;
        System.err.println(sb.toString());
    }

    private int verifyTimers(Collection<Timer> ts, String param) {
        if("28081".equals(param)) {
            // called from c1in2, remove non-persistent programmatic timers from expectation
            expected_infos.remove("StlesTimerBean.timer02.t");
            expected_infos.remove("SgltTimerBean.timer02.t");
        }

        for(Timer t : ts) {
            String info = "" + t.getInfo();
            if (!expected_infos.contains(info)) {
                errors.add(info);
            }
        }

        if (ts.size() != expected_infos.size()) {
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
        return ts.size();
    }
}
