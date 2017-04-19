package com.acme;

import javax.interceptor.InvocationContext;
import javax.interceptor.AroundInvoke;
import javax.interceptor.AroundConstruct;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

public class InterceptorA {

    @AroundConstruct
    private void create(InvocationContext ctx) {
        System.out.println("In InterceptorA.AroundConstruct");

        try {
            java.lang.reflect.Constructor<?> c = ctx.getConstructor();
            System.out.println("Using Constructor: " + c);
            ctx.proceed();
            BaseBean b = (BaseBean)ctx.getTarget();
            System.out.println("Created instance: " + b);
            b.ac = true;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostConstruct
    private void afterCreation(InvocationContext ctx) {
        System.out.println("In InterceptorA.PostConstruct");

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

    @PreDestroy
    private void preDestroy(InvocationContext ctx) {
        System.out.println("In InterceptorA.PreDestroy");
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

    @AroundInvoke
    public Object interceptCall(InvocationContext ctx) throws Exception {
        System.out.println("In InterceptorA.AroundInvoke");
        BaseBean b = (BaseBean)ctx.getTarget();
        System.out.println("AroundInvoke on : " + b);
        return ctx.proceed();
    }

}
