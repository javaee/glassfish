package com.sun.enterprise.tools.classmodel.test;

import org.jvnet.hk2.annotations.FactoryFor;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.ComponentException;
import org.jvnet.hk2.component.Factory;

@Service
@FactoryFor(CService.class)
public class FactoryForCService implements Factory {

  @Override
  public Object getObject() throws ComponentException {
    return new CService();
  }

}
