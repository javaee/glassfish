/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.s1asdev.ejb.ejb30.hello.session4;

import javax.ejb.Stateless;
import javax.ejb.Remote;

// Use @Remote without parameters.  If there's only one ambiguous
// interface in implements clause, it will be treate as @Remote instead of
// @Local
@Remote  
@Stateless
public class SlessEJB implements Sless
{
    public String hello() {
        System.out.println("In SlessEJB:hello()");
        return "hello";
    }

}
