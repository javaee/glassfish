package org.jvnet.hk2.config.provider;

import org.jvnet.hk2.config.Configured;

/**
 * Testing a Configured annotated Class with a getName() method.
 * 
 * @author Jeff Trent
 */
@Configured
public class JmsServerConfigBean {
  
  private String name;

  public JmsServerConfigBean(String name) {
    this.name = name;
  }
  
  public String getName() {
    return name;
  }

}
