package com.acme;

import javax.ejb.*;
import javax.annotation.*;

import javax.naming.*;

@Singleton
@Startup
public class SingletonBean {

    @EJB Hello hello;
    @EJB HelloHome helloHome;

    @PostConstruct
    public void init() {
        System.out.println("In SingletonBean::init()");

	try {
	    HelloRemote hr = helloHome.create();
	    System.out.println("HellohelloRemote.hello() says " + hr.hello());

	    System.out.println("Hello.hello() says " + hello.hello());

	    InitialContext ic = new InitialContext();

	    SessionContext ctx = (SessionContext) 
		ic.lookup("java:module/env/sesCtx");

	    SingletonBean me = (SingletonBean)
		ic.lookup("java:app/ejb-ejb31-full-remote1-ejb/SingletonBean");
	    
	    SingletonBean meToo = (SingletonBean)
		ic.lookup("java:app/ejb-ejb31-full-remote1-ejb/SingletonBean!com.acme.SingletonBean");

	    Hello m1 = (Hello) ic.lookup("java:module/env/M1");

	    HelloHome m2 = (HelloHome) ctx.lookup("java:module/M2");

	    Hello a1 = (Hello) ctx.lookup("java:app/env/A1");

	    HelloHome a2 = (HelloHome) ic.lookup("java:app/A2");

	    try {
		ic.lookup("java:comp/env/C1");
		throw new EJBException("Expected exception accessing private component environment entry of HelloBean");
	    } catch(NamingException e) {
		System.out.println("Successfully did *not* find HelloBean private component environment dependency");
	    }

	    try {
		ic.lookup("java:comp/C1");
		throw new EJBException("Expected exception accessing private component environment entry of HelloBean");
	    } catch(NamingException e) {
		System.out.println("Successfully did *not* find HelloBean private component environment dependency");
	    }

	    System.out.println("My AppName = " + 
			       ctx.lookup("java:app/AppName"));

	    System.out.println("My ModuleName = " + 
			       ctx.lookup("java:module/ModuleName"));

			       

	} catch(Exception e) {
	    throw new EJBException("singleton init error" , e);
	}

    }

    @PreDestroy
    public void destroy() {
        System.out.println("In SingletonBean::destroy()");
    }



}
