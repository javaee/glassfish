package com.sun.s1asdev.ejb31.timer.schedule_exp;


import javax.ejb.*;
import javax.interceptor.AroundTimeout;
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

    private static Map<String, ScheduleExpression> hm = new HashMap<String, ScheduleExpression>();
    private static Set<String> callers = new HashSet<String>();
    private static Set<String> expected_callers = new HashSet<String>();

    public void createTimers() throws Exception {

        Calendar now = new GregorianCalendar();
        int month = (now.get(Calendar.MONTH) + 1); // Calendar starts with 0
        int dayOfMonth = now.get(Calendar.DAY_OF_MONTH);

        System.out.println("createTimers(): creating timer with 5 sec ");
        ScheduleExpression se = new ScheduleExpression().second("*/5").minute("*").hour("*");
        TimerConfig tc = new TimerConfig("timer-5-sec", true);
        createTimer(se, tc);
        expected_callers.add("" + tc.getInfo());

        int dayOfWeek = (now.get(Calendar.DAY_OF_WEEK) - 1); // SUN is 1
        System.out.println("createTimers(): creating timer with dayOfWeek=today-today (" 
                + dayOfWeek + "-" + dayOfWeek + ") ");
        se = new ScheduleExpression().second("2 /    5").minute("*").hour("*").
                dayOfWeek("" + dayOfWeek+" -  " + dayOfWeek);
        tc = new TimerConfig("timer-dayOfWeek=" + dayOfWeek + "-" + dayOfWeek, false);
        createTimer(se, tc);
        expected_callers.add("" + tc.getInfo());

        System.out.println("createTimers(): creating timer with dayOfWeek=0-7 ");
        se = new ScheduleExpression().second("2/2").minute(" *   ").hour("*").dayOfWeek("0-7");
        tc = new TimerConfig("timer-dayOfWeek=0-7", true);
        createTimer(se, tc);
        expected_callers.add("" + tc.getInfo());

        int hour = now.get(Calendar.HOUR_OF_DAY);
        int h0 = (hour < 2) ? 24 : hour;
        int h1 = (hour > 21) ? hour - 24 : hour;

        String s = "" + (h0 - 1) + " - " + (h1 + 2);
        int second = now.get(Calendar.SECOND);
        if (second >= 55) {
            second -= 60;
        }

        System.out.println("createTimers(): creating timer with second=" + (second + 5) 
                + ", hour=" + s );
        se = new ScheduleExpression().second(second + 5).minute("*").hour(s);

        String s1 = "" + (h0 - 1) + "-" + (h1 + 2);
        tc = new TimerConfig("timer-hour=" + s1, false);
        createTimer(se, tc);
        expected_callers.add("" + tc.getInfo());

        System.out.println("createTimers(): creating timer with year=3000,2007, 4096 ");
        se = new ScheduleExpression().second("*").minute("*").hour("*").year("3000,2007, 4096");
        tc = new TimerConfig("timer-year=3000,2007, 4096", true);
        createTimer(se, tc);

        System.out.println("createTimers(): creating timer with dayOfMonth=Last ");
        se = new ScheduleExpression().second("*").minute("*").hour("*").dayOfMonth("Last");
        tc = new TimerConfig("timer-dayOfMonth=Last", false);
        createTimer(se, tc);

        System.out.println("createTimers(): creating timer with dayOfMonth=Last Sun ");
        se = new ScheduleExpression().second("*").minute("*").hour("*").dayOfMonth("Last Sun");
        tc = new TimerConfig("timer-dayOfMonth=Last Sun", true);
        createTimer(se, tc);

        System.out.println("createTimers(): creating timer with dayOfMonth=-4--2 ");
        se = new ScheduleExpression().second("*").minute("*").hour("*").dayOfMonth("-4--2");
        tc = new TimerConfig("timer-dayOfMonth=-4--2", false);
        createTimer(se, tc);

        System.out.println("createTimers(): creating never expired (Feb 29 non-leap year) timer ");
        se = new ScheduleExpression().dayOfMonth(29).month("FEB").
                year("2007-2011, 2013-2015, 2017-2019");
        tc = new TimerConfig("timer-02-29-non-leap-year", true);
        createTimer(se, tc);

        try {
            System.out.println("createTimers(): creating wrong (year before 1971) timer ");
            se = new ScheduleExpression().year("1966");
            tc = new TimerConfig("timer-1966", true);
            createTimer(se, tc);
            throw new EJBException("Timer with year before 1971 was created");
        } catch (IllegalArgumentException e) {
            System.out.println("createTimers(): caught expected IllegalArgumentException");
        }

        try {
            System.out.println("createTimers(): creating wrong (month 1/5) timer ");
            se = new ScheduleExpression().month("1/5");
            tc = new TimerConfig("timer-1/5", true);
            createTimer(se, tc);
            throw new EJBException("Timer with month 1/5 was created");
        } catch (IllegalArgumentException e) {
            System.out.println("createTimers(): caught expected IllegalArgumentException");
        }

        try {
            System.out.println("createTimers(): creating wrong (seconds -5) timer ");
            se = new ScheduleExpression().second("-5");
            tc = new TimerConfig("timer-negative-seconds-5", true);
            createTimer(se, tc);
            throw new EJBException("Timer with negative 5 seconds was created");
        } catch (IllegalArgumentException e) {
            System.out.println("createTimers(): caught expected IllegalArgumentException");
        }

        try {
            System.out.println("createTimers(): creating wrong (seconds AB) timer ");
            se = new ScheduleExpression().second("AB");
            tc = new TimerConfig("timer-negative-secondsAB", true);
            createTimer(se, tc);
            throw new EJBException("Timer with seconds AB was created");
        } catch (IllegalArgumentException e) {
            System.out.println("createTimers(): caught expected IllegalArgumentException");
        }

        try {
            System.out.println("createTimers(): creating wrong (month ABC) timer ");
            se = new ScheduleExpression().month("ABC");
            tc = new TimerConfig("timer-month-ABC", true);
            createTimer(se, tc);
            throw new EJBException("Timer with month ABC was created");
        } catch (IllegalArgumentException e) {
            System.out.println("createTimers(): caught expected IllegalArgumentException");
        }

    }

    public void verifyTimers() {
        Collection<Timer> ts = timerSvc.getTimers();
        Set<String> errors = new HashSet<String>();
        for(Timer t : ts) {
            ScheduleExpression s = hm.remove("" + t.getInfo());
            if (s == null) {
                errors.add("" + t.getInfo());
            }
            t.cancel();
        }
        
        if (!expected_callers.isEmpty()) {
            StringBuffer sb = new StringBuffer();
            for (String c : expected_callers) {
                sb.append(c).append(", ");
            }
            throw new EJBException("Timers DID NOT expire for infos: " + sb.toString() );
        }

        if (callers.isEmpty()) {
            throw new EJBException("NO Timers expired!");

        } else {
            StringBuffer sb = new StringBuffer();
            for (String c : callers) {
                sb.append(c).append(", ");
            }
            System.out.println("Timers expired for infos: " + sb.toString() );
        }

        if (!errors.isEmpty()) {
            StringBuffer sb = new StringBuffer();
            for (String e : errors) {
                sb.append("" + e).append(", ");
            }
            throw new EJBException("Internal error: ScheduleExpressions for infos: " 
                    + sb.toString() + " were not available at verify!");
        }

        if (timerSvc.getTimers().size() != 0) {
            throw new EJBException("After cencel, timerSvc.getTimers().size() = " 
                    + timerSvc.getTimers().size());
        }
    }

    private void createTimer(ScheduleExpression se, TimerConfig tc) throws Exception {
        Timer t = timerSvc.createCalendarTimer(se, tc);

        verifyTimer(t, se, tc);
        hm.put("" + tc.getInfo(), se);
        System.out.println("Created timer #" + timerSvc.getTimers().size());
    }

    private void verifyTimer(Timer t) {
        verifyTimer(t, hm.get("" + t.getInfo()), null);
    }

    private void verifyTimer(Timer t, ScheduleExpression se0, TimerConfig tc0) {
        String info = "" + t.getInfo();
        if (tc0 != null) {
            boolean p = t.isPersistent();
            if (p != tc0.isPersistent()) {
                throw new IllegalStateException("Timer " + info + " persistence is not as expected");
            }
            assertSame(info, tc0.getInfo(), "" + info, "INFO");
        }

        ScheduleExpression tse = t.getSchedule();
        if (!tse.getSecond().trim().equals(se0.getSecond().trim()) ) {
            throw new IllegalStateException("Timer " + info + " SECONDS is not as expected: " + tse.getSecond());
        }
        if (!tse.getMinute().trim().equals(se0.getMinute().trim()) ) {
            throw new IllegalStateException("Timer " + info + " MINUTES is not as expected: " + tse.getMinute());
        }
        if (!tse.getHour().trim().equals(se0.getHour().trim()) ) {
            throw new IllegalStateException("Timer " + info + " HOUR is not as expected: " + tse.getHour());
        }
        if (!tse.getDayOfMonth().trim().equals(se0.getDayOfMonth().trim()) ) {
            throw new IllegalStateException("Timer " + info + " DAY_OF_MONTH is not as expected: " + tse.getDayOfMonth());
        }
        if (!tse.getMonth().trim().equals(se0.getMonth().trim()) ) {
            throw new IllegalStateException("Timer " + info + " MONTH is not as expected: " + tse.getMonth());
        }
        if (!tse.getDayOfWeek().trim().equals(se0.getDayOfWeek().trim()) ) {
            throw new IllegalStateException("Timer " + info + " DAY_OF_WEEK is not as expected: " + tse.getDayOfWeek());
        }
        if (!tse.getYear().trim().equals(se0.getYear().trim()) ) {
            throw new IllegalStateException("Timer " + info + " YEAR is not as expected: " + tse.getYear());
        }
        assertSame(tse.getTimezone(), se0.getTimezone(), "" + info, "TZ");
        assertSame(tse.getStart(), se0.getStart(), "" + info, "START");
        assertSame(tse.getEnd(), se0.getEnd(), "" + info, "END");
    }

    private void assertSame(Object test, Object orig, String info, String name) {
        if (test != null && orig == null) {
            throw new IllegalStateException("Timer " + info + " " + name + " is not null as expected");
        }
        if (test == null && orig != null) {
            throw new IllegalStateException("Timer " + info + " " + name + " is null");
        }
        if (test != null && !test.equals(orig)) {
            throw new IllegalStateException("Timer " + info + " " + name + " is not as expected: " + test);
        }
    }

    @Timeout
    public void timeout(Timer t) {

        System.out.println("in StlesEJB:timeout "  + t.getInfo() + " - persistent: " + t.isPersistent());
        if (!t.isCalendarTimer()) {
                throw new IllegalStateException("Timer " + t.getInfo() + " is not schedule based timer");
        }
        verifyTimer(t);

        callers.add("" + t.getInfo());
        expected_callers.remove("" + t.getInfo());
    }

    @AroundTimeout
    private Object xxx(InvocationContext ctx) throws Exception {
        System.out.println("in StlesEJB:xxx "  + ((Timer)ctx.getTimer()).getInfo() );
        return ctx.proceed();
    }
}
