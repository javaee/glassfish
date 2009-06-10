package com.acme;

import javax.ejb.*;
import javax.annotation.*;
import javax.annotation.security.*;
import org.omg.CORBA.ORB;

@Singleton
@Remote(Hello.class)
@RunAs("foo")
@LocalBean
public class SingletonBean {

    @Resource
    private ORB orb;

    @EJB
    private SingletonBean me;

    @Resource
    private SessionContext sessionCtx;

    @PostConstruct
    public void init() {
        System.out.println("In SingletonBean::init()");
        System.out.println("orb = " + orb);
	if( orb == null ) {
	    throw new EJBException("null ORB");
	}
    }

//    @RolesAllowed("foo")
    public void protectedMethod() {

	System.out.println("In SingletonBean::protected() cp = " +
			   sessionCtx.getCallerPrincipal());

    }

//    @RolesAllowed("foo")
    public String hello() {
	System.out.println("In SingletonBean::hello() cp = " +
			   sessionCtx.getCallerPrincipal());

	me.protectedMethod();

	return "hello, world!\n";
    }

    @PreDestroy
    public void destroy() {
        System.out.println("In SingletonBean::destroy()");
    }



}
