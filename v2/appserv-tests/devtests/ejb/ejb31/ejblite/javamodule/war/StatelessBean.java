package com.acme;

import javax.ejb.Stateless;
import javax.ejb.*;
import javax.interceptor.Interceptors;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;

import java.util.Map;

@Stateless
@Interceptors(InterceptorB.class)
public class StatelessBean {
    
    @Resource 
    private SessionContext sessionCtx;

    @Resource(mappedName="java:module/foomanagedbean")
    private FooManagedBean foo;

    @Resource(mappedName="java:app/ejb-ejb31-ejblite-javamodule-web/foomanagedbean")
    private FooManagedBean foo2;

    @EJB(name="stateless/singletonref")
    private SingletonBean singleton;

    @PostConstruct
    private void init() {
	System.out.println("In StatelessBean:init()");
    }

    public void hello() {
	System.out.println("In StatelessBean::hello()");

	Map<String, Object> ctxData = sessionCtx.getContextData();
	String fooctx = (String) ctxData.get("foo");
	System.out.println("foo from context data = " + fooctx);
	if( fooctx == null ) {
	    throw new EJBException("invalid context data");
	}
	ctxData.put("foobar", "foobar");

	FooManagedBean fmb = (FooManagedBean) 
	    sessionCtx.lookup("java:module/foomanagedbean");

	// Make sure dependencies declared in java:comp are visible
	// via equivalent java:module entries since this is a 
	// .war
	SessionContext sessionCtx2 = (SessionContext)
	    sessionCtx.lookup("java:module/env/com.acme.StatelessBean/sessionCtx");

	SingletonBean singleton2 = (SingletonBean)
	    sessionCtx2.lookup("java:module/env/stateless/singletonref");

	// Lookup a comp env dependency declared by another ejb in the .war
	SingletonBean singleton3 = (SingletonBean)
	    sessionCtx2.lookup("java:comp/env/com.acme.SingletonBean/me");

	// Lookup a comp env dependency declared by a servlet
	FooManagedBean fmbServlet = (FooManagedBean)
	    sessionCtx.lookup("java:comp/env/foo2ref");
	FooManagedBean fmbServlet2 = (FooManagedBean)
	    sessionCtx.lookup("java:module/env/foo2ref");

	// Ensure that each injected or looked up managed bean 
	// instance is unique
	Object fooThis = foo.getThis();
	Object foo2This = foo2.getThis();
	Object fmbThis = fmb.getThis();

	System.out.println("fooThis = " + fooThis);
	System.out.println("foo2This = " + foo2This);
	System.out.println("fmbThis = " + fmbThis);
	System.out.println("fmbServlet = " + fmbServlet);
	System.out.println("fmbServlet2 = " + fmbServlet2);

	if( ( fooThis == foo2This ) || ( fooThis == fmbThis  ) ||
	    ( foo2This == fmbThis ) ) {
	    throw new EJBException("Managed bean instances not unique");
	}

    }

    @PreDestroy
    private void destroy() {
	System.out.println("In StatelessBean:destroy()");
    }


}
