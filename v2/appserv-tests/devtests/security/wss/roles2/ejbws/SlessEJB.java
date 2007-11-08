/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.s1asdev.security.wss.roles2.ejb;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Local;
import javax.ejb.Stateless;

@Stateless
@Local({SlessLocal.class})
public class SlessEJB implements SlessLocal {
    @RolesAllowed(value={"j2ee"})
    public String hello() {
        return "hello from Sless";
    }

    @RolesAllowed(value={"javaee"})
    public String goodBye() {
        return "goodBye from Sless";
    }
}
