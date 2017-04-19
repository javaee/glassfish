package com.acme;

import javax.ejb.*;
import javax.annotation.*;
import java.util.concurrent.Future;

@Singleton
public class SingletonNoIntf {

    @PostConstruct
    public void init() {
	System.out.println("In SingletonNoIntf::init()");
    }

    public String hello() {
	System.out.println("In SingletonNoIntf::hello()");
	return "hello, world\n";
    }

    @PreDestroy
    public void destroy() {
	System.out.println("In SingletonNoIntf::destroy()");
    }


}