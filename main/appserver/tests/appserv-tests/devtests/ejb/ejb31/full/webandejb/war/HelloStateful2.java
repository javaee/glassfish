package com.acme;

import javax.ejb.*;
import javax.annotation.*;

@Stateful
@LocalBean
public class HelloStateful2 implements java.util.Observer {

    @PostConstruct 
    private void init() {
	System.out.println("HelloStateful2::init()");
    }

    public String hello() {
	System.out.println("In HelloStateful2::hello()");	
	return "hello, world!\n";
    }


    @Remove
    public void goodbye() {}

    @PreDestroy
    private void destroy() {
	System.out.println("HelloStateful2::destroy()");
    }

    // not part of public interface
    public void update(java.util.Observable o, Object a) {
	throw new EJBException("shouldn't be invoked by client");
    }

}
