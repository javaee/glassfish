package com.sun.hk2.jsr330.test.components;

// jsr-330 style #1 (@Named in the binding)
public class EnglishHelloService implements HelloService {

  @Override
  public String sayHello() {
    return "hello";
  }

}
