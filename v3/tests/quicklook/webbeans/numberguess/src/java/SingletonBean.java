package org.jboss.webbeans.examples.numberguess;

import javax.ejb.*;
import javax.annotation.*;

import org.omg.CORBA.ORB;

import javax.inject.Inject;

@Singleton
// TODO enable after classloading bug fix @Startup
public class SingletonBean {

    @Resource
    private ORB orb;
    
    /** causing classloading bug
    @Inject
	private StatelessBean statelessBean;
    */

    @PostConstruct
	public void init() {
	System.out.println("In SingletonBean::init()");
	System.out.println("orb = " + orb);
	if( orb == null ) {
	    throw new EJBException("EE injection error");
	}
    }

    public void hello() {
	System.out.println("In SingletonBean::hello()");
    }

    @PreDestroy
	public void destroy() {
	System.out.println("In SingletonBean::destroy()");
    }

    

}