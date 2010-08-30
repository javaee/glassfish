package org.jvnet.hk2.test.runlevel;

import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;

/**
 * @see RunLevelServiceTest
 * 
 * @author Jeff Trent
 */
@RunLevelTen
@Service
public class RunLevelServiceB extends RunLevelServiceBase implements ServiceB {

  @Inject
  ServiceC serviceC;

}
