/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.s1asdev.security.wss.roles2.ejbws;

import javax.annotation.Resource;
import javax.annotation.security.DeclareRoles;
import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.annotation.security.RunAs;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.SessionContext;
import javax.jws.WebService;

import com.sun.s1asdev.security.wss.roles2.ejb.SfulLocal;

@Stateless
@WebService(targetNamespace="http://ejbws.roles2.wss.security.s1asdev.sun.com", serviceName="WssRoles2EjbService")
@DeclareRoles({"javaee", "webuser", "ejbuser"})
@RunAs("ejbuser")
public class HelloEjb {
    @EJB private SfulLocal sful;
    @Resource private SessionContext sc;

    public String hello(String who) {
        if (!sc.isCallerInRole("javaee")) {
            throw new RuntimeException("not of role javaee");
        }
        if (sc.isCallerInRole("ejbuser")) {
            throw new RuntimeException("of role ejbuser");
        }
        return "Hello, " + who;
    }

    @RolesAllowed(value={"javaee"})
    public String rolesAllowed1(String who) {
        return "Hello, " + who;
    }
    
    @RolesAllowed(value={"webuser"})
    public String rolesAllowed2(String who) {
        return "Hello, " + who;
    }

    @DenyAll
    public String denyAll(String who) {
        return "Hello, " + who;
    }

    @PermitAll
    public String permitAll(String who) {
        return "Hello, " + who;
    }    

    public String runAs1() {
        return sful.hello();
    }

    public String runAs2() {
        return sful.goodBye();
    }

    @RolesAllowed(value={"javaeegp"})
    public String runAsRunAs1() {
        return sful.slessHello();
    }

    public String runAsRunAs2() {
        return sful.slessGoodBye();
    }
}
