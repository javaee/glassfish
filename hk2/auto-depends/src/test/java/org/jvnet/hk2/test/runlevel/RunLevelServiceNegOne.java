package org.jvnet.hk2.test.runlevel;

import org.jvnet.hk2.annotations.RunLevel;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.test.contracts.TestingInfoService;

/**
 * A service that will be initialized by the framework immediately.
 * 
 * @author Jeff Trent
 */
@RunLevel(-1)
@Service
public class RunLevelServiceNegOne implements TestingInfoService {

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
