package com.acme;

import javax.interceptor.InvocationContext;
import javax.interceptor.AroundInvoke;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

public class InterceptorA extends BaseInterceptor {

    @PostConstruct
    private void afterCreation0(InvocationContext ctx) {
        System.out.println("In InterceptorA.PostConstruct");
        if (!pc) throw new RuntimeException("BaseInterceptor was not called");
        pc = false; //reset

        try {
            ctx.proceed();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PreDestroy
    private void preDestroy0(InvocationContext ctx) {
        System.out.println("In InterceptorA.PreDestroy");
        try {
            ctx.proceed();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @AroundInvoke
    public Object interceptCall0(InvocationContext ctx) throws Exception {
        System.out.println("In InterceptorA.AroundInvoke");
        if (!ai) throw new RuntimeException("BaseInterceptor was not called");
        ai = false; //reset
        return ctx.proceed();
    }

}
