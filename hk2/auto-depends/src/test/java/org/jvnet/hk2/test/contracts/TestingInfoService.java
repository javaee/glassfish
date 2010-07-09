package org.jvnet.hk2.test.contracts;

import org.jvnet.hk2.annotations.Contract;

/**
 * Used in Testing
 * 
 * @author Jeff Trent
 */
@Contract
public interface TestingInfoService {
  
  public boolean isPreDestroyed();
  
}
