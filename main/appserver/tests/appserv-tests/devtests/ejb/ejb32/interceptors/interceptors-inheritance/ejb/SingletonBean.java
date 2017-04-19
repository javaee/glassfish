package com.acme;

import javax.ejb.*;
import javax.annotation.*;
import javax.interceptor.*;

@Singleton
@Startup
@Interceptors(InterceptorA.class)
public class SingletonBean implements Snglt {


    @EJB Sful sful;

    @PostConstruct
    public void init() {
        System.out.println("In SingletonBean::init()");
        sful.hello();
    }
    
    public String hello() {
	System.out.println("In SingletonBean::hello()");
        sful.remove();
	return "hello, world!\n";
    }

    @PreDestroy
    public void destroy() {
        System.out.println("In SingletonBean::destroy()");
    }
}
