package com.acme;

import javax.ejb.*;
import javax.naming.InitialContext;
import javax.annotation.*;

@Singleton
@Startup
public class MultiBean {

    @Resource(name="multi")
    private String multi;

    @PostConstruct 
    private void init() {
	System.out.println("In init()");
	System.out.println("multi = " + multi);
	try {
	    String multiLookup = (String)
		new InitialContext().lookup("java:comp/env/multi");
	    if( !multi.equals(multiLookup) ) {
		throw new EJBException("Non-matching values of multi" + 
				       multi + " : " + multiLookup);
	    }

	} catch(Exception e) {
	    throw new EJBException(e);
	}
    }

    public String foo() {
	try {
	    String multiLookup = (String)
		new InitialContext().lookup("java:comp/env/multi");
	    System.out.println("multiLookup = " + multiLookup);
	    if( !multi.equals(multiLookup) ) {
		throw new EJBException("Non-matching values of multi" + 
				       multi + " : " + multiLookup);
	    }
	} catch(Exception e) {
	    throw new EJBException(e);
	}
	return multi;
    }

}
