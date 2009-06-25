package com.acme;

import javax.annotation.*;

import javax.ejb.EJB;
import javax.annotation.Resource;
import javax.transaction.UserTransaction;
import org.omg.CORBA.ORB;
import javax.persistence.PersistenceContext;
import javax.persistence.EntityManager;

@ManagedBean("foomanagedbean")
public class FooManagedBean extends ManagedSuper {

    @EJB StatelessBean s;
    @Resource UserTransaction ut;
    @Resource BarManagedBean bmb;
    @PersistenceContext EntityManager em;

    @PostConstruct
    private void init() {
	System.out.println("In FooManagedBean::init() " + this);
    }

    public void foo() {
	System.out.println("In FooManagedBean::foo() ");
	bmb.bar();
    }

    public Object getThis() {
	return this;
    }

    @PreDestroy
    private void destroy() {
	System.out.println("In FooManagedBean::destroy() ");
    }


    public String toString() {
	return "FooManagedBean this = " + super.toString() + 
			   " s = " + s + " , ut = " + ut + 
	    " , bmb = " + bmb + " , em = " + em;
    }

}