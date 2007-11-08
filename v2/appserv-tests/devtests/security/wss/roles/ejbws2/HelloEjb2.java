/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.s1asdev.security.wss.roles.ejbws2;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.jws.WebService;

@Stateless
@WebService(targetNamespace="http://ejbws2.roles.wss.security.s1asdev.sun.com", serviceName="WssRolesEjb2Service")
public class HelloEjb2 {
    @RolesAllowed(value={"javaee"})
    public String rolesAllowed1(String who) {
        return "Hello, " + who;
    }
    
    @RolesAllowed(value={"webuser"})
    public String rolesAllowed2(String who) {
        return "Hello, " + who;
    }
}
