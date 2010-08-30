package org.jvnet.hk2.test.runlevel;

import org.jvnet.hk2.annotations.RunLevel;
import org.jvnet.hk2.annotations.Service;

/**
 * @see RunLevelServiceTest
 * 
 * @author Jeff Trent
 */
@RunLevel(10)
@Service
public class RunLevelServiceC extends RunLevelServiceBase implements ServiceC {
  
}
