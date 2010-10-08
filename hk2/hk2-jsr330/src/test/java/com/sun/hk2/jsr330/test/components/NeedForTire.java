package com.sun.hk2.jsr330.test.components;

import org.atinject.tck.auto.Tire;
import org.jvnet.hk2.annotations.Service;

@Service
public class NeedForTire {

  @javax.inject.Inject
  public Tire aTire;
  
}
