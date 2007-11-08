/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.s1asdev.ejb.ejb30.hello.session;

import javax.ejb.Stateless;
import javax.ejb.RemoteInterface;
@Stateless
@RemoteInterface({Sless.class})
public class SlessEJB implements Sless
{
    public String hello() {
        System.out.println("In SlessEJB:hello()");
        return "hello";
    }

}
