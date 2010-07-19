package org.jvnet.hk2.test.runlevel;

import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.test.contracts.TestingInfoService;

/**
 * @see RunLevelServiceTest
 * 
 * @author Jeff Trent
 */
@RunLevelTen
@Service
public class RunLevelServiceB implements TestingInfoService, ServiceB {

  private boolean destroyed;
  
  @Inject
  ServiceC serviceC;

  @Override
  public void preDestroy() {
    destroyed = true;
  }

  @Override
  public boolean isPreDestroyed() {
    return destroyed;
  }
  
}
