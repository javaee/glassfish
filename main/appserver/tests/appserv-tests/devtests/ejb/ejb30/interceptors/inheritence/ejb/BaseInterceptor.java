/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.s1asdev.ejb.ejb30.interceptors.session;

import java.util.List;
import java.util.ArrayList;

import javax.interceptor.InvocationContext;
import javax.interceptor.AroundInvoke;
import javax.annotation.PostConstruct;
import javax.ejb.PrePassivate;
import javax.ejb.PostActivate;

public class BaseInterceptor
	implements java.io.Serializable {

    protected static final String BASE_INTERCEPTOR_NAME = "BaseInterceptor";

    protected List<String> postConstructList = new ArrayList<String>();
    protected List<String> aroundInvokeList = new ArrayList<String>();

    protected int baseAICount = 0;
    protected int prePassivateCount = 0;
    private int postActivateCount = 0;
    protected int basePCCount = 0;


    @PostConstruct
    private void basePostConstruct(InvocationContext ctx)
    	throws RuntimeException {
	    postConstructList = new ArrayList<String>();
	    postConstructList.add(BASE_INTERCEPTOR_NAME);
        ctx.getContextData().put(getName(), this);
        basePCCount++;
        System.out.println("GGGG: @PostConstruct for: " + this.getClass().getName());
        try {
            ctx.proceed();
        } catch(RuntimeException e) {
            throw e;
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }


    @AroundInvoke
    public Object baseAroundInvoke(InvocationContext ctx)
   		throws Exception
    {
	    aroundInvokeList = new ArrayList<String>();
	    aroundInvokeList.add(BASE_INTERCEPTOR_NAME);
		ctx.getContextData().put(getName(), this);
		baseAICount++;
    	return ctx.proceed();
    }

    @PrePassivate
    public void prePassivate(InvocationContext ctx) {
	prePassivateCount++;
	}

    @PostActivate
    public void postActivate(InvocationContext ctx) {
	postActivateCount++;
	}


    String getName() {
       return BaseInterceptor.class.getName();
    }
}
