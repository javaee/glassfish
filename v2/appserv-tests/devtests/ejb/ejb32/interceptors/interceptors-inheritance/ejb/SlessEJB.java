package com.acme;

import javax.ejb.Stateless;
import javax.interceptor.*;
import javax.annotation.*;

@Stateless
@Interceptors(InterceptorA.class)
public class SlessEJB extends BaseBean implements Sless {
    public String sayHello() {
	    return "Hello";
    }

    @AroundInvoke
    private Object interceptCall1(InvocationContext ctx) throws Exception {
        System.out.println("**SlessEJB AROUND-INVOKE++ [@AroundInvoke]: " + ctx.getMethod());
        if (!ai) throw new RuntimeException("BaseBean was not called");
        ai = false; //reset
        return ctx.proceed();
    }

    @PostConstruct
    private void init1() {
        System.out.println("**SlessEJB PostConstruct");
        if (!pc) throw new RuntimeException("BaseBean was not called");
        pc = false; //reset
    }

    @PreDestroy
    private void destroy1() {
        System.out.println("**SlessEJB PreDestroy");
    }

}
