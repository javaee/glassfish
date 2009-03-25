package com.acme;

import javax.ejb.Stateless;
import javax.ejb.*;
import javax.interceptor.Interceptors;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;

@Stateless
public class StatelessBean {

    @PostConstruct
    private void init() {
	System.out.println("In StatelessBean:init()");
    }

    public void hello() {
	System.out.println("In StatelessBean::hello()");
    }

    @PreDestroy
    private void destroy() {
	System.out.println("In StatelessBean:destroy()");
    }


}
