package com.sun.s1asdev.ejb31.timer.schedule_ann;


import javax.ejb.*;
import javax.annotation.Resource;

import java.util.Map;
import java.util.Set;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Date;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.concurrent.atomic.AtomicBoolean;

@Stateless
public class StlesEJB implements Stles {

    @Resource
    private TimerService timerSvc;

    private static Map<String, ScheduleExpression> hm = Collections.synchronizedMap(new HashMap<String, ScheduleExpression>());
    private static Set<String> callers = Collections.synchronizedSet(new HashSet<String>());
    private static Set<String> expected_callers = Collections.synchronizedSet(new HashSet<String>());
    private static Set<String> errors = Collections.synchronizedSet(new HashSet<String>());

    private static AtomicBoolean verifying = new AtomicBoolean(false);

    static {

        ScheduleExpression se = new ScheduleExpression().second("*/5").minute("*").hour("*");
        expected_callers.add("timer-5-sec");
        hm.put("timer-5-sec", se);

        se = new ScheduleExpression().second("*").minute("*").hour("*");
        expected_callers.add("timer-every-sec");
        hm.put("timer-every-sec", se);

        se = new ScheduleExpression().second("2/2").minute(" *   ").hour("*").dayOfWeek("0-7");
        expected_callers.add("another-timer-dayOfWeek=0-7");
        hm.put("another-timer-dayOfWeek=0-7", se);

        se = new ScheduleExpression().second("3/2").minute(" *   ").hour("12-11");
        expected_callers.add("timer-NO-ARG-hour=12-11");
        hm.put("timer-NO-ARG-hour=12-11", se);

        se = new ScheduleExpression().second("*/2").minute(" *   ").hour("*");
        expected_callers.add("dd_timer1");
        hm.put("dd_timer1", se);

        se = new ScheduleExpression().second("*/4").minute(" *   ").hour("*");
        expected_callers.add("dd_timer2");
        hm.put("dd_timer2", se);

        se = new ScheduleExpression().second("*").minute("*").hour("*").year("3000,2007, 4096");
        hm.put("timer-year=3000,2007, 4096", se);

        se = new ScheduleExpression().second("*").minute("*").hour("*").dayOfMonth("Last");
        hm.put("timer-dayOfMonth=Last", se);

        se = new ScheduleExpression().second("*").minute("*").hour("*").dayOfMonth("Last Sun");
        hm.put("timer-dayOfMonth=Last Sun", se);

        se = new ScheduleExpression().second("*").minute("*").hour("*").dayOfMonth("-4--2");
        hm.put("timer-dayOfMonth=-4--2", se);

        se = new ScheduleExpression().dayOfMonth(29).month("FEB").
                year("2007-2011, 2013-2015, 2017-2019");
        hm.put("timer-02-29-non-leap-year", se);

    }

    public void verifyTimers() {
        verifying.set(true);

        if (!errors.isEmpty()) {
            StringBuffer sb = new StringBuffer();
            for (String e : errors) {
                sb.append("" + e).append(", ");
            }
            throw new EJBException("Timers SHOULD NOT expire for infos: " + sb.toString() );
        }

        Collection<Timer> ts = timerSvc.getTimers();
        for(Timer t : ts) {
            String info = "" + t.getInfo();
            t.cancel();
            ScheduleExpression s = hm.remove(info);
            if (s == null) {
                errors.add(info);
            }
        }
        
        if (!expected_callers.isEmpty()) {
            System.out.println("Missed info count: " + expected_callers.size());
            StringBuffer sb = new StringBuffer();
            for (String c : expected_callers) {
                System.out.println("Missed info: " + c);
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

    private void verifyTimer(Timer t) {
        System.out.println("in StlesEJB:timeout "  + t.getInfo() + " - persistent: " + t.isPersistent());

        verifyTimer(t, hm.get("" + t.getInfo()), null);

        callers.add("" + t.getInfo());
        expected_callers.remove("" + t.getInfo());
    }

    private void verifyTimer(Timer t, ScheduleExpression se0, TimerConfig tc0) {
        String info = "" + t.getInfo();
        if (!t.isCalendarTimer()) {
                throw new IllegalStateException("Timer " + info + " is not schedule based timer");
        }

        if (tc0 != null) {
            boolean p = t.isPersistent();
            if (p != tc0.isPersistent()) {
                throw new IllegalStateException("Timer " + info + " persistence is not as expected");
            }
            assertSame(info, tc0.getInfo(), "" + info, "INFO");
        }

        ScheduleExpression tse = t.getSchedule();
        if (tse == null) {
            throw new IllegalStateException("Timer Schedule for " + info + " is NULL!");
        }
        if (se0 == null) {
            throw new IllegalStateException("Unknown Schedule for " + info );
        }
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

    @Schedules ({
        @Schedule(second="*/5", minute="*", hour="*", info="timer-5-sec"),
        @Schedule(second="*", minute="*", hour="*", info="timer-every-sec"),
        @Schedule(second="*", minute="*", hour="*", year="3000,2007, 4096", info="timer-year=3000,2007, 4096"),
        @Schedule(second="*", minute="*", hour="*", dayOfMonth="Last", info="timer-dayOfMonth=Last"),
        @Schedule(second="*", minute="*", hour="*", dayOfMonth="Last Sun", info="timer-dayOfMonth=Last Sun"),
        @Schedule(second="*", minute="*", hour="*", dayOfMonth="-4--2", info="timer-dayOfMonth=-4--2"),
        @Schedule(dayOfMonth="29", month="FEB", year="2007-2011, 2013-2015, 2017-2019", info="timer-02-29-non-leap-year")
    })
    private void timeout(Timer t) {
        if (("" + t.getInfo()).startsWith("another") || ("" + t.getInfo()).startsWith("dd")) {
            errors.add("" + t.getInfo());
            throw new IllegalStateException("Timer " + t.getInfo() + " is wrong for this timeout");
        }
        if (!verifying.get()) { // skip if we are verifying calls at this time
            verifyTimer(t);
        }
    }

    @Schedule(second="2/2", minute="*", hour="*", dayOfWeek="0-7", info="another-timer-dayOfWeek=0-7")
    private void anothertimeout(Timer t) {
        if (!("" + t.getInfo()).startsWith("another")) {
            errors.add("" + t.getInfo());
            throw new IllegalStateException("Timer " + t.getInfo() + " is wrong for this timeout");
        }
        if (!verifying.get()) { // skip if we are verifying calls at this time
            verifyTimer(t);
        }
    }

    @Schedule(second="3/2", minute=" *   ", hour="12-11", info="timer-NO-ARG-hour=12-11")
    private void timeout() {

        System.out.println("in StlesEJB:NO-ARG timeout "  );
        if (!verifying.get()) { // skip if we are verifying calls at this time
            ScheduleExpression se = hm.get("timer-NO-ARG-hour=12-11");
            if (se != null) {
                callers.add("timer-NO-ARG-hour=12-11");
                expected_callers.remove("timer-NO-ARG-hour=12-11");
            }
        }

    }

    @Schedule(second="4/2", minute="*", hour="*", dayOfWeek="0-7", info="overridden-timer")
    private void ddTimeout(Timer t) {
        if (!("" + t.getInfo()).startsWith("dd_timer")) {
            errors.add("" + t.getInfo());
            throw new IllegalStateException("Timer " + t.getInfo() + " is wrong for this timeout");
        }
        if (!verifying.get()) { // skip if we are verifying calls at this time
            verifyTimer(t);
        }
    }

}
