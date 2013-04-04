package com.acme;

import javax.interceptor.InvocationContext;

public class InterceptorB {

    private void create(InvocationContext ctx) {
        System.out.println("In InterceptorB.AroundConstruct");

        try {
            ctx.proceed();
            BaseBean b = (BaseBean)ctx.getTarget();
            System.out.println("Created instance: " + b);
            b.ac1 = true;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void afterCreation(InvocationContext ctx) {
        System.out.println("In InterceptorB.PostConstruct");
        try {
            ctx.proceed();
            BaseBean b = (BaseBean)ctx.getTarget();
            System.out.println("PostConstruct on : " + b);
            if (b.pc1) throw new Exception("PostConstruct already called for " + b);
            b.pc1 = true;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void preDestroy(InvocationContext ctx) {
        System.out.println("In InterceptorB.PreDestroy");
        try {
            System.out.println("PreDestroy on : " + ctx.getTarget());
            ctx.proceed();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Object interceptCall(InvocationContext ctx) throws Exception {
        System.out.println("In InterceptorB.AroundInvoke");
        return ctx.proceed();
    }

}
