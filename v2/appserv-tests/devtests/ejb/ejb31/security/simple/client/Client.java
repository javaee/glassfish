package com.acme;


import javax.ejb.*;
import javax.annotation.*;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import java.util.concurrent.*;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

import com.sun.enterprise.security.ee.auth.login.ProgrammaticLogin;

public class Client {

    private static SimpleReporterAdapter stat = 
        new SimpleReporterAdapter("appserv-tests");

    private static String appName;
    private static String principal;

    @EJB static Hello hello;

    @Resource
    static FooManagedBean2 fooMb2;

    @Resource(lookup="java:module/FooManagedBean")
    static FooManagedBean fooMb;

    @Resource(lookup="java:comp/env/com.acme.Client/fooMb2")
    static FooManagedBean2 fooMb3;

    @Resource(lookup="java:module/ModuleName")
    static String moduleName;

    @Resource(lookup="java:app/AppName")
    static String appNameL;


    @EJB(lookup="java:app/env/forappclient") 
    static Hello hello3;

    @EJB(mappedName="java:app/ejb-ejb31-security-simple-ejb/SingletonBean!com.acme.Hello") 
    static Hello hello4;

    @EJB(name="java:app/env/appclientdefinedejbref1") 
    static Hello hello5;

    @EJB(name="java:app/appclientdefinedejbref2") 
    static Hello hello6;

    @EJB(name="java:global/appclientdefinedejbref3") 
    static Hello hello7;

    @Resource(name="java:app/env/enventry1")
    static String envEntry1;

    @Resource(name="java:global/enventry2")
    static String envEntry2;

    @Resource(name="envEntry3")
    static Integer envEntry3;

    @Resource(name="envEntry4", lookup="java:app/env/value1")
    static Integer envEntry4;

    private static boolean appClient = false;

 @PostConstruct
    public static void init() {
	try {
	    System.out.println("In init()");

	    appClient = true;

	    System.out.println("AppName = " + appNameL);
	    System.out.println("ModuleName = " + moduleName);

	    fooMb.hello();
	    fooMb2.hello();
	    fooMb3.hello();

	    Integer envEntry = (Integer)
		new InitialContext().lookup("java:app/env/value1");
	    System.out.println("java:app/env/value1 = " + envEntry);
	
	    System.out.println("java:app/env/enventry1 = " + envEntry1);
	    System.out.println("java:global/enventry2 = " + envEntry2);
	    System.out.println("java:global/enventry3 = " + envEntry3);
	    if( (envEntry3 == null) || envEntry3.intValue() != 18338 ) {
		throw new RuntimeException("invalid enventry3 value");
	    }
	    
	    System.out.println("java:global/enventry4 = " + envEntry4);
	    if( (envEntry4 == null) || envEntry4.intValue() != 18338 ) {
		throw new RuntimeException("invalid enventry4 value");
	    }

	    FooManagedBean fmbl = (FooManagedBean) new InitialContext().lookup("java:module/FooManagedBean");
	    fmbl.hello();

	     FooManagedBean fmbl2 = (FooManagedBean) new InitialContext().lookup("java:app/ejb-ejb31-security-simple-client/FooManagedBean");
	    fmbl2.hello();

	} catch(NamingException e) {
	    throw new RuntimeException(e);
	}
    }

    public static void main(String args[]) {
        for (String arg : args) {
            System.out.println("Arg: " + arg);
        }
	appName = args[0];
	principal = args[1];
	stat.addDescription(appName);
	Client client = new Client(args);       
        client.doTest();	
        stat.printSummary(appName + "ID");
    }

    public Client(String[] args) {}

    public void doTest() {

	System.out.println("Executing test with user principal " + principal);
	boolean havePermission = principal.equals("bob");
	if( havePermission ) {
	    System.out.println("Expecting permission to access protected methods");
	} else {
	    System.out.println("NOT expecting permission to access protected methods");
	}


	String results;

	try {
	    
	    /**
	    if( !appClient ) {
		System.out.println("In SE client.  Using programmatic login");
		ProgrammaticLogin pm = new ProgrammaticLogin();
		pm.login("mary", "mob", "default", true);
		System.out.println("Programmatic login succeeded");
		
	    }
	    */

	    //	    ProgrammaticLogin login = new com.sun.appserv.security.api.ProgrammaticLogin();
	    
	    if( hello == null ) {
		hello = (Hello) new InitialContext().lookup("java:global/ejb-ejb31-security-simpleApp/ejb-ejb31-security-simple-ejb/SingletonBean!com.acme.Hello");
	    } else {
		// In an appclient.  
		Hello hello2 = (Hello) new InitialContext().lookup("java:app/env/forappclient");
		System.out.println("java:app/env/forappclient lookup = " + hello2);
		System.out.println("hello3 = " + hello3);

		Hello hello5 = (Hello) new InitialContext().lookup("java:app/ejb-ejb31-security-simple-ejb/SingletonBean!com.acme.Hello");
		System.out.println("hello4 = " + hello4);
		System.out.println("hello5 = " + hello5);
		String env = (String) new InitialContext().lookup("java:app/enventryforappclient");
		System.out.println("java:app env entry = " + env);
	    }

	    boolean pass;

	    try {
		hello.protectedSyncRemote();
		pass = havePermission; 
	    } catch(EJBAccessException e) {
		pass = !havePermission;
	    }
	    

	    System.out.println("pass = " + pass);

	    try {
		hello.unprotectedSyncRemote();
		pass = true;
	    } catch(EJBAccessException e) {
		pass = false;
	    }


	    System.out.println("pass = " + pass);

	    try {
		Future<Object> future = hello.protectedAsyncRemote();
		Object obj = future.get();
		pass = havePermission;
		
	    } catch(ExecutionException ee) {
		if( ee.getCause() instanceof EJBAccessException ) {
		    pass = !havePermission;
		} else {
		    pass = false;
		}
	    }


	    System.out.println("pass = " + pass);


	    try {
		Future<Object> future = hello.unprotectedAsyncRemote();
		Object obj = future.get();
		pass = true;
	    } catch(ExecutionException ee) {
		pass = false;
	    }



	    System.out.println("pass = " + pass);


	    try {
		hello.testProtectedSyncLocal();
		pass = havePermission;
	    }  catch(EJBAccessException e) {
		pass = !havePermission;
	    }



	    System.out.println("pass = " + pass);


	    try {
		hello.testUnprotectedSyncLocal();
		pass = true;
	    }  catch(Exception e) {
		pass = false;
	    }


	    System.out.println("pass = " + pass);


	    try {
		hello.testProtectedAsyncLocal();
		pass = havePermission;
	    }  catch(EJBAccessException e) {
		pass = !havePermission;
	    }



	    System.out.println("pass = " + pass);

	    try {
		hello.testUnprotectedAsyncLocal();
		pass = true;
	    }  catch(Exception e) {
		pass = false;
	    }

	    System.out.println("pass = " + pass);

	    stat.addStatus("local main", stat.PASS);

	} catch(NamingException ne) {
	    
	    if( appClient ) {
		stat.addStatus("local main", stat.FAIL);
	    } else {
		System.out.println("Got expected security failure during lookup for non-authenticated SE client");
		stat.addStatus("local main", stat.PASS);
	    }

	} catch(Exception e) {
	    stat.addStatus("local main", stat.FAIL);
	    e.printStackTrace();
	}
    }


}
