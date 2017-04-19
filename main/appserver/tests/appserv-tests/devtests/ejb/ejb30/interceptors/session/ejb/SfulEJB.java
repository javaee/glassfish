/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.s1asdev.ejb.ejb30.interceptors.session;

import java.util.Map;
import java.util.HashMap;

import javax.ejb.Stateful;
import javax.interceptor.InvocationContext;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptors;


import javax.ejb.*;
import javax.annotation.*;


@Stateful
@Interceptors({
        ErrorRecoveryInterceptor.class, LifecycleCallbackInterceptor.class
        })
public class SfulEJB implements Sful
{

    @Resource(name="sc") SessionContext sc;

    @EJB(name="foobar") Sless foobar;

    private int count = 0;
    
    private int id;
    private LifecycleCallbackInterceptor interceptor;

    @EJB private Sless sless;
    @EJB private Dummy dummy;

    private int assertionFailedCount = 0;

    public String hello() {
        System.out.println("In SfulEJB:hello()");
	sless.sayHello();
	System.out.println("caller principal = " + sc.getCallerPrincipal());
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

    public Map<String, Boolean> checkSetParams() {
        boolean status = true;
        Map map = new HashMap<String, Boolean>();
        try {
            sless.setFoo(new Foo());

            try {
                status = false;
                sless.setBar(new Bar());
            } catch (MyBadException ejbEx) {
                status = true;
            } catch (EJBException ejbEx) {
            }
            map.put("setBar", status);

            try {
                status = false;
                sless.emptyArgs();
            } catch (MyBadException illEx) {
                status = true;
            } catch (EJBException ejbEx) {
            }
            map.put("emptyArgs", status);

            try {
                status = false;
                sless.objectArgs(new Long(70));
            } catch (MyBadException illEx) {
                status = true;
            } catch (EJBException ejbEx) {
            }
            map.put("objectArgs", status);

            try {
                status = false;
                sless.addIntInt(1, 2);
                status = true;
            } catch (MyBadException myBadEx) {
                myBadEx.printStackTrace();
            } catch (WrongResultException wrEx) {
                wrEx.printStackTrace();
            } catch (EJBException ejbEx) {
            }
            map.put("addIntInt", status);

            try {
                status = false;
                sless.setInt(935);
            } catch (MyBadException illEx) {
                status = true;
            } catch (EJBException ejbEx) {
            }
            map.put("setInt", status);

            try {
                status = false;
                sless.setLong(2323l);
                status = true;
            } catch (MyBadException illEx) {
            } catch (EJBException ejbEx) {
            }
            map.put("setLong", status);

            try {
                status = false;
                sless.addLongLong(123l, 234l);
                status = true;
            } catch (MyBadException illEx) {
            } catch (EJBException ejbEx) {
            }
            map.put("setLongLong", status);

        } catch (Exception ex) {
                status = false;
        }

        return map;
    }

    public void assertIfTrue(boolean val)
        throws AssertionFailedException {

        if (val == false) {
            System.out.println("**ASSERTION_FAILED: " + val);
            assertionFailedCount++;
            throw new AssertionFailedException("Got: " + val);
        } else {
            System.out.println("**ASSERTION_PASSED: " + val);
        }
    }

    public int getAssertionFailedCount() {
        return assertionFailedCount;
    }

}
