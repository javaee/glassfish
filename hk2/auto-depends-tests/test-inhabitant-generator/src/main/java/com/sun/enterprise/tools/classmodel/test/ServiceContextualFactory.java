package com.sun.enterprise.tools.classmodel.test;

import java.security.AccessControlContext;

import org.jvnet.hk2.annotations.FactoryFor;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.ComponentException;
import org.jvnet.hk2.component.ContextualFactory;
import org.jvnet.hk2.component.InjectionPoint;

import com.sun.enterprise.tools.classmodel.test.SomeRandomClass.Inner;

@FactoryFor({String.class, Integer.class, Inner.class})
@Service
public class ServiceContextualFactory implements ContextualFactory<Object> {

  @Override
  public Object get() throws ComponentException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Object getObject(InjectionPoint injectionPoint,
      AccessControlContext acc) throws ComponentException {
    // TODO Auto-generated method stub
    return null;
  }
  
}
