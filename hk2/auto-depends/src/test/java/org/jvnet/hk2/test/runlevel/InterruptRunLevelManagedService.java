package org.jvnet.hk2.test.runlevel;

import org.jvnet.hk2.annotations.RunLevel;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.PostConstruct;

/**
 * Used in testing interrupt handling, also belonging to another RunLevel environment.
 * 
 * @author Jeff Trent
 */
@Service
@RunLevel(value=1, environment=String.class)  // use of "String" is arbitrary --- just need a unique namespace
public class InterruptRunLevelManagedService implements RunLevelContract, PostConstruct {

  @SuppressWarnings("static-access")
  @Override
  public void postConstruct() {
    // we expect to be interrupted
    long x = 0;
    while (true) {
      x++;
      try {
        Thread.currentThread().sleep(1);
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }
  }
  
}
