package org.jvnet.hk2.test.runlevel;

import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;

/**
 * This guy should be able to come alive because his transitive dependency don't exists in the habitat.
 * 
 * @see RunLevelService#testOptionalDependencies()
 * 
 * @author Jeff Trent
 */
@Service
public class ServiceClientFirstRemovedOf_ContractWithNoImplementers implements ShouldBeActivateable1 {

  @Inject(optional=true) ServiceClientOf_ContractWithNoImplementers dep;

  @Override
  public void validateSelf() {
    assert(null != dep);
    dep.validateSelf();
  }
  
}
