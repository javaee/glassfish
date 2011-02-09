package org.jvnet.hk2.config.provider;

import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.ConfiguredBy;

/**
 * 
 * @author Jeff Trent
 */
@Service
@ConfiguredBy(EjbServerConfigBean.class)
public class EjbServer /*implements JmsServerService*/ {

  public EjbServer() {
    @SuppressWarnings("unused")
    int dummy = 0;
  }
  
}
