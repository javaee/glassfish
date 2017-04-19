/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.s1asdev.security.wss.roles.ejb;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Local;
import javax.ejb.Stateful;

@Stateful
@Local({SfulLocal.class})
public class SfulEJB implements SfulLocal {
    @RolesAllowed(value={"javaee"})
    public String hello() {
        return "hello from Sful";
    }

    @RolesAllowed(value={"ejbuser"})
    public String goodBye() {
        return "goodBye from Sful";
    }
}
