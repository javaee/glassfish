package com.acme;

import javax.ejb.*;
import javax.annotation.*;
import javax.interceptor.*;

@Singleton
@Interceptors(InterceptorA.class)
public class SingletonBean extends BaseBean implements Snglt {


    @EJB SfulEJB sful;

    @Interceptors(InterceptorB.class)
    public SingletonBean() {}

    private SingletonBean(String s) {}

    @PostConstruct
    public void init() {
        System.out.println("In SingletonBean::init()");
        verifyMethod("init");
        sful.hello();
    }
    
    @Interceptors(InterceptorC.class)
    public String hello() {
        verifyAB_AC("SingletonBean");
	System.out.println("In SingletonBean::hello()");
        sful.remove();
	return "hello, world!\n";
    }

    @PreDestroy
    public void destroy() {
        System.out.println("In SingletonBean::destroy()");
        verifyMethod("destroy");
    }
}
