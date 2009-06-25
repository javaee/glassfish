package com.acme;

import javax.annotation.*;
import javax.ejb.EJB;
import javax.annotation.Resource;
import javax.transaction.UserTransaction;

@ManagedBean
public class BarManagedBean {

    @EJB StatelessBean s;
    @Resource UserTransaction ut;

    @PostConstruct
    private void init() {
	System.out.println("In BarManagedBean::init() " + this);
    }

   public void bar() {
       System.out.println("In BarManagedBean::bar() ");
    }


    @PreDestroy
    private void destroy() {
	System.out.println("In BarManagedBean::destroy() ");
    }

    public String toString() {
	return "BarManagedBean this = " + super.toString() + 
	    " s = " + s + " , ut = " + ut;

    }

}