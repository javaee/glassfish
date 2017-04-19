package com.acme;

import javax.ejb.*;
import javax.annotation.*;

import org.omg.CORBA.ORB;


@Singleton
@Startup
@DependsOn("ejb-ejb31-full-ear-web.war#HelloS2")
public class HelloS {

    @PostConstruct
    public void init() {
        System.out.println("In HelloS::init()");
    }

    @PreDestroy
    private void destroy() {
	System.out.println("HelloS::destroy()");
    }

}


