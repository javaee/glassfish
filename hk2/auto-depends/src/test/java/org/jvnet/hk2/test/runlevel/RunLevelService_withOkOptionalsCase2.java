package org.jvnet.hk2.test.runlevel;

import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.RunLevel;
import org.jvnet.hk2.annotations.Service;

/**
 * 
 * @see RunLevelService#testOptionalDependencies()
 * 
 * @author Jeff Trent
 */
@RunLevel(value=1, environment=OptionalRunLevelTstEnv.class)
@Service(name="ok.case2")
public class RunLevelService_withOkOptionalsCase2 implements ShouldBeActivateable1 {

  @Inject(optional=true)
  ServiceClientFirstRemovedOf_ContractWithNoImplementers dep;
  
  @Override
  public void validateSelf() {
    assert(null != dep);
    dep.validateSelf();
  }

}
