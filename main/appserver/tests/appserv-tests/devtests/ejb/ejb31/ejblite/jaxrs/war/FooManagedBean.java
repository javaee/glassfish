package com.acme;

import javax.interceptor.Interceptors;

import javax.annotation.PostConstruct;
import javax.annotation.ManagedBean;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;

@ManagedBean("FooManagedBean")
@Interceptors(InterceptorA.class)
public class FooManagedBean {

    public boolean interceptorWasHere;

    @PostConstruct
    private void init() {
	System.out.println("In FooManagedBean:init()");
    }

    public void hello() {
	System.out.println("In FooManagedBean::hello()");
    }


    public void assertInterceptorBinding() {
	if( !interceptorWasHere ) {
	    throw new RuntimeException("interceptor was not here");
	}
    }

    @PreDestroy
    private void destroy() {
	System.out.println("In FooManagedBean:destroy()");
    }


}
