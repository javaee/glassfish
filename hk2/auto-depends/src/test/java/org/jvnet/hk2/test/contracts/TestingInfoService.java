package org.jvnet.hk2.test.contracts;

import org.jvnet.hk2.annotations.Contract;
import org.jvnet.hk2.component.PreDestroy;

/**
 * Used in Testing
 * 
 * @author Jeff Trent
 */
@Contract
public interface TestingInfoService extends PreDestroy {
  
  public boolean isPreDestroyed();
  
}
