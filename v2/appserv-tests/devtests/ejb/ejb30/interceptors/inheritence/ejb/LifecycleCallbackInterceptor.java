/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.s1asdev.ejb.ejb30.interceptors.session;

import javax.interceptor.InvocationContext;
import javax.interceptor.AroundInvoke;
import javax.ejb.PrePassivate;
import javax.ejb.PostActivate;
import javax.ejb.EJB;
import javax.annotation.Resource;
import javax.ejb.SessionContext;

public class LifecycleCallbackInterceptor {

    private static int prePassivateCallbackCount = 0;
    private static int postActivateCallbackCount = 0;
    
    private int interceptorID;
    private int computedInterceptorID;

    @EJB Sless sless;
    @Resource SessionContext sessionCtx;

    @PrePassivate
    void prePassivate(InvocationContext ctx) {
	prePassivateCallbackCount++;
    }

    @PostActivate
    private void postActivate(InvocationContext ctx) {
	postActivateCallbackCount++;
    }

    @AroundInvoke
    Object aroundInvoke(InvocationContext ctx)
	        throws Exception {
        if (ctx.getMethod().getName().equals("setID")) {
            ctx.getContextData().put("LifecycleCallbackInterceptor", this);
            System.out.println("calling sless from interceptor. sless says " +
                               sless.sayHello());
            System.out.println("invoked business interface = " +
                               sessionCtx.getInvokedBusinessInterface());
        }
        return ctx.proceed();
    }

    public static void resetLifecycleCallbackCounters() {
	prePassivateCallbackCount = postActivateCallbackCount = 0;
    }

    public static int getPrePassivateCallbackCount() {
	return prePassivateCallbackCount;
    }

    public static int getPostActivateCallbackCount() {
	return postActivateCallbackCount;
    }
    

    void setInterceptorID(int val) {
        this.interceptorID = val;
        this.computedInterceptorID = 2 * val + 1;
    }
    
    int getInterceptorID() {
        return interceptorID;
    }
    
    boolean checkInterceptorID(int val) {
        return (val == interceptorID) && 
            (computedInterceptorID == 2 * val + 1);
    }
   
}
