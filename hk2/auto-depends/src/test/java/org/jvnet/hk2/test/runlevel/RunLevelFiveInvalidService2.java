package org.jvnet.hk2.test.runlevel;

import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.RunLevel;
import org.jvnet.hk2.annotations.Service;

/**
 * An improperly declared RunLevel(5) service.
 * 
 * @author Jeff Trent
 */
@RunLevel(5)
@Service
public class RunLevelFiveInvalidService2 implements RunLevelContract {

  // this is at RunLevel 10
  @Inject
  SomeServerService ss;
  
}
