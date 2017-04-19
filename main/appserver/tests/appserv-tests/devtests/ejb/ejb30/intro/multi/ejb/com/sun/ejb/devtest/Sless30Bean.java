package com.sun.ejb.devtest;

import javax.annotation.PostConstruct;

import javax.ejb.Stateless;
import javax.ejb.EJB;

@Stateless
public class Sless30Bean
    implements Sless30 {

    boolean ejbCreateCalled = false;

    public void ejbCreate() {
        this.ejbCreateCalled = true;
    }


    @PostConstruct
    private void myPostConstruct() {
    }

    public String sayHello() {
	return "Hello";
    }

    public boolean wasEjbCreateCalled() {
	return ejbCreateCalled;
    }

}
