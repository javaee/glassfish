package com.acme;

import javax.annotation.*;

import javax.ejb.EJB;
import javax.annotation.Resource;
import org.omg.CORBA.ORB;
import javax.persistence.PersistenceContext;
import javax.persistence.EntityManager;

@ManagedBean("somemanagedbean")
public class Foo2ManagedBean {

    @EJB HelloRemote s;
    @Resource ORB orb;
    @PersistenceContext EntityManager em;

    @PostConstruct
    private void init() {
	System.out.println("In Foo2ManagedBean::init() " + this);
    }
    
    public void foo() {
	System.out.println("In Foo2ManagedBean::foo() ");
	System.out.println("In Foo2ManagedBean::foo() testing @EJB: " + s.hello());
    }

    public Object getThis() {
	return this;
    }

    @PreDestroy
    private void destroy() {
	System.out.println("In Foo2ManagedBean::destroy() ");
    }


    public String toString() {
	return "Foo2ManagedBean this = " + 
			   " s = " + s + " , orb = " + orb + 
	    " , em = " + em;
    }

}
