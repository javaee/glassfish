package org.jvnet.hk2.test.runlevel;

import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;

/**
 * This guy should NOT come alive because his transitive dependency exists, but it failed to activate.
 * 
 * @see RunLevelService#testOptionalDependencies()
 * 
 * @author Jeff Trent
 */
@Service
public class ServiceClientFirstRemovedOf_ContractWithExceptionThrowingImplementers implements ShouldNotBeActivateable1 {

  @Inject(optional=true) ServiceClientOf_ContractWithExceptionThrowingImplementers dep;
  
}
