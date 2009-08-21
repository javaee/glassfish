package com.acme;

import javax.ejb.*;
import javax.annotation.*;
import java.util.concurrent.*;

@Singleton
@Startup
@Remote(RemoteAsync.class)
@Local(SuperAsync.class)
public class SingletonBean {

    @EJB
    private SuperAsync meLocal;

    @PostConstruct
    public void init() {
        System.out.println("In SingletonBean::init()");
	Future<String> future = meLocal.hello("init");

    }

    @Asynchronous
    public Future<String> hello(String name) {
	System.out.println("In SingletonBean::hello( " + name + ")");
	return new AsyncResult<String>("Hello, " + name);
    }


    @PreDestroy
    public void destroy() {
        System.out.println("In SingletonBean::destroy()");
    }



}
