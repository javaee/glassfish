/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.s1asdev.ejb.ejb30.hello.session5;

import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;
import javax.annotation.PostConstruct;

public class MyInterceptor
{
    private static boolean postConstructCalled = false;

    static boolean getPostConstructCalled() {
        return postConstructCalled;
    }

    @PostConstruct 
    private void postConstruct(InvocationContext ctx) {
        postConstructCalled = true;
        System.out.println("MyInterceptor::postConstruct called");
    }


}
