package com.acme;

import javax.ejb.*;
import javax.annotation.*;

@Singleton
@Remote(Hello.class)
public class SingletonBean {

    @PostConstruct
    public void init() {
        System.out.println("In SingletonBean::init()");

    }
    
    public String hello() {
	System.out.println("In SingletonBean::hello()");
	return "hello, world!\n";
    }

    @PreDestroy
    public void destroy() {
        System.out.println("In SingletonBean::destroy()");
    }



}
