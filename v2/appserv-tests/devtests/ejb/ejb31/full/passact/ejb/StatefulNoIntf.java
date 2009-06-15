package com.acme;

import javax.ejb.*;
import javax.annotation.*;
import java.util.concurrent.Future;

@Stateful
public class StatefulNoIntf {

    @PostConstruct
    public void init() {
	System.out.println("In StatefulNoIntf::init()");
    }

    public String hello() {
	System.out.println("In StatefulNoIntf::hello()");
	return "hello, world\n";
    }

    @PreDestroy
    public void destroy() {
	System.out.println("In StatefulNoIntf::destroy()");
    }


}