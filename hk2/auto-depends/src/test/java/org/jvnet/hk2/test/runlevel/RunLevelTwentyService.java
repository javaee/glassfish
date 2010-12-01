package org.jvnet.hk2.test.runlevel;

import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.PostConstruct;
import org.jvnet.hk2.test.contracts.Simple;
import org.jvnet.hk2.test.impl.PerLookupService;

/**
 * A properly defined runlevel service.
 * 
 * @author Jeff Trent
 */
@RunLevelTwenty()
@Service
public class RunLevelTwentyService implements RunLevelContract, PostConstruct {

  @Inject
  public PerLookupService perLookupService;
  
  @Inject(name="one")
  public Simple simple; 
  
  @Override
  public void postConstruct() {
    // intentionally slow this one down a bit
    try {
      Thread.currentThread().sleep(500);
    } catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

}
