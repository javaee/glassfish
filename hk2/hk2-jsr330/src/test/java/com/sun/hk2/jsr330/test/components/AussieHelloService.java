package com.sun.hk2.jsr330.test.components;

// jsr-330 style #2 (@Qualifier in binding)
public class AussieHelloService implements HelloService {

  @Override
  public String sayHello() {
    return "g'day";
  }

}
