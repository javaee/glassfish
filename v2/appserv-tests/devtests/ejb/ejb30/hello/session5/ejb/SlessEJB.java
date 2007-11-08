/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.s1asdev.ejb.ejb30.hello.session5;

import javax.ejb.Stateless;
import javax.ejb.Remote;

@Stateless(mappedName="ejb_ejb30_hello_session5_Sless")
@Remote({Sless.class, Sless2.class})
public class SlessEJB implements Sless, Sless2
{
    public String hello() {
        System.out.println("In SlessEJB:hello()");
        return "hello";
    }

    public String hello2() {
        System.out.println("In SlessEJB:hello2()");
        return "hello2";
    }

    public String foo(int a) {
        System.out.println("In SlessEJB:foo()");
        return "foo_" + a;
    }

}
