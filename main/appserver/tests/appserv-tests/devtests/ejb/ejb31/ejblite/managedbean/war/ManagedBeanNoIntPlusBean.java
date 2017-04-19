package com.acme;

import javax.annotation.*;
import javax.interceptor.Interceptors;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;

@ManagedBean("ManagedBeanNoIntPlusBean")
public class ManagedBeanNoIntPlusBean extends ManagedBeanSuper {

    static private int numInstances = 0;
    static private int numInterceptorInstances = 0;

    private String aroundInvoke = "";

    public void newInterceptorInstance() {
	numInterceptorInstances++;
    }

    public int getNumInstances() {
	return numInstances;
    }

    public int getNumInterceptorInstances() {
	return numInterceptorInstances;
    }

     @AroundInvoke
    private Object roundInvoke(InvocationContext c) throws Exception {
	System.out.println("In ManagedBeanNoIntPlusBean::roundInvoke() ");
	if( c.getMethod().getName().equals("getAroundInvokeSequence") ) {
	    String result = (String) c.proceed();
	    return "M" + result;
	} else {
	    return c.proceed();
	}
    }

    @PostConstruct
    private void init() {
	numInstances++;
    }

    @PreDestroy
    private void destroy() {
    }

}