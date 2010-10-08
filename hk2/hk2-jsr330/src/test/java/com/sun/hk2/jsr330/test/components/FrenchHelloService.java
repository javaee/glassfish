package com.sun.hk2.jsr330.test.components;

import org.jvnet.hk2.annotations.ContractProvided;
import org.jvnet.hk2.annotations.Service;

// hk2 style #1 (named)
@Service(name="French")
@ContractProvided(HelloService.class)
public class FrenchHelloService implements HelloService {

  @Override
  public String sayHello() {
    return "bonjour";
  }

}
