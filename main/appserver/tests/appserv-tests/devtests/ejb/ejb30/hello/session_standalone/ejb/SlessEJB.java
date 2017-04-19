/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.s1asdev.ejb.ejb30.hello.session_standalone;

import javax.ejb.*;
import javax.annotation.Resource;

@Stateless
@Remote({Sless.class})
public class SlessEJB implements Sless
{
    @Resource TimerService timerSvc;

    private static boolean timeoutWasCalled = false;

    public String hello() {
        System.out.println("In SlessEJB:hello()");
        timerSvc.createTimer(1, "timer");
        return "hello";
    }

    public boolean timeoutCalled() {
        return timeoutWasCalled;
    }

    @Timeout 
    private void timeout(Timer t) {

        System.out.println("in SlessEJB:timeout");
        timeoutWasCalled = true;

    }
    
}
