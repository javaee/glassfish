/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.s1asdev.ejb.ejb30.hello.session;

import javax.ejb.Stateful;

@Stateful
public class SfulEJB implements Sful
{

    public String hello() {
        System.out.println("In SfulEJB:hello()");
        return "hello";
    }

}
