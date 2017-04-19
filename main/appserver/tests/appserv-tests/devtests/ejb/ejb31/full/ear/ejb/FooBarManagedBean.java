package com.acme;

import javax.annotation.*;

@ManagedBean("foobarmanagedbean")
public class FooBarManagedBean {

    @PostConstruct
    private void init() {
	System.out.println("In FooBarManagedBean::init() " + this);
    }
    
    @PreDestroy
    private void destroy() {
	System.out.println("In FooBarManagedBean::destroy() ");
    }

}
