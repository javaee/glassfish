package com.sun.hk2.jsr330.test.components;

import org.jvnet.hk2.annotations.ContractProvided;
import org.jvnet.hk2.annotations.Service;

// hk2 style #2 (qualifier on class)
@ContractProvided(HelloService.class)
@Service
@Latin
public class LatinHelloService implements HelloService {

  @Override
  public String sayHello() {
    return "salve";
  }

}
