/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.s1asdev.ejb.util.methodmap.client;

import java.util.Map;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import com.sun.ejb.containers.util.MethodMap;
import java.lang.reflect.Method;

public class MethodPerf {

    public static String toMicroSeconds(long timeInMillis, int numIterations) {

        return ((timeInMillis * 1000.0) / numIterations) + " micro-seconds";
    }
    
    public static void main(String args[]) {


        String className = args[0];
        System.out.println("Classname = " + className);
        int numIterations = Integer.parseInt(args[1]);
        System.out.println("num iterations = " + numIterations);

        try {
            Class clz = Class.forName(className);
            
            Map methodMap = new HashMap();

            Method[] methods = clz.getMethods();

            for(int i = 0; i < methods.length; i++) {
                methodMap.put(methods[i], methods[i].toString());
            }

            
            System.out.println("--------------------");
            System.out.println("With regular hashmap");
            System.out.println("--------------------");
            int totalTime = 0;
            for(int i = 0; i < methods.length; i++) {
                Method m = methods[i];
                long time = testMethodLookup(methodMap, m, numIterations);
                totalTime += time;
                System.out.println(toMicroSeconds(time, numIterations) + " : " + m.getName());
            }
            System.out.println("Avg time = " + 
                               toMicroSeconds(totalTime / methods.length,
                                              numIterations));
            
            
            System.out.println("--------------------");
            System.out.println("With optimized method lookup");
            System.out.println("--------------------");

            methodMap = new MethodMap(methodMap);
            totalTime = 0;
            for(int i = 0; i < methods.length; i++) {
                Method m = methods[i];
                long time = testMethodLookup(methodMap, m, numIterations);
                totalTime += time;
                System.out.println(toMicroSeconds(time, numIterations) + " : " + m.getName());
            }
            System.out.println("Avg time = " + 
                               toMicroSeconds(totalTime / methods.length,
                                              numIterations));
                                              
            
            System.out.println("--------------------");
            System.out.println("With extra optimized method lookup");
            System.out.println("--------------------");

            methodMap = new MethodMap(methodMap, methods.length * 20);
            totalTime = 0;
            for(int i = 0; i < methods.length; i++) {
                Method m = methods[i];
                long time = testMethodLookup(((MethodMap)methodMap), m, numIterations);
                totalTime += time;
                System.out.println(toMicroSeconds(time, numIterations) + " : " + m.getName());
            }
            System.out.println("Avg time = " + 
                               toMicroSeconds(totalTime / methods.length,
                                              numIterations));
                                  
            /*
            System.out.println("--------------------");
            System.out.println("With java.util.IdentityHashMap lookup");
            System.out.println("--------------------");

            methodMap = new java.util.IdentityHashMap(methodMap);

            for(int i = 0; i < methods.length; i++) {
                Method m = methods[i];
                long time = testMethodLookup(methodMap, m, numIterations);
                System.out.println(toMicroSeconds(time, numIterations) + " : " + m.getName());
            }

            */

            
            long ihcTime = testSystemIdentityHashCode(numIterations);            
            System.out.println("system identity hash code= " + toMicroSeconds(ihcTime, numIterations));
           

            MethodPerf ml = new MethodPerf();

            long sTime = testStaticStronglyTypedInvocation(numIterations);
            Method fooMethod = MethodPerf.class.getMethod("foo", null);
            System.out.println("static strong typed invocation time = " + toMicroSeconds(sTime, numIterations));
            long rTime = testStaticReflectiveInvocation(numIterations, fooMethod);
            System.out.println("static reflective invocation time = " + toMicroSeconds(rTime, numIterations));

            long i_sTime = testStaticStronglyTypedInvocation(numIterations);
            Method barMethod = MethodPerf.class.getMethod("bar", null);
            System.out.println("instance strong typed invocation time = " + toMicroSeconds(i_sTime, numIterations));
            long i_rTime = testStaticReflectiveInvocation(numIterations, fooMethod);
            System.out.println("instance reflective invocation time = " + toMicroSeconds(i_rTime, numIterations));
            
        } catch(Exception e) {
            e.printStackTrace();
        }

    }



    private static long testMethodLookup(MethodMap methodMap, Method m, int numIterations)
    {
        int numParams = m.getParameterTypes().length;

        long begin = System.currentTimeMillis();
        for(int i = 0; i < numIterations; i++) {
            methodMap.get(m, numParams);
        }
        long end = System.currentTimeMillis();


        // measure the time for the loop processing itself 
        long beginLoop = System.currentTimeMillis();
        for(int i = 0; i < numIterations; i++) {
        }        
        long endLoop = System.currentTimeMillis();
        long loopTime = endLoop - beginLoop;
        System.out.println("Loop time = " + loopTime);
        return (end - begin) - loopTime;
    }

    private static long testMethodLookup(Map methodMap, Method m, int numIterations)
    {
        long begin = System.currentTimeMillis();
        for(int i = 0; i < numIterations; i++) {
            methodMap.get(m);
        }
        long end = System.currentTimeMillis();

        // measure the time for the loop processing itself 
        long beginLoop = System.currentTimeMillis();
        for(int i = 0; i < numIterations; i++) {
        }        
        long endLoop = System.currentTimeMillis();
        long loopTime = endLoop - beginLoop;
        System.out.println("Loop time = " + loopTime);
        return (end - begin) - loopTime;
    }

 private static long testSystemIdentityHashCode(int numIterations) {
     Object o = new Object();
        long begin = System.currentTimeMillis();
        for(int i = 0; i < numIterations; i++) {
            System.identityHashCode(o);
        }
        long end = System.currentTimeMillis();

        // measure the time for the loop processing itself 
        long beginLoop = System.currentTimeMillis();
        for(int i = 0; i < numIterations; i++) {
        }        
        long endLoop = System.currentTimeMillis();
        long loopTime = endLoop - beginLoop;
        System.out.println("Loop time = " + loopTime);
        return (end - begin) - loopTime;
    }


    private static long testStaticStronglyTypedInvocation(int numIterations) {
        long begin = System.currentTimeMillis();
        for(int i = 0; i < numIterations; i++) {
            MethodPerf.foo();
        }
        long end = System.currentTimeMillis();

        // measure the time for the loop processing itself 
        long beginLoop = System.currentTimeMillis();
        for(int i = 0; i < numIterations; i++) {
        }        
        long endLoop = System.currentTimeMillis();
        long loopTime = endLoop - beginLoop;
        System.out.println("Loop time = " + loopTime);
        return (end - begin) - loopTime;
    }

    private static long testInstanceStronglyTypedInvocation(int numIterations, MethodPerf ml) {
        long begin = System.currentTimeMillis();
        for(int i = 0; i < numIterations; i++) {
            ml.bar();
        }
        long end = System.currentTimeMillis();

        // measure the time for the loop processing itself 
        long beginLoop = System.currentTimeMillis();
        for(int i = 0; i < numIterations; i++) {
        }        
        long endLoop = System.currentTimeMillis();
        long loopTime = endLoop - beginLoop;
        System.out.println("Loop time = " + loopTime);
        return (end - begin) - loopTime;
    }

    private static long testStaticReflectiveInvocation(int numIterations, Method m) 
        throws Exception {
                                                 
        long begin = System.currentTimeMillis();        
        for(int i = 0; i < numIterations; i++) {
            m.invoke(null, null);
        }
        long end = System.currentTimeMillis();

        // measure the time for the loop processing itself 
        long beginLoop = System.currentTimeMillis();
        for(int i = 0; i < numIterations; i++) {
        }        
        long endLoop = System.currentTimeMillis();
        long loopTime = endLoop - beginLoop;
        System.out.println("Loop time = " + loopTime);
        return (end - begin) - loopTime;
    }

    private static long testInstanceReflectiveInvocation(int numIterations, Object o, Method m) 
        throws Exception {
                                                 
        long begin = System.currentTimeMillis();        
        for(int i = 0; i < numIterations; i++) {
            m.invoke(o, null);
        }
        long end = System.currentTimeMillis();

        // measure the time for the loop processing itself 
        long beginLoop = System.currentTimeMillis();
        for(int i = 0; i < numIterations; i++) {
        }        
        long endLoop = System.currentTimeMillis();
        long loopTime = endLoop - beginLoop;
        System.out.println("Loop time = " + loopTime);
        return (end - begin) - loopTime;
    }


    public static void foo() {}

    public MethodPerf() {}

    public static void bar() {}

}
