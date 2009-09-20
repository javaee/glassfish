package com.acme;

import javax.ejb.*;
import javax.annotation.*;
import javax.annotation.security.*;
import org.omg.CORBA.ORB;
import java.util.concurrent.*;

@Singleton
    @Remote({ Hello.class, Hello2.class})
//    @Remote(Hello.class)
@LocalBean
    @ConcurrencyManagement(ConcurrencyManagementType.BEAN)
    @EJB(name="java:app/env/forappclient", beanInterface=Hello.class)
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

	Hello meViaAppClientDefinedDependency = (Hello)
	    sessionCtx.lookup("java:app/env/appclientdefinedejbref1");
	System.out.println("meViaAppClientDefinedDependency =" +
			   meViaAppClientDefinedDependency);
	Hello meViaAppClientDefinedDependency2 = (Hello)
	    sessionCtx.lookup("java:app/appclientdefinedejbref2");

	Hello meViaAppClientDefinedDependency3 = (Hello)
	    sessionCtx.lookup("java:global/appclientdefinedejbref3");

	String appLevelEnvEntry = (String)
	    sessionCtx.lookup("java:app/env/enventry1");
	System.out.println("appLevelEnvEntry = " + appLevelEnvEntry);

	String globalLevelEnvEntry = (String)
	    sessionCtx.lookup("java:global/enventry2");
	System.out.println("globalLevelEnvEntry = " + globalLevelEnvEntry);

    }

    public void blah() { }

    @RolesAllowed("foo")
    public void protectedSyncRemote() {
	System.out.println("In SingletonBean::protectedSyncRemote cp = " +
			   sessionCtx.getCallerPrincipal()  + " , " +
			   Thread.currentThread());
    }
    @PermitAll
    public void unprotectedSyncRemote() {
	System.out.println("In SingletonBean::unprotectedSyncRemote cp = " +
			   sessionCtx.getCallerPrincipal()  + " , " +
			   Thread.currentThread());
    }

    @RolesAllowed("foo")
    @Asynchronous
    public Future<Object> protectedAsyncRemote() {
	System.out.println("In SingletonBean::protectedAsyncRemote cp = " +
			   sessionCtx.getCallerPrincipal()  + " , " +
			   Thread.currentThread());
	return new AsyncResult<Object>(new String());
    }

    @Asynchronous
    @PermitAll
    public Future<Object> unprotectedAsyncRemote() {
	System.out.println("In SingletonBean::unprotectedAsyncRemote cp = " +
			   sessionCtx.getCallerPrincipal()  + " , " +
			   Thread.currentThread());
	return new AsyncResult<Object>(new String());
    }

    @RolesAllowed("foo")
    @Asynchronous
    public Future<Object> protectedAsyncLocal() {
	System.out.println("In SingletonBean::protectedAsyncLocal cp = " +
			   sessionCtx.getCallerPrincipal()  + " , " +
			   Thread.currentThread());
	return new AsyncResult<Object>(new String());
    }

    @Asynchronous
    @PermitAll
    public Future<Object> unprotectedAsyncLocal() {
	System.out.println("In SingletonBean::unprotectedAsyncLocal cp = " +
			   sessionCtx.getCallerPrincipal()  + " , " +
			   Thread.currentThread());
	return new AsyncResult<Object>(new String());
    }

    @RolesAllowed("foo")
    public void protectedSyncLocal() {
	System.out.println("In SingletonBean::protectedSyncLocal cp = " +
			   sessionCtx.getCallerPrincipal()  + " , " +
			   Thread.currentThread());
	return;
    }

    
    @PermitAll
    public void unprotectedSyncLocal() {
	System.out.println("In SingletonBean::unprotectedSyncLocal cp = " +
			   sessionCtx.getCallerPrincipal()  + " , " +
			   Thread.currentThread());
	return ;
    }

    @PermitAll
    public void testProtectedSyncLocal() {
	me.protectedSyncLocal();
    }

    @PermitAll
    public void testProtectedAsyncLocal() {
	try {
	    Future<Object> future = me.protectedAsyncLocal();
	    Object obj = future.get();
	} catch(Exception ee) {
	    if( ee.getCause() instanceof EJBAccessException) {
		throw (EJBAccessException) ee.getCause();
	    }
	}
    }

    @PermitAll
    public void testUnprotectedSyncLocal() {
	me.unprotectedSyncLocal();

    }

    @PermitAll
    public void testUnprotectedAsyncLocal() {
	try {
	    Future<Object> future = me.unprotectedAsyncLocal();
	    Object obj = future.get();
	    // Success
	} catch(Exception ee) {
	    throw (EJBException) new EJBException("Got unexpected exception").initCause(ee.getCause());
	}
    }
    


    @PreDestroy
    public void destroy() {
        System.out.println("In SingletonBean::destroy()");
    }
    


}
