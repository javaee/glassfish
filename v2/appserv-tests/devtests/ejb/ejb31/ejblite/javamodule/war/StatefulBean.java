package com.acme;

import javax.ejb.Stateful;
import javax.ejb.*;
import javax.interceptor.Interceptors;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;

@Stateful
public class StatefulBean {

    @PostConstruct
    private void init() {
	System.out.println("In StatefulBean:init()");
    }

    @PrePassivate
    private void prePass() {
	System.out.println("In StatefulBean:prePassivate()");
    }

    public void hello() {
	System.out.println("In StatefulBean::hello()");
    }

    @PreDestroy
    private void destroy() {
	System.out.println("In StatefulBean:destroy()");
    }


}
