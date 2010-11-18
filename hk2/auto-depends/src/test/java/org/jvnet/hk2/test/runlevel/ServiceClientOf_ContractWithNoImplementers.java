package org.jvnet.hk2.test.runlevel;

import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;

/**
 * Should be allowed to activate because there are no qualifying implementations installed for his
 * contract dependency.
 * 
 * @see RunLevelService#testOptionalDependencies()
 * 
 * @author Jeff Trent
 */
@Service
public class ServiceClientOf_ContractWithNoImplementers implements ShouldBeActivateable1 {

  @Inject(optional=true) ContractWithNoImplementers dep;

  @Override
  public void validateSelf() {
    assert(null == dep);
  }
  
}
