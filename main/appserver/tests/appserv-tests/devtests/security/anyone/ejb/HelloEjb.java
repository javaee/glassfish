/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.s1asdev.security.anyone.ejb;

import javax.annotation.Resource;
import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Remote;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;

@Stateless
@Remote({Hello.class})
@DeclareRoles({"javaee", "nobody"})
public class HelloEjb implements Hello {
    @Resource private SessionContext sc;

    @RolesAllowed(value={"ANYONE"})
    public String hello(String msg) {
        if (!sc.isCallerInRole("javaee") || sc.isCallerInRole("nobody")) {
            throw new RuntimeException("Not of role javaee or is of role nobody");
        }

        return "Hello , " + msg;
    }
}
