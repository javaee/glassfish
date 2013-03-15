package com.acme;

import javax.interceptor.InvocationContext;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptors;


import javax.ejb.*;
import javax.annotation.*;


@Stateful
@Interceptors(InterceptorA.class)
public class SfulEJB extends BaseBean implements Sful {

    @EJB private Sless sless;

    public String hello() {
        System.out.println("In SfulEJB:hello()");
	return sless.sayHello();
    }

    @Remove
    public void remove() {
        System.out.println("In SfulEJB:remove()");
    }

    @AroundInvoke
    private Object interceptCall0(InvocationContext ctx) throws Exception {
	System.out.println("**SfulEJB AROUND-INVOKE++ [@AroundInvoke]: " + ctx.getMethod());
        if (!ai) throw new RuntimeException("BaseBean was not called");
        ai = false; //reset
        return ctx.proceed();
    }

    @PostConstruct
    private void init0() {
	System.out.println("**SfulEJB PostConstruct");
        if (!pc) throw new RuntimeException("BaseBean was not called");
        pc = false; //reset
    }

    @PreDestroy
    private void destroy0() {
	System.out.println("**SfulEJB PreDestroy");
    }

}
