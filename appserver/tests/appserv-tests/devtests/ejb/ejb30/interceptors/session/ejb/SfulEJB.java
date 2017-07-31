/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2002-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
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
