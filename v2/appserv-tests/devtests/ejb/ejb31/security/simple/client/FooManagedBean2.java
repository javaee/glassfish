package com.acme;

import javax.annotation.ManagedBean;
import javax.annotation.Resource;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@ManagedBean
public class FooManagedBean2 {

  @Resource(lookup="java:app/AppName")
  String appName;

    @Resource(name="java:module/env/fmbAppName", lookup="java:app/AppName")
	String appName2;

    @Resource(lookup="java:module/env/fmbAppName")
	String appName3;

  @PostConstruct 
  private void init() {
    System.out.println("In FooManagedBean2::init()");
    System.out.println("appName = " + appName);
    System.out.println("appName2 = " + appName2);
    System.out.println("appName3 = " + appName3);
  } 

    public void hello() {
	System.out.println("In FooManagedBean2::hello()");
    }

    @PreDestroy
	private void destroy() {
	System.out.println("In FooManagedBean2::destroy()");
    }


}

