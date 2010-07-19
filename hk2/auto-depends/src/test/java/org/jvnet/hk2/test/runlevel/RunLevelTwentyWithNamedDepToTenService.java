package org.jvnet.hk2.test.runlevel;

import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;

/**
 * A properly defined runlevel service.
 * 
 * @author Jeff Trent
 */
@RunLevelTwenty()
@Service
public class RunLevelTwentyWithNamedDepToTenService implements RunLevelContract {

  @Inject(name="RunLevel10")
  RunLevelContract anamedRunLevel10;
  
}
