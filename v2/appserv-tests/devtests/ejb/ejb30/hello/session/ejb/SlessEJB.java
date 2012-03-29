/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.s1asdev.ejb.ejb30.hello.session;

import javax.ejb.Stateless;

@Stateless
public class SlessEJB implements Sless
{

    @javax.annotation.Resource javax.ejb.TimerService ts;

    public String hello() {
        System.out.println("In SlessEJB:hello()");
        return "hello";
    }

}
