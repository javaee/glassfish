/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.s1asdev.ejb.ejb30.interceptors.session;

import javax.ejb.Stateful;
import javax.interceptor.InvocationContext;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptors;
import javax.ejb.EJB;
import javax.ejb.Remote;


@Stateful
@Interceptors({
	LifecycleCallbackInterceptor.class,
	BaseLifecycleInterceptor.class
	})
public class SfulEJB implements Sful
{

    private int count = 0;

    private int id;
    private LifecycleCallbackInterceptor interceptor;

    @EJB private Sless sless;
    @EJB private Dummy dummy;

    public String hello() {
        System.out.println("In SfulEJB:hello()");
        return "hello";
    }

    @AroundInvoke
    private Object interceptCall(InvocationContext ctx)
   	throws Exception
    {
	System.out.println("**Beans AROUNDINVOKE++ [@AroundInvoke]: " + ctx.getMethod());
	count++;
        try {
            if (ctx.getMethod().getName().equals("setID")) {
                java.util.Map map = ctx.getContextData();
                interceptor = (LifecycleCallbackInterceptor) map.get("LifecycleCallbackInterceptor");
            }
            return ctx.proceed();
        } catch(EatException ee) {
            return "ate exception -- yummy!!";
        }
    }

    public int getCount() {
	    return count;
    }

    public void throwAppException(String msg)
        throws AppException
    {
        throw new AppException(msg);
    }

    public String computeMid(int min, int max)
	    throws SwapArgumentsException
    {
	    return sless.sayHello()
		    	+ ", Midpoint of " + min + ", " + max + "; "
			+  sless.computeMidPoint(min, max);
    }

    public String callDummy()
	    throws Exception
    {
	    return dummy.dummy();
    }

    public String eatException()
        throws EatException
    {
        System.out.println("In SfulEJB::eatException()");
        throw new EatException("SfulEJB::eatException()");
    }

    public int getPrePassivateCallbackCount() {
	return LifecycleCallbackInterceptor.getPrePassivateCallbackCount();
    }

    public int getPostActivateCallbackCount() {
	return LifecycleCallbackInterceptor.getPostActivateCallbackCount();
    }

    public void resetLifecycleCallbackCounters() {
	LifecycleCallbackInterceptor.resetLifecycleCallbackCounters();
    }

    public void setID(int val) {
        this.id = val;
        interceptor.setInterceptorID(val);
    }

    public boolean isStateRestored() {
        return interceptor.checkInterceptorID(id);
    }

    public String isInterceptorCallCounOK()
    {
		try {
			return dummy.isInterceptorCallCounOK();
		} catch (Exception ex) {
			System.out.println("*********");
			ex.printStackTrace();
			System.out.println("*********");
		}

		return null;
	}

	    public String isPostConstructCallCounOK()
	    {
			try {
				return dummy.isPostConstructCallCounOK();
			} catch (Exception ex) {
				System.out.println("*********");
				ex.printStackTrace();
				System.out.println("*********");
			}

			return null;
	}


}
