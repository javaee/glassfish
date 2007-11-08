package com.sun.ejb.devtest;

import javax.annotation.PostConstruct;

import javax.ejb.Stateless;
import javax.ejb.EJB;

@Stateless
public class SlessBean
    implements Sless {

    boolean ejbCreateCalled = false;

    public void ejbCreate() {
        this.ejbCreateCalled = true;
    }

    public String sayHello() {
	return "Hello";
    }

    public boolean wasEjbCreateCalled() {
	return ejbCreateCalled;
    }

}
