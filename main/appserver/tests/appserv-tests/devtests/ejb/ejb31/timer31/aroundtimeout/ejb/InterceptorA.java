/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.s1asdev.ejb.ejb31.aroundtimeout;

import javax.interceptor.AroundTimeout;
import javax.interceptor.InvocationContext;
import javax.annotation.PostConstruct;
import javax.naming.InitialContext;
import javax.ejb.SessionContext;
import javax.annotation.Resource;

public class InterceptorA {

    @Resource SessionContext sessionCtx;
    
    @PostConstruct
    private void postConstruct(InvocationContext ctx) {
        try {
            ctx.proceed();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    @AroundTimeout
    Object aroundTimeout(InvocationContext ctx)
	        throws Exception {

/**
        // access injected environment dependency
        System.out.println("invoked business interface = " +
                           sessionCtx.getInvokedBusinessInterface());
**/

        // look up ejb-ref defined within default interceptor in ejb-jar.xml
        InitialContext ic = new InitialContext();
        Sless3 sless3 = (Sless3) ic.lookup("java:comp/env/ejb/Sless3");
        
        
        Common.aroundTimeoutCalled(ctx, "A");
        return ctx.proceed();
    }
   
}
