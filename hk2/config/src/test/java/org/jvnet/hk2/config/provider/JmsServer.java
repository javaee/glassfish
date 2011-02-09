package org.jvnet.hk2.config.provider;

import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.ConfiguredBy;

/**
 * 
 * @author Jeff Trent
 */
@Service
@ConfiguredBy(JmsServerConfigBean.class)
public class JmsServer implements JmsServerService {

  public static int constructCount;
  public static int destroyCount;

  @Override
  public void postConstruct() {
    constructCount++;
  }
  
  @Override
  public void preDestroy() {
    destroyCount++;
  }
  
}
