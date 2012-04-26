package org.jvnet.hk2.config.provider;

import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.config.ConfiguredBy;

/**
 * 
 * @author Jeff Trent
 */
@Service(name="webserver")
@ConfiguredBy(WebServerConfigBean.class)
public class WebServer implements ServerService {

  @Inject
  Habitat h;

  // this is not the preferred way to inject, ctor injection is, but this should still be permissible
  @Inject(optional = true)
  public WebServerConfigBean webServerConfigBean;
  
  
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
