package org.jvnet.hk2.test.runlevel;

import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.PostConstruct;
import org.jvnet.hk2.component.PreDestroy;

/**
 * A properly declared RunLevel(10) service.
 * 
 * @author Jeff Trent
 */
@RunLevelTen
@Service(name="RunLevel10")
public class RunLevelTenNamedService implements RunLevelContract, PostConstruct, PreDestroy {

  @Override
  public void postConstruct() {
  }

  @Override
  public void preDestroy() {
  }

}
