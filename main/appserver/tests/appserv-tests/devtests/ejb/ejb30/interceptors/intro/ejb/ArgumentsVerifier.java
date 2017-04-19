/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.s1asdev.ejb.ejb30.interceptors.intro;

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


        if (methodName.equals("concatAndReverse")) {
            String arg1 = (String) params[0];
            String arg2 = (String) params[1];

            if ("null".equalsIgnoreCase(arg1)) {
                params[0] = null;
            }
            if ("null".equalsIgnoreCase(arg2)) {
                params[1] = null;
            }
        } else if (methodName.equals("plus")) {
            params = new Object[] {new Byte((byte) 6), new Short((short) 6), new Integer(6)};
        } else {
            params = new Object[] {new Byte((byte) 88), new Short((short) 6)};
        }

        try {
            ctx.setParameters(params);
        } catch (IllegalArgumentException illArgEx) {
            throw new MyBadException("Invalid type as argument", illArgEx);
        }
        return ctx.proceed();
    }

}
