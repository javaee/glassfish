package com.sun.hk2.jsr330.test.components;

import org.jvnet.hk2.annotations.ContractProvided;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.Constants;

// hk2 style #2 (qualifier in metadata)
@ContractProvided(HelloService.class)
@Service(metadata=Constants.QUALIFIER + "=com.sun.hk2.jsr330.test.components.Spanish")
public class SpanishHelloService implements HelloService {

  @Override
  public String sayHello() {
    return "hola";
  }

}
