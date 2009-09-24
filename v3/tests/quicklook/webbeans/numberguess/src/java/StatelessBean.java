package org.jboss.webbeans.examples.numberguess;

import javax.ejb.*;
import javax.annotation.*;

import org.omg.CORBA.ORB;

import javax.inject.Inject;

@Stateless
public class StatelessBean {


    @PostConstruct
	public void init() {
	System.out.println("In StatelessBean::init()");
    }

    public void hello() {
	System.out.println("In StatelessBean::hello()");
    }

    @PreDestroy
	public void destroy() {
	System.out.println("In StatelessBean::destroy()");
    }

    

}