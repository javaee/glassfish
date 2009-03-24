package com.acme;

import javax.ejb.*;
import javax.annotation.*;

@Stateless
@LocalBean
public class HelloStateless implements HelloRemote {

    @PostConstruct 
    private void init() {
	System.out.println("HelloStateless::init()");
    }

    public String hello() {
	System.out.println("In HelloStateless::hello()");	
	return "hello, world!\n";
    }

    @Asynchronous
    public void helloAsync() {
    	System.out.println("In HelloStateless::helloAsync()");	    
    }

    @PreDestroy
    private void destroy() {
	System.out.println("HelloStateless::destroy()");
    }

}
