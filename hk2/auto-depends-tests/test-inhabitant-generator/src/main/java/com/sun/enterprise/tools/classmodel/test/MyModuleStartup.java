package com.sun.enterprise.tools.classmodel.test;

import org.jvnet.hk2.annotations.Service;

import com.sun.enterprise.module.bootstrap.StartupContext;

@Service(name="startup")
public class MyModuleStartup implements MyBaseModuleStartupContract {

  @Override
  public void setStartupContext(StartupContext context) {
  }

  @Override
  public void start() {
  }

  @Override
  public void stop() {
  }

}
