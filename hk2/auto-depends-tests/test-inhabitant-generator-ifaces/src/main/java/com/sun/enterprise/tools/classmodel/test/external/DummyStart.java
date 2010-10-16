package com.sun.enterprise.tools.classmodel.test.external;

import org.jvnet.hk2.annotations.Service;

import com.sun.enterprise.module.bootstrap.ModuleStartup;
import com.sun.enterprise.module.bootstrap.StartupContext;

// TODO: This should really not be necessary!
@Service
public class DummyStart implements ModuleStartup {

  @Override
  public void setStartupContext(StartupContext context) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void start() {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void stop() {
    // TODO Auto-generated method stub
    
  }

}
