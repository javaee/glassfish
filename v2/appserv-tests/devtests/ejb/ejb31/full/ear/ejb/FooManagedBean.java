package com.acme;

import javax.annotation.*;

import javax.ejb.EJB;
import javax.annotation.Resource;
import javax.transaction.UserTransaction;
import org.omg.CORBA.ORB;
import javax.persistence.PersistenceContext;
import javax.persistence.EntityManager;

@ManagedBean("somemanagedbean")
public class FooManagedBean extends ManagedSuper implements Foo {

    @EJB HelloRemote s;
    @Resource UserTransaction ut;
    @Resource BarManagedBean bmb;
    @PersistenceContext EntityManager em;

    @PostConstruct
    private void init() {
	System.out.println("In FooManagedBean::init() " + this);
    }
    

    public String getName() {
	return "somemanagedbean";
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