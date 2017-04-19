package com.acme;

import javax.ejb.*;
import javax.annotation.*;
import javax.interceptor.*;


public class BaseBean {

    boolean ai = false;
    boolean pc = false;
    boolean pd = false;

    @AroundInvoke
    private Object interceptCall(InvocationContext ctx) throws Exception {
	System.out.println("**BaseBean AROUND-INVOKE++ [@AroundInvoke]: " + ctx.getMethod());
        ai = true;
        return ctx.proceed();
    }

    @PostConstruct
    private void init() {
	System.out.println("**BaseBean PostConstruct");
        pc = true;
    }

    @PreDestroy
    private void destroy() {
	System.out.println("**BaseBean PreDestroy");
        pd = true;
    }

}
