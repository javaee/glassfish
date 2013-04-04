package com.acme;

import javax.interceptor.InvocationContext;

public class InterceptorC {

    private void create(InvocationContext ctx) {
        System.out.println("In InterceptorC.AroundConstruct");

        try {
            java.lang.reflect.Constructor<?> c = ctx.getConstructor();
            System.out.println("Using Constructor: " + c);
            ctx.proceed();
            BaseBean b = (BaseBean)ctx.getTarget();
            System.out.println("Created instance: " + b);
            b.ac2 = true;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void afterCreation(InvocationContext ctx) {
        System.out.println("In InterceptorC.PostConstruct");

        try {
            BaseBean b = (BaseBean)ctx.getTarget();
            b.method = ctx.getMethod();
            System.out.println("PostConstruct on : " + b);
            ctx.proceed();
            b.method = null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void preDestroy(InvocationContext ctx) {
        System.out.println("In InterceptorC.PreDestroy");
        try {
            BaseBean b = (BaseBean)ctx.getTarget();
            System.out.println("PreDestroy on : " + b);
            b.method = ctx.getMethod();
            ctx.proceed();
            b.method = null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Object interceptCall(InvocationContext ctx) throws Exception {
        System.out.println("In InterceptorC.AroundInvoke");
        BaseBean b = (BaseBean)ctx.getTarget();
        System.out.println("AroundInvoke on : " + b);
        return ctx.proceed();
    }

}
