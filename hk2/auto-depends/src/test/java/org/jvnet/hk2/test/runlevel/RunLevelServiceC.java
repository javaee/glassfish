package org.jvnet.hk2.test.runlevel;

import org.jvnet.hk2.annotations.RunLevel;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.test.contracts.TestingInfoService;

/**
 * @see RunLevelServiceTest
 * 
 * @author Jeff Trent
 */
@RunLevel(10)
@Service
public class RunLevelServiceC implements TestingInfoService, ServiceC {

  private boolean destroyed;
  
  @Override
  public void preDestroy() {
    destroyed = true;
  }

  @Override
  public boolean isPreDestroyed() {
    return destroyed;
  }
  
}
