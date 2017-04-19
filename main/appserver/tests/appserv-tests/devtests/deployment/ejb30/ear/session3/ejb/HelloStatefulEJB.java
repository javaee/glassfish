package com.sun.s1asdev.ejb.ejb30.hello.session3;

import javax.ejb.Stateful;
import javax.annotation.PostConstruct;

@Stateful public class HelloStatefulEJB implements HelloStateful {

    
    @PostConstruct public void postConstruction() {
        System.out.println("In HelloStatefulEJB::postConstruction()");
    }

    public void hello() {
        System.out.println("In HelloStatefulEJB::hello()");
    }

    public void sleepFor(int sec) {
	try {
	    for (int i=0 ; i<sec; i++) {
		Thread.currentThread().sleep(1000);
	    }
	} catch (Exception ex) {
	}
    }

    public void ping() {
    }

}
