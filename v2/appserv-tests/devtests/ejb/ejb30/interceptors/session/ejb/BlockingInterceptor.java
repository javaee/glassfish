/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.s1asdev.ejb.ejb30.interceptors.session;

import javax.interceptor.InvocationContext;
import javax.interceptor.AroundInvoke;
import javax.annotation.PostConstruct;

public class BlockingInterceptor {
    //implements java.io.Serializable {

    private static int blockCount = 0;

    @PostConstruct
    private void afterCreation(InvocationContext ctx) {

        String value = (String) ctx.getContextData().get("PostConstruct");
        System.out.println("In BlockingInterceptor.afterCreation value = " 
                           + value);
        if(value == null) {
            throw new IllegalStateException("BaseInterceptor.PostConstruct " +
                                            " should have executed first");
        }
        try {
            ctx.proceed();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    @AroundInvoke
    private Object interceptCall(InvocationContext ctx)
   	throws CallBlockedException 
    {
	System.out.println("[test.BlockingInterceptor @AroundInvoke]: " + ctx.getMethod());
	blockCount++;
	throw new CallBlockedException("Call blocked");
    }

    
}
