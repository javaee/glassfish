/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.s1asdev.deployment.ejb30.ear.security;

import javax.annotation.Resource;
import javax.annotation.security.DeclareRoles;
import javax.annotation.security.DenyAll;
import javax.annotation.security.RolesAllowed;
import javax.annotation.security.RunAs;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.ejb.SessionContext;
import javax.ejb.Stateful;

@RunAs(value="sunuser")
@DeclareRoles({"j2ee", "sunuser"})
@Stateful
public class SfulEJB implements Sful
{
    @EJB private Sless sless;
    @EJB private SlessLocal slessLocal;
    @Resource private SessionContext sc;

    public String hello() {
        System.out.println("In SfulEJB:hello()");

        try {
            slessLocal.goodMorning();
            throw new RuntimeException("Unexpected success from slessLocal.goodMorning()");
        } catch(Exception ex) {
            System.out.println("Expected failure from slessLocal.goodMorning()");
        }

        try {
            slessLocal.goodBye();
            throw new RuntimeException("Unexpected success from slessLocal.goodBye()");
        } catch(EJBException ex) {
            System.out.println("Expected failure from slessLocal.goodBye()");
        }
        
        System.out.println(slessLocal.hello());
        return sless.hello();
    }

    @RolesAllowed({"j2ee"}) 
    public String goodAfternoon() {
        if (!sc.isCallerInRole("j2ee") || sc.isCallerInRole("sunuser")) {
            throw new RuntimeException("not of role j2ee or of role sunuser");
        }
        return "Sful: good afternoon";
    }

    @DenyAll
    public String goodNight() {
        System.out.println("In SfulEJB:goodNight()");
        return "goodNight";
    }
}
