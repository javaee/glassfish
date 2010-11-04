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

    @Schedule(second="*/5", minute="*", hour="*", info = "timer01")
    private void scheduledtimeout(Timer t) {
            System.err.println("In ___MyBean:timeout___ "  + t.getInfo() + " - persistent: " + t.isPersistent());
    }

    public int countTimers() {
        int result = ts.getTimers().size();
        System.err.println("In ___MyBean:timersFound___ " + result);
        return result;
    }
}
