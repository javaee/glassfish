/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.s1asdev.security.timerStandalone;

import javax.annotation.Resource;
import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.annotation.security.RunAs;
import javax.ejb.Remote;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerService;

@Stateless
@Remote({Sless.class})
@DeclareRoles({"dummy"})
@RunAs("dummy")
public class SlessEJB implements Sless
{
    @Resource private TimerService timerSvc;
    @Resource private SessionContext sc;

    private static boolean timeoutWasCalled = false;

    @RolesAllowed("javaee")
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
        sc.isCallerInRole("dummy");
        timeoutWasCalled = true;
    }
    
}
