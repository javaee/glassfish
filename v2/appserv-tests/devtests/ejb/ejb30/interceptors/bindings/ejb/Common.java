/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.s1asdev.ejb.ejb30.interceptors.bindings;

import java.util.List;
import java.util.LinkedList;

import javax.interceptor.InvocationContext;
import javax.ejb.EJBException;

public class Common {

    static final String INTERCEPTORS_PROP = "interceptors";
    static final String NOTHING_METHOD = "nothing";

    static void aroundInvokeCalled(InvocationContext ctx, String id) {

        List<String> interceptors = (List<String>) 
            ctx.getContextData().get(INTERCEPTORS_PROP);
        
        if( interceptors == null ) {
            interceptors = new LinkedList<String>();
            ctx.getContextData().put(INTERCEPTORS_PROP, interceptors);
        }

        interceptors.add(id);
        
    }

    static void checkResults(InvocationContext ctx) {

        String methodName = ctx.getMethod().getName();

        List<String> expected = null;

        if( !methodName.equals("nothing") ) {

            expected = new LinkedList<String>();

            String methodNameUpper = methodName.toUpperCase();

            for( char nextChar : methodNameUpper.toCharArray() ) {
                expected.add(nextChar + "");
            }
        } 

        List<String> actual = (List<String>) 
            ctx.getContextData().get(INTERCEPTORS_PROP);

        String msg = "Expected " + expected + " for method " +
            ctx.getMethod() + " actual = " + actual;

        if( (expected == null) && (actual == null) ) {
            System.out.println("Successful interceptor chain : " + msg);
        } else if( (expected == null) || (actual == null) ) {
            throw new EJBException(msg);
        } else if( !expected.equals(actual) ) {
            throw new EJBException(msg);
        } else {
            System.out.println("Successful interceptor chain : " + msg);
        }
    }
   
}
