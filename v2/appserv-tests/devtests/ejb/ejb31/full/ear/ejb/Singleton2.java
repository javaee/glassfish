package com.acme;

import javax.ejb.*;
import javax.annotation.*;


@Singleton
@DependsOn("ejb-ejb31-full-ear-ejb.jar#Singleton3")
public class Singleton2 {

    @PostConstruct
    public void init() {
        System.out.println("In SingletonBean2::init()");
    }
    
    @PreDestroy
    public void destroy() {
        System.out.println("In SingletonBean2::destroy()");
    }



}
