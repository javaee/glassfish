package com.sun.enterprise.tools.classmodel.test;

import org.jvnet.hk2.annotations.FactoryFor;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.ComponentException;
import org.jvnet.hk2.component.Factory;

import com.sun.enterprise.tools.classmodel.test.SomeRandomClass.Inner;

@FactoryFor({String.class, Integer.class, Inner.class})
@Service
public class ServiceFactory implements Factory<Object> {

  @Override
  public Object get() throws ComponentException {
    // TODO Auto-generated method stub
    return null;
  }
  
}
