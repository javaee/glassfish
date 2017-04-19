package com.acme;

import javax.ejb.*;
import javax.annotation.*;

import org.omg.CORBA.ORB;

@Stateless
    @EJB(name="java:global/GS1", beanName="HelloSingleton", beanInterface=Hello.class)
public class HelloStateless implements HelloRemote {

    @EJB(name="java:app/env/AS1", beanName="HelloSingleton")
    private Hello h;

    @Resource(name="java:module/MORB1")
    private ORB orb;
    
    @EJB(name="lookupref1", lookup="java:app/env/AS1")
    private Hello lookupref1;

    @EJB(name="lookupref2", lookup="java:global/GS1")
    private Hello lookupref2;

    @EJB(name="lookupref3", lookup="java:module/HelloStateless!com.acme.HelloRemote")
    private HelloRemote lookupref3;

    // declare component-level dependency using fully-qualified
    // java:comp/env form.  
    @Resource(name="java:comp/env/foo") SessionContext sessionCtx;

    @PostConstruct 
    private void init() {
	System.out.println("HelloStateless::init()");
    }

    public String hello() {
	System.out.println("In HelloStateless::hello()");

	String appName = (String) sessionCtx.lookup("java:app/AppName");
	String moduleName = (String) sessionCtx.lookup("java:module/ModuleName");
	System.out.println("AppName = " + appName);
	System.out.println("ModuleName = " + moduleName);

	ORB orb1 = (ORB) sessionCtx.lookup("java:module/MORB1");
	ORB orb2 = (ORB) sessionCtx.lookup("java:module/env/MORB2");

	Hello s1 = (Hello) sessionCtx.lookup("java:global/" +
					   appName + "/" +
					   moduleName + "/" +
					   "HelloSingleton");

	Hello s2 = (Hello) sessionCtx.lookup("java:app/" +
							 moduleName + "/" +
							 "HelloSingleton");

	// Rely on default to resolve "java:comp/env/ declared resource
	SessionContext sc1 = (SessionContext)
	    sessionCtx.lookup("foo");

	SessionContext sc2 = (SessionContext)
	    sc1.lookup("java:comp/env/foo");

	Integer envEntry = (Integer)
	    sc1.lookup("java:app/env/value1");
	System.out.println("java:ap/env/value1 = " + envEntry);

	return "hello, world!\n";
    }

    @PreDestroy
    private void destroy() {
	System.out.println("HelloStateless::destroy()");
    }

}
