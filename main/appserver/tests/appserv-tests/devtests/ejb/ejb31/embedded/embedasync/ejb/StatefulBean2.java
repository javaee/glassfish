package com.acme;

import javax.ejb.*;
import javax.annotation.*;

@Stateful
@StatefulTimeout(1) // defaults to 1 minute
public class StatefulBean2 {


    @PostConstruct
    public void init() {
        System.out.println("In StatefulBean2::init()");
    }

    public void hello() {
	System.out.println("In StatefulBean2::hello()");
    }

    @PreDestroy
    public void destroy() {
        System.out.println("In StatefulBean2::destroy()");
    }



}
