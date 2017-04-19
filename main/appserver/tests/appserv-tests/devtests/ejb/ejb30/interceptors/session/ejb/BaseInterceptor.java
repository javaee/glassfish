/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.s1asdev.ejb.ejb30.interceptors.session;

import javax.interceptor.InvocationContext;
import javax.interceptor.AroundInvoke;
import javax.annotation.PostConstruct;
import javax.ejb.PrePassivate;
import javax.ejb.PostActivate;

public class BaseInterceptor {

    private static int baseCount = 0;
    private static int prePassivateCount = 0;
    private static int postActivateCount = 0;

    private int interceptorID;
    private int computedInterceptorID;

    @PostConstruct
    private void afterCreation(InvocationContext ctx) {
        System.out.println("In BaseInterceptor.afterCreation");
        ctx.getContextData().put("PostConstruct", "BaseInterceptor");
        try {
            ctx.proceed();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    @AroundInvoke
    public Object interceptCall(InvocationContext ctx)
   	throws Exception 
    {
        Object result = null;
	boolean setId = false;
	baseCount++;
    if (ctx.getMethod().getName().equals("setInterceptorId")) {
        java.util.Map map = ctx.getContextData();
        map.put("BaseInterceptor", this);
        setId = true;
    }
    try {
        result = ctx.proceed();
    } finally {
        if (setId == true) {
            computedInterceptorID = interceptorID * 2 + 1;
        }
    }
    return result;
    }

    public static int getInvocationCount() {
	    return baseCount;
    }
    
    @PrePassivate
    public void prePassivate(InvocationContext ctx) {
	prePassivateCount++;
	System.out.println("prePassivateCount: " + prePassivateCount);
    }

    @PostActivate
    public void postActivate(InvocationContext ctx) {
	postActivateCount++;
	System.out.println("postActivateCount: " + postActivateCount);
    }
    
    //Some package private methods to check state
    void setInterceptorID(int val) {
        this.interceptorID = val;
    }
    
    int getInterceptorID() {
        boolean valid = (computedInterceptorID == interceptorID * 2 + 1);
        if (! valid) {
            throw new IllegalStateException("" + interceptorID + " != " + computedInterceptorID);
        }
        return this.interceptorID;
    }

}
