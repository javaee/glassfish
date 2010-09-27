package org.jvnet.hk2.test.runlevel;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.jvnet.hk2.annotations.RunLevel;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.PostConstruct;
import org.jvnet.hk2.component.PreDestroy;

/**
 * Used in testing interrupt handling, also belonging to another RunLevel environment.
 * 
 * @author Jeff Trent
 */
@Service
@RunLevel(value=2, environment=String.class)  // use of "String" is arbitrary --- just need a unique namespace
public class InterruptRunLevelManagedService2b implements RunLevelContract, PostConstruct, PreDestroy {

  public static volatile long i;
  
  public static volatile Object self;

  public static volatile boolean breakOut;
  
  public static boolean doSleep = true;

  
  @SuppressWarnings("static-access")
  @Override
  public void postConstruct() {
    self = this;

    Logger.getAnonymousLogger().log(Level.INFO, "entering hang state");
    // we expect to be interrupted
    while (true) {
      i++;
      try {
        if (doSleep) {
          Thread.currentThread().sleep(1);
        } else {
          synchronized (this) {
            wait(50);
          }
        }
        
        if (breakOut) {
          breakOut = false;
          return;
        }
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }
  }

  @Override
  public void preDestroy() {
    self = null;
  }
  
}
