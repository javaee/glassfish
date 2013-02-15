package com.acme;

import javax.interceptor.InvocationContext;
import javax.interceptor.AroundInvoke;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

public class InterceptorA {

    private void create(InvocationContext ctx) {
        System.out.println("In InterceptorA.AroundConstruct");

        try {
            ctx.proceed();
            BaseBean b = (BaseBean)ctx.getTarget();
            System.out.println("Created instance: " + b);
            b.ac = true;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

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

    public Object interceptCall(InvocationContext ctx) throws Exception {
        System.out.println("In InterceptorA.AroundInvoke");
        BaseBean b = (BaseBean)ctx.getTarget();
        System.out.println("AroundInvoke on : " + b);
        return ctx.proceed();
    }

}
