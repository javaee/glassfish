package com.acme;

import javax.annotation.ManagedBean;
import javax.annotation.Resource;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;


@ManagedBean("FooManagedBean")
public class FooManagedBean {

  @Resource(lookup="java:app/AppName")
  String appName;

  @Resource
  private FooManagedBean2 fmb2;

  @PostConstruct 
  private void init() {
    System.out.println("In FooManagedBean::init()");
    System.out.println("appName = " + appName);
    fmb2.hello();

  } 

    public void hello() {
	System.out.println("In FooManagedBean::hello()");
    }
    
    @PreDestroy
	private void destroy() {
	System.out.println("In FooManagedBean::destroy()");
    }

}

