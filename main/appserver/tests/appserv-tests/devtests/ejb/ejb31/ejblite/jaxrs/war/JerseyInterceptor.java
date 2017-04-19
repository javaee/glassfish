package com.sun.jersey;

import javax.interceptor.InvocationContext;
import javax.annotation.PostConstruct;

import com.acme.SingletonBean;
import com.acme.StatelessBean;
import com.acme.FooManagedBean;

public class JerseyInterceptor {

    @PostConstruct
    private void init(InvocationContext context) throws Exception {

	Object beanInstance = context.getTarget();

	System.out.println("In JerseyInterceptor::init() : " + 
			   beanInstance);


	if( beanInstance instanceof SingletonBean ) {
	    ((SingletonBean) beanInstance).interceptorWasHere = true;
	} else if( beanInstance instanceof StatelessBean ) {
	    ((StatelessBean) beanInstance).interceptorWasHere = true;
	} else if( beanInstance instanceof FooManagedBean ) {
	    ((FooManagedBean) beanInstance).interceptorWasHere = true;
	}

	// ...

	// Invoke next interceptor in chain
	context.proceed();

    }


}