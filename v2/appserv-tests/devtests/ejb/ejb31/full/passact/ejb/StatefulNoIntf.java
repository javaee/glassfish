package com.acme;

import javax.ejb.*;
import javax.annotation.*;
import java.util.concurrent.Future;

@Stateful
public class StatefulNoIntf {

    @EJB private StatelessNoIntf statelessNoIntf;

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

    @PrePassivate
    public void passivate() {
	System.out.println("In StatefulNoIntf::passivate()");
    }


}
