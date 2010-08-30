package org.jvnet.hk2.test.runlevel;

import org.jvnet.hk2.component.PostConstruct;
import org.jvnet.hk2.test.contracts.TestingInfoService;

public abstract class RunLevelServiceBase implements TestingInfoService, PostConstruct {

  protected boolean destroyed;
  
  public static int count;
  public int countStamp;
  
  @Override
  public void preDestroy() {
    destroyed = true;
    countStamp = count++;
  }

  @Override
  public boolean isPreDestroyed() {
    return destroyed;
  }

  @Override
  public void postConstruct() {
    countStamp = count++;
  }
  
}
