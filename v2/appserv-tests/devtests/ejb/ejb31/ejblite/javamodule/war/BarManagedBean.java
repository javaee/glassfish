package com.acme;

import javax.annotation.*;
import javax.ejb.EJB;
import javax.annotation.Resource;
import javax.sql.DataSource;

@ManagedBean
public class BarManagedBean {

    @EJB StatelessBean s;
    @Resource(name="jdbc/__default") DataSource ds;

    @PostConstruct
    private void init() {
	System.out.println("In BarManagedBean::init() ");
    }

   public void bar() {
       System.out.println("In BarManagedBean::bar() ");
    }


    @PreDestroy
    private void destroy() {
	System.out.println("In BarManagedBean::destroy() ");
    }

    public String toString() {
	return "BarManagedBean this = " + super.toString() + 
	    " s = " + s + " , ds = " + ds;

    }

}
