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
            System.out.println("PostConstruct on : " + b);
            if (b.pc) throw new Exception("PostConstruct already called for " + b);
            ctx.proceed();
            b.pc = true;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PreDestroy
    private void preDestroy(InvocationContext ctx) {
        System.out.println("In InterceptorA.PreDestroy");
        try {
            ctx.proceed();
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
