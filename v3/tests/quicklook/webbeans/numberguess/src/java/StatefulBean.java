package org.jboss.webbeans.examples.numberguess;

import javax.ejb.*;
import javax.annotation.*;

import org.omg.CORBA.ORB;

import javax.inject.Inject;

@Stateful
public class StatefulBean {

    @Resource
	private ORB orb;

    @EJB
	private SingletonBean singleton;

    @PostConstruct
	public void init() {
	System.out.println("In StatefulBean::init()");
	System.out.println("orb = " + orb);
	if( orb == null ) {
	    throw new EJBException("EE injection error");
	}
	singleton.hello();
    }

    public void hello() {
	System.out.println("In StatefulBean::hello()");
    }

    @PreDestroy
	public void destroy() {
	System.out.println("In StatefulBean::destroy()");
    }

    

}