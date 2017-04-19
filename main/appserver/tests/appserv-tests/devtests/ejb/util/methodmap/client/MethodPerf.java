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
