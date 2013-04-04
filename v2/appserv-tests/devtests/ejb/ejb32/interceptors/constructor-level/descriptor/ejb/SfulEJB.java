package com.acme;

import javax.interceptor.InvocationContext;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptors;


import javax.ejb.*;
import javax.annotation.*;


@Stateful
public class SfulEJB extends BaseBean {

    @EJB private SlessEJB sless;

    public SfulEJB() {}

    public String hello() {
        System.out.println("In SfulEJB:hello()");
        verify("SfulEJB");
	return sless.sayHello();
    }

    @Remove
    public void remove() {
        System.out.println("In SfulEJB:remove()");
    }

    @PostConstruct
    private void init0() {
	System.out.println("**SfulEJB PostConstruct");
        verifyMethod(null);
    }

}
