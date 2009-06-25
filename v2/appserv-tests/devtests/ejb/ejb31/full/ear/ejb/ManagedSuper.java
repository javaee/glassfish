package com.acme;

import javax.annotation.*;
import javax.ejb.EJB;
import javax.annotation.Resource;
import javax.transaction.UserTransaction;

public class ManagedSuper {

    @EJB Hello s;
    @Resource UserTransaction ut;

    @PostConstruct
    private void init() {
	System.out.println("In ManagedSuper::init() " + this);
    }

    @PreDestroy
    private void destroy() {
	System.out.println("In ManagedSuper::destroy() ");
    }

    public String toString() {
	return "ManagedSuper this = " + super.toString() + 
	    " s = " + s + " , ut = " + ut;

    }

}