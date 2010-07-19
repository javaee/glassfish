package org.jvnet.hk2.test.runlevel;

import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.PostConstruct;

/**
 * A properly defined runlevel service.
 * 
 * @author Jeff Trent
 */
@RunLevelTwenty()
@Service
public class RunLevelTwentyService implements RunLevelContract, PostConstruct {

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
