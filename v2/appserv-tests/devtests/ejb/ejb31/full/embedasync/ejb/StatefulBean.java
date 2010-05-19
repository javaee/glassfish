package com.acme;

import javax.ejb.*;
import javax.annotation.*;

import javax.inject.Inject;

@Stateful
@StatefulTimeout(0)
public class StatefulBean {

    @Inject
	public StatefulBean(SingletonBean sing) {
	System.out.println("In StatefulBean ctor sing = " + sing);
    }

    @PostConstruct
    public void init() {
        System.out.println("In StatefulBean::init()");
    }

    public void hello() {
	System.out.println("In StatefulBean::hello()");
    }

    @PreDestroy
    public void destroy() {
        System.out.println("In StatefulBean::destroy()");
    }



}
