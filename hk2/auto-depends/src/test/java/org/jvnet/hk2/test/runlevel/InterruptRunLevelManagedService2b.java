package org.jvnet.hk2.test.runlevel;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.jvnet.hk2.annotations.RunLevel;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.PostConstruct;

/**
 * Used in testing interrupt handling, also belonging to another RunLevel environment.
 * 
 * @author Jeff Trent
 */
@Service
@RunLevel(value=2, environment=String.class)  // use of "String" is arbitrary --- just need a unique namespace
public class InterruptRunLevelManagedService2b implements RunLevelContract, PostConstruct {

  public static long i;
  
  public static boolean doSleep = true;

  @SuppressWarnings("static-access")
  @Override
  public void postConstruct() {
    Logger.getAnonymousLogger().log(Level.INFO, "entering hang state");
    // we expect to be interrupted
    while (true) {
      i++;
      try {
        if (doSleep) {
          Thread.currentThread().sleep(1);
        } else {
          synchronized (this) {
            wait();
          }
        }
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }
  }
  
}
