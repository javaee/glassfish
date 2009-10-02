package com.acme;

import javax.ejb.*;
import javax.annotation.*;


@Singleton
public class Singleton4 {

    @PostConstruct
    public void init() {
        System.out.println("In SingletonBean4::init()");
    }
    
    @PreDestroy
    public void destroy() {
        System.out.println("In SingletonBean4::destroy()");
    }



}
