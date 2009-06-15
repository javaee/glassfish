package com.acme;

import javax.ejb.*;
import javax.annotation.*;
import java.util.concurrent.Future;

@Stateless
public class StatelessNoIntf {

    @PostConstruct
    public void init() {
	System.out.println("In StatelessNoIntf::init()");
    }

    public String hello() {
	System.out.println("In StatelessNoIntf::hello()");
	return "hello, world\n";
    }

    @PreDestroy
    public void destroy() {
	System.out.println("In StatelessNoIntf::destroy()");
    }


}