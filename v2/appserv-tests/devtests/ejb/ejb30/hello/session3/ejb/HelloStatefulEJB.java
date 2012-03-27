package com.sun.s1asdev.ejb.ejb30.hello.session3;

import javax.ejb.Stateful;
import javax.ejb.AccessTimeout;
import javax.annotation.PostConstruct;

import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;
import javax.persistence.EntityManager;

@AccessTimeout(0)
@Stateful public class HelloStatefulEJB implements HelloStateful {

    
    @PersistenceContext(name="myem",
                        unitName="foo", type=PersistenceContextType.EXTENDED)
        private EntityManager em;

    @PostConstruct public void postConstruction() {
        System.out.println("In HelloStatefulEJB::postConstruction()");
    }

    public void hello() {
        System.out.println("In HelloStatefulEJB::hello()");
    }

    public void sleepFor(int sec) {
        System.out.println("In HelloStatefulEJB::sleepFor()");
	try {
	    for (int i=0 ; i<sec; i++) {
		Thread.currentThread().sleep(1000);
	    }
	} catch (Exception ex) {
	}
        System.out.println("Finished HelloStatefulEJB::sleepFor()");
    }

    public void ping() {
        System.out.println("In HelloStatefulEJB::ping()");
    }

}
