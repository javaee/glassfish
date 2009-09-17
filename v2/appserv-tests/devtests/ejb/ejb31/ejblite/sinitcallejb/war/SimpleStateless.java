package com.acme;

import javax.ejb.*;
import javax.annotation.*;

@Stateless
public class SimpleStateless {

     @PostConstruct
     private void init() {
	 System.out.println("In SimpleStateless:init()");
     }

    public void hello() {
	 System.out.println("In SimpleStateless:hello()");
    }

    protected void helloProtected() {
	 System.out.println("In SimpleStateless:helloProtected()");
    }

    void helloPackage() {
	 System.out.println("In SimpleStateless:helloPackage()");
    }

     @PreDestroy
     private void destroy() {
	 System.out.println("In SimpleStateless:destroy()");
     }

    

}