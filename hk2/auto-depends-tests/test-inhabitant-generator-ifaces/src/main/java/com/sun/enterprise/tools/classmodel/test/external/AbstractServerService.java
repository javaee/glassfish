package com.sun.enterprise.tools.classmodel.test.external;

import org.jvnet.hk2.component.PostConstruct;
import org.jvnet.hk2.component.PreDestroy;

public abstract class AbstractServerService implements ServerService, PostConstruct, PreDestroy {

  @Override
  public void postConstruct() {
  }

  @Override
  public void preDestroy() {
  }

}
