package com.acme;

import javax.ejb.*;
import javax.annotation.*;

import org.omg.CORBA.ORB;


@Singleton
@Startup
    @EJB(name="java:app/env/AS2", beanName="HelloStateless", beanInterface=HelloRemote.class)
    @DependsOn("Singleton2")
public class HelloSingleton implements Hello {

    @Resource SessionContext sessionCtx;

    @Resource(mappedName="java:module/foobarmanagedbean")
    private FooBarManagedBean fbmb;

    @Resource
    private FooBarManagedBean fbmb2;

    @Resource(name="java:module/env/MORB2")
    private ORB orb;

    @Resource 
    private FooManagedBean foo;
    
    @Resource(name="foo2ref", mappedName="java:module/somemanagedbean")
    private FooManagedBean foo2;

    @Resource(name="foo3ref", mappedName="java:module/somemanagedbean")
    private Foo foo3;

    private FooManagedBean foo4;
    private FooManagedBean foo5;
    private Foo foo6;
    private FooManagedBean foo7;
    private Foo foo8;

    @Resource(name = "java:app/env/myString")
    protected String myString;

    @EJB(name="java:app/env/appLevelEjbRef")
    private Hello hello;
    
    String appName;
    String moduleName;

    @PostConstruct    
    private void init() {
	System.out.println("HelloSingleton::init()");

	System.out.println("myString = '" + myString + "'");
	if( (myString == null) || !(myString.equals("myString") ) ) {
	    throw new RuntimeException("Invalid value " + myString + " for myString");
	}

	appName = (String) sessionCtx.lookup("java:app/AppName");
	moduleName = (String) sessionCtx.lookup("java:module/ModuleName");

	ORB orb1 = (ORB) sessionCtx.lookup("java:module/MORB1");
	ORB orb2 = (ORB) sessionCtx.lookup("java:module/env/MORB2");

	System.out.println("AppName = " + appName);
	System.out.println("ModuleName = " + moduleName);

	foo4 = (FooManagedBean) sessionCtx.lookup("java:module/somemanagedbean");
	foo5 = (FooManagedBean) sessionCtx.lookup("java:app/" + moduleName +
						  "/somemanagedbean");
	foo6 = (Foo) sessionCtx.lookup("java:app/" + moduleName +
						  "/somemanagedbean");
	foo7 = (FooManagedBean) sessionCtx.lookup("java:comp/env/foo2ref");
	foo8 = (Foo) sessionCtx.lookup("java:comp/env/foo3ref");
    }

    public String hello() {
	
	System.out.println("HelloSingleton::hello()");


	foo.foo();
	foo2.foo();
	foo3.foo();

	foo4.foo();
	foo5.foo();
	foo6.foo();
	foo7.foo();
      	foo8.foo();

	return "hello, world!\n";
    }


    @PreDestroy
    private void destroy() {
	System.out.println("HelloSingleton::destroy()");
    }

}


