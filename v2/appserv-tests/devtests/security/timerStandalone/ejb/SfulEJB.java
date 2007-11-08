/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.s1asdev.security.timerStandalone;

import javax.ejb.Stateful;
import javax.ejb.Remote;

@Stateful
@Remote({Sful.class})
public class SfulEJB implements Sful
{

    public String hello() {
        System.out.println("In SfulEJB:hello()");
        return "hello";
    }

}
