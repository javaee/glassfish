package com.acme;

import javax.interceptor.InvocationContext;
import javax.interceptor.AroundInvoke;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

public class BaseInterceptor {

    boolean ai = false;
    boolean pc = false;
    boolean pd = false;

    @PostConstruct
    private void afterCreation(InvocationContext ctx) {
        System.out.println("In BaseInterceptor.PostConstruct");
        pc = true;

        try {
            ctx.proceed();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PreDestroy
    private void preDestroy(InvocationContext ctx) {
        System.out.println("In BaseInterceptor.PreDestroy");
        pd = true;

        try {
            ctx.proceed();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @AroundInvoke
    public Object interceptCall(InvocationContext ctx) throws Exception {
        System.out.println("In BaseInterceptor.AroundInvoke");
        ai = true;
        return ctx.proceed();
    }

}
