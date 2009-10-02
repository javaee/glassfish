package com.acme;

import javax.ejb.*;
import javax.annotation.*;


@Singleton
@DependsOn("./ejb-ejb31-full-ear-ejb.jar#Singleton4")
public class Singleton3 {

    @PostConstruct
    public void init() {
        System.out.println("In SingletonBean3::init()");
    }
    
    @PreDestroy
    public void destroy() {
        System.out.println("In SingletonBean3::destroy()");
    }



}
