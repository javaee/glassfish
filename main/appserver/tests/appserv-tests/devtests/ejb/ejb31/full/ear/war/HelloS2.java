package com.acme;

import javax.ejb.*;
import javax.annotation.*;

import org.omg.CORBA.ORB;


@Singleton
public class HelloS2 {

    @PostConstruct
    public void init() {
        System.out.println("In HelloS2::init()");
    }

    @PreDestroy
    private void destroy() {
	System.out.println("HelloS2::destroy()");
    }

}


