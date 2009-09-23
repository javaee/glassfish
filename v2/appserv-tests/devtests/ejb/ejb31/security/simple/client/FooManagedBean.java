package com.acme;

import javax.annotation.ManagedBean;
import javax.annotation.Resource;
import javax.annotation.PostConstruct;

@ManagedBean
public class FooManagedBean {

  @Resource(lookup="java:app/AppName")
  String appName;

  @PostConstruct 
  private void init() {
    System.out.println("In FooManagedBean::init()");
    System.out.println("appName = " + appName);
  } 

}

