package com.acme;

import javax.ejb.*;
import javax.annotation.*;
import org.omg.CORBA.ORB;

@Singleton
@Remote(SingletonRemote.class)
@LocalBean
public class SingletonBean {

    boolean passed = false;

    @PostConstruct
    public void init() {
        System.out.println("In SingletonBean::init()");
    }
    
    public void testPassed() {
	passed = true;
    }

    public boolean getTestPassed() {
	return passed;
    }

    @PreDestroy
    public void destroy() {
        System.out.println("In SingletonBean::destroy()");
    }



}
