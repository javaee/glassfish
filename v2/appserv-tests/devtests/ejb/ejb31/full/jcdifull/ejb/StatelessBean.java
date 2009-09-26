package com.acme;

import javax.ejb.*;
import javax.annotation.*;

import javax.inject.Inject;
import javax.inject.Named;

@Stateless
@LocalBean
public class StatelessBean implements StatelessLocal {


    @PostConstruct
	public void init() {
	System.out.println("In StatelessBean::init()");
    }

    public void hello() {
	System.out.println("In StatelessBean::hello()");
    }

    @PreDestroy
	public void destroy() {
	System.out.println("In StatelessBean::destroy()");
    }

    

}