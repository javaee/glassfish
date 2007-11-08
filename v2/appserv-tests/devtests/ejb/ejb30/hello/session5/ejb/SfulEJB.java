/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.s1asdev.ejb.ejb30.hello.session5;

import javax.ejb.Stateful;
import javax.ejb.Remote;
import javax.interceptor.Interceptors;
import javax.ejb.EJBException;

@Stateful
@Remote({Sful.class, Sful2.class})
@Interceptors(MyInterceptor.class)
public class SfulEJB implements Sful, Sful2
{

    @Interceptors(MyInterceptor2.class)
    public String hello() {
        System.out.println("In SfulEJB:hello()");

        if( !MyInterceptor.getPostConstructCalled() ) {
            throw new EJBException("MyInterceptor.postConstruct should have " +
                                   "been called");
        }

        if( MyInterceptor2.getPostConstructCalled() ) {
            throw new EJBException("MyInterceptor2.postConstruct should not " +
                                   "have been called.  Callback methods on " +
                                   "interceptor classes do not apply to " +
                                   "method-level interceptors");
        }

        return "hello";
    }

    public String hello2() {
        System.out.println("In SfulEJB:hello2()");
        return "hello2";
    }
    

}
