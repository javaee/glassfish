/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.s1asdev.ejb.ejb30.interceptors.session;

import java.lang.reflect.Method;

import javax.interceptor.InvocationContext;
import javax.interceptor.AroundInvoke;

public class ArgumentsVerifier {

    @AroundInvoke
    public Object interceptCall(InvocationContext ctx)
   	throws MyBadException, Exception 
    {
        Method method = ctx.getMethod();
        String methodName = method.getName();
        Object[] params = ctx.getParameters();
        if (methodName.equals("addIntInt")) {
            System.out.println("**++**Args[0]: " + params[0].getClass().getName());
            System.out.println("**++**Args[1]: " + params[1].getClass().getName());
            params[0] = new Integer(10);
            params[1] = new Integer(20);
        } else if (methodName.equals("setInt")) {
            params[0] = new Long(10);
        } else if (methodName.equals("setLong")) {
            params[0] = new Integer(10);
        } else if (methodName.equals("addLongLong")) {
            params[0] = new Integer(10);
            params[0] = new Long(10);
        } else if (methodName.equals("setFoo")) {
            params[0] = new SubFoo();
        } else if (methodName.equals("setBar")) {
            params[0] = new SubFoo();   //Should fail
        } else if (methodName.equals("emptyArgs")) {
            params = new Object[1];
            params[0] = new Long(45);
        } else if (methodName.equals("objectArgs")) {
            params = new Object[0];
        } 

        try {
            ctx.setParameters(params);
        } catch (IllegalArgumentException illArgEx) {
            throw new MyBadException("Invalid type as argument", illArgEx);
        }
        Object retVal = ctx.proceed();

        if (methodName.equals("setIntInt")) {
            Integer ii = (Integer) retVal;
            if (! ii.equals(new Integer(30))) {
                throw new WrongResultException("Wrong result. expected 20. Got: " + ii);
            }
        }

        return retVal;
    }

}
