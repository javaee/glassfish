package org.jvnet.hk2.test.runlevel;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.jvnet.hk2.annotations.RunLevel;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.PostConstruct;
import org.jvnet.hk2.component.RunLevelService;

/**
 * Used in testing interrupt handling, also belonging to another RunLevel environment.
 * 
 * @author Jeff Trent
 */
@Service
@RunLevel(value=1, environment=String.class)  // use of "String" is arbitrary --- just need a unique namespace
public class InterruptRunLevelManagedService1a implements RunLevelContract, PostConstruct {

  public static RunLevelService<?> rls;
  
  public static boolean swallowExceptionsInProceedTo;
  
  @Override
  public void postConstruct() {
    Logger.getAnonymousLogger().log(Level.INFO, "here");
    if (null != rls) {
      Logger.getAnonymousLogger().log(Level.INFO, "proceedTo(2)");
      RunLevelService<?> r = rls;
      rls = null;
      
      if (swallowExceptionsInProceedTo) {
        try {
          r.proceedTo(2);
        } catch (Exception e) {
          Logger.getAnonymousLogger().log(Level.INFO, "Swallowed Exception", e);
        }
      } else {
        r.proceedTo(2);
      }
    }
  }
  
}
