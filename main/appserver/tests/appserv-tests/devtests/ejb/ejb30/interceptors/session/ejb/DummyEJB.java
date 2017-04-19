/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.s1asdev.ejb.ejb30.interceptors.session;

import javax.ejb.Stateless;
import javax.interceptor.Interceptors;
import javax.interceptor.InvocationContext;
import javax.interceptor.AroundInvoke;
import javax.ejb.EJBException;
import javax.annotation.PostConstruct;

@Interceptors({
	com.sun.s1asdev.ejb.ejb30.interceptors.session.BaseInterceptor.class,
	com.sun.s1asdev.ejb.ejb30.interceptors.session.BlockingInterceptor.class
})

@Stateless
public class DummyEJB
	implements Dummy
{
    int interceptorId;
    private boolean createCalled = false;
    
    @PostConstruct
    private void afterCreate()
    {
        System.out.println("In DummyEJB::afterCreate");
        createCalled = true;
    }

    public String dummy()
	throws CallBlockedException
    {
	    return "Dummy!!";
    }

    public void setInterceptorId(int val) {
        if( !createCalled ) {
            throw new EJBException("create was not called");
        }

        this.interceptorId = val;
    }
    
    @AroundInvoke
    private Object interceptCall(InvocationContext ctx)
        throws Exception { 
        return ctx.proceed();
    }
}

