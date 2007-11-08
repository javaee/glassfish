/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.s1asdev.security.sameEjbName.ejb;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Remote;
import javax.ejb.Stateless;

@Stateless
@Remote({Hello.class})
public class HelloEjb implements Hello {
    @RolesAllowed(value={"javaee"})
    public String rolesAllowed1(String who) {
        return "Hello rolesAllowed1, " + who;
    }
    
    @RolesAllowed(value={"ejbuser"})
    public String rolesAllowed2(String who) {
        return "Hello rolesAllowed2, " + who;
    }
}
