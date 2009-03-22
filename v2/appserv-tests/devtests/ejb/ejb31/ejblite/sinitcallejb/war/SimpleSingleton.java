package com.acme;

import javax.ejb.*;
import javax.annotation.*;

@Singleton
@Startup
public class SimpleSingleton {

     @PostConstruct
     private void init() {
	 System.out.println("In SimpleSingleton:init()");
     }

    public void hello() {
	 System.out.println("In SimpleSingleton:hello()");
    }

     @PreDestroy
     private void destroy() {
	 System.out.println("In SimpleSingleton:destroy()");
     }

    

}