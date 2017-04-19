package com.acme;

import javax.ejb.*;
import javax.annotation.*;
import javax.interceptor.*;

import javax.inject.Inject;

public class Bar {

    @Inject Bar2 bar2;

    @Resource(name="jdbc/__default") javax.sql.DataSource ds;

    public Bar() {
	System.out.println("Constructed::Bar");
    }

    @PostConstruct
    public void init() {
        System.out.println("In Bar::init()");
        if( bar2 == null ) {
            throw new EJBException("bar2 is null");
        }
        if( ds == null ) {
            throw new EJBException("ds is null");
        }
        System.out.println("bar2 inject = " + bar2);
        System.out.println("ds inject = " + ds);
    }

    public String toString() {
	return "Bar";
    }

}
