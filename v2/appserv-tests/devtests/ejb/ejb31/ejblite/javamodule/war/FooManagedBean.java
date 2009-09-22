package com.acme;

import javax.annotation.*;

import javax.ejb.EJB;
import javax.annotation.Resource;
import org.omg.CORBA.ORB;
import javax.persistence.PersistenceContext;
import javax.persistence.EntityManager;
import javax.interceptor.Interceptors;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;

@ManagedBean("foomanagedbean")
@Interceptors(InterceptorA.class)
public class FooManagedBean extends ManagedSuper {

    @EJB StatelessBean s;
    @Resource ORB orb;
    @Resource BarManagedBean bmb;
    @PersistenceContext EntityManager em;

    @PostConstruct
    private void init() {
	System.out.println("In FooManagedBean::init() ");
    }

    public void foo() {
	System.out.println("In FooManagedBean::foo() ");
	bmb.bar();
    }

    public void foobar(String s) {
	System.out.println("foobar::s = " + s);
    }

    public Object getThis() {
	return this;
    }

    public int returnIntNoExceptions() { return 1; }

    public Integer returnIntegerNoExceptions() { return 1; }

    /**
    public void noReturnNoExceptions() {}

    public void noReturnException() throws Exception {}

    public int returnExceptions() throws Exception { return 1; }

    public void param(int i, String j) {}
    */

    @PreDestroy
    private void destroy() {
	System.out.println("In FooManagedBean::destroy() ");
    }


    public String toString() {
	return "FooManagedBean this = " + super.toString() + 
			   " s = " + s + " , orb = " + orb + 
	    " , bmb = " + bmb + " , em = " + em;
    }

    @AroundInvoke
    public Object around(InvocationContext c) throws Exception {
	System.out.println("In FooManagedBean::around() ");
	return c.proceed();
    }

}
