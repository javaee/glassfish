/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.s1asdev.security.wss.roles2.ejb;

import javax.annotation.security.RolesAllowed;
import javax.annotation.security.RunAs;
import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Stateful;

@Stateful
@Local({SfulLocal.class})
@RunAs("j2ee")
public class SfulEJB implements SfulLocal {
    @EJB private SlessLocal sless;
 
    @RolesAllowed(value={"javaee"})
    public String hello() {
        return "hello from Sful";
    }

    @RolesAllowed(value={"ejbuser"})
    public String goodBye() {
        return "goodBye from Sful";
    }

    public String slessHello() {
        return sless.hello();
    }

    public String slessGoodBye() {
        return sless.goodBye();
    }
}
