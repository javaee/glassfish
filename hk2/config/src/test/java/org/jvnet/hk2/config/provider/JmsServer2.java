package org.jvnet.hk2.config.provider;

import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.ConfiguredBy;

/**
 * Demonstrates how the presence of one bean can spawn the creation of multiple services.
 * 
 * @author Jeff Trent
 */
@Service
@ConfiguredBy(JmsServerConfigBean.class)
public class JmsServer2 implements JmsServerService2 {
  
  private final JmsServerConfigBean bean;

  private JmsServer2(JmsServerConfigBean bean) {
    this.bean = bean;
  }
  
  public String getName() {
    return bean.getName();
  }

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
