package org.jvnet.hk2.test.runlevel;

import org.jvnet.hk2.annotations.Contract;

/**
 * Convenience, for getting services from the habitat that should be activateable by calling inhabitant.get()
 * 
 * @see RunLevelService#testOptionalDependencies()
 * 
 * @author Jeff Trent
 */
@Contract
public interface ShouldBeActivateable1 {

  /**
   * Expected to throw an exception if its not in a valid state
   */
  public void validateSelf();
  
}
