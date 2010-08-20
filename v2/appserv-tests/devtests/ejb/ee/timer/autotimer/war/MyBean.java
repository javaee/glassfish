/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.acme;

import java.util.Collection;
import javax.ejb.*;
import javax.annotation.Resource;

/**
 *
 * @author marina vatkina
 */

@Singleton
public class MyBean {

    @Resource TimerService ts;
    private volatile int counter = 0;

    @Schedule(second="*/5", minute="*", hour="*", info = "timer01")
    private void scheduledtimeout(Timer t) {
        test(t, "timer01");
    }

    @Timeout
    private void requestedtimeout(Timer t) {
        test(t, "timer02");
    }

    private void test(Timer t, String name) {
        if (t.getInfo().equals(name)) {
            System.err.println("In ___MyBean:timeout___ "  + t.getInfo() + " - persistent: " + t.isPersistent());
            counter++;
        } else {
            throw new RuntimeException("Wrong " + t.getInfo() + " timer was called");
        }

    }

    public boolean timeoutReceived() {
        System.err.println("In ___MyBean:timeoutReceived___ " + counter + " times");
        int result = counter;
        Collection<Timer> timers = ts.getTimers();
        for (Timer t : timers) {
            t.cancel();
        }
        ts.createTimer(1000, 5000, "timer02");
        counter = 0;
        return (result > 0);
    }
}
