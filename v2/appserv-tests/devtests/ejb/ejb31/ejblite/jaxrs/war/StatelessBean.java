package com.acme;

import javax.ejb.Stateless;
import javax.ejb.*;
import javax.interceptor.Interceptors;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;

@Stateless
public class StatelessBean {

    public boolean interceptorWasHere;

    @PostConstruct
    private void init() {
	System.out.println("In StatelessBean:init()");
    }

    public void hello() {
	System.out.println("In StatelessBean::hello()");
    }


    public void assertInterceptorBinding() {
	if( !interceptorWasHere ) {
	    throw new EJBException("interceptor was not here");
	}
    }

    @PreDestroy
    private void destroy() {
	System.out.println("In StatelessBean:destroy()");
    }


}
