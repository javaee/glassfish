/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.s1asdev.ejb.ejb31.aroundtimeout;

import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Set;
import java.util.HashSet;
import java.util.Collections;

import javax.interceptor.InvocationContext;
import javax.ejb.EJBException;
import javax.ejb.Timer;

public class Common {

    static final String INTERCEPTORS_PROP = "aroundtimeout";
    static final String NOTHING_METHOD = "nothing";

    static final Set<String> calls = Collections.synchronizedSet(new HashSet<String>());

    static void aroundTimeoutCalled(InvocationContext ctx, String id) {

        List<String> aroundtimeout = (List<String>) 
            ctx.getContextData().get(INTERCEPTORS_PROP);
        
        if( aroundtimeout == null ) {
            aroundtimeout = new LinkedList<String>();
            ctx.getContextData().put(INTERCEPTORS_PROP, aroundtimeout);
        }

        aroundtimeout.add(id);
        
    }

    static void checkResults(InvocationContext ctx) {

        String methodName = ctx.getMethod().getName();

        if (methodName.equals("noaroundtimeout")) {
            throw new EJBException("AroundTimeout is called for method " + methodName);
        }

        String info = "" ;
        if( !(ctx.getTarget() instanceof SlessEJB7) ) {
            Timer t = (Timer) ctx.getTimer();
            if (t == null) {
                throw new EJBException("Timer is null for " + methodName);
            }
            info = "" + t.getInfo();

            String method_part = info.substring(info.indexOf('-') + 1);

            if (!methodName.equals(method_part)) {
                throw new EJBException("methodName: " + methodName + " vs. " + info);
            }
        }

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
        calls.add(info);
    }
   
    static void checkResults(String s0, int expected) {
        List<String> results = new ArrayList<String>();
        for (String s : calls) {
            if (s.startsWith(s0)) {
                results.add(s);
            }
        }
        if (results.size() != expected) {
             throw new RuntimeException("Expected for " + s0 + ": " + expected + " Got: " + results);
        }
        System.out.println("Verified " + expected + " aroundTimeout calls for " + s0);
    }

    static void storeResult(String s) {
        calls.add(s);
    }
}
