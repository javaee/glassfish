package com.acme;

import javax.ejb.*;
import javax.annotation.*;

@Stateful
public class SimpleStateful {

     @PostConstruct
     private void init() {
	 System.out.println("In SimpleStateful:init()");
     }

    public void hello() {
	 System.out.println("In SimpleStateful:hello()");
    }

     @PreDestroy
     private void destroy() {
	 System.out.println("In SimpleStateful:destroy()");
     }

    

}