package org.jvnet.hk2.test.runlevel;

import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;

/**
 * This guy should NOT come alive because his dependency exists, but it failed to activate.
 * 
 * @see RunLevelService#testOptionalDependencies()
 * 
 * @author Jeff Trent
 */
@Service
public class ServiceClientOf_ContractWithExceptionThrowingImplementers implements ShouldNotBeActivateable1 {

  @Inject(optional=true) ContractWithExceptionThrowingImplementers dep;
  
}
