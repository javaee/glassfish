/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.s1asdev.ejb.ejb30.hello.session4;

import javax.ejb.Stateful;
import javax.ejb.SessionContext;
import javax.annotation.Resource;

@Stateful(mappedName="ejb_ejb30_hello_session4_Sful")
public class SfulEJB implements Sful, Sful2
{

    private String id_;

    @Resource private SessionContext ctx;

    public void setId(String id) {
        id_ = id;
    }

    public String getId() {
        return id_;
    }

    public Sful2 getSful2() {
        return (Sful2) ctx.getBusinessObject(Sful2.class);
    }

    public String hello() {
        System.out.println("In SfulEJB:hello()");
        return "hello";
    }

    public String hello2() {
        System.out.println("In SfulEJB:hello2()");
        return "hello2";
    }

    public void sameMethod() {
        System.out.println("In SfulEJB:sameMethod()");
    }
    

}
