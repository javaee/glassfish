package org.jvnet.hk2.test.runlevel;

import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.test.contracts.TestingInfoService;

@RunLevelTen
@Service
public class RunLevelServiceA implements TestingInfoService, ServiceA {

  private boolean destroyed;
  
  @Inject
  ServiceB serviceB;

  @Override
  public void preDestroy() {
    destroyed = true;
  }

  @Override
  public boolean isPreDestroyed() {
    return destroyed;
  }
  
}
