package com.acme;

import javax.ejb.*;
import javax.annotation.*;

import javax.inject.Inject;

@Stateful
@StatefulTimeout(0)
public class StatefulBean {

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
