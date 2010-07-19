package org.jvnet.hk2.test.runlevel;

import org.jvnet.hk2.annotations.RunLevel;
import org.jvnet.hk2.annotations.Service;

/**
 * A properly declared RunLevel(5) service.
 * 
 * @author Jeff Trent
 */
@RunLevel(5)
@Service
public class RunLevelFiveService implements RunLevelContract {

}
