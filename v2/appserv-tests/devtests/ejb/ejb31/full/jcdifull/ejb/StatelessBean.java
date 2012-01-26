package com.acme;

import javax.ejb.*;
import javax.annotation.*;
import javax.interceptor.ExcludeClassInterceptors;
import javax.interceptor.ExcludeDefaultInterceptors;
import javax.interceptor.Interceptors;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.enterprise.event.Observes;

@Stateless
@test.beans.interceptors.Another
@Interceptors(InterceptorA.class)
@LocalBean
public class StatelessBean implements StatelessLocal {

    private List<Integer> interceptorIds = new ArrayList<Integer>();

    @Inject Foo foo;

    @PostConstruct
	public void init() {
	System.out.println("In StatelessBean::init()");
    }

    public void processSomeEvent(@Observes SomeEvent event) {
	System.out.println("In StatelessBean::processSomeEvent " +
			   event);
    }

    public void hello() {
	System.out.println("In StatelessBean::hello() " +
			   foo);
        if (interceptorIds.size() != 2) {
            throw new IllegalStateException("Wrong number of interceptors were called: expected 2, got " + interceptorIds.size());
        } else if (interceptorIds.get(0) != 0 || interceptorIds.get(1) != 1) {
            throw new IllegalStateException("Interceptors were called in a wrong order");
        }

        interceptorIds.clear();
    }

    @ExcludeClassInterceptors
    @ExcludeDefaultInterceptors
    public void interceptorCalled(int id) {
	System.out.println("In StatelessBean::interceptorCalled() " + id);
        interceptorIds.add(id);
    }

    @PreDestroy
	public void destroy() {
	System.out.println("In StatelessBean::destroy()");
    }

    

}
