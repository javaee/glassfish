package org.jvnet.hk2.test.runlevel;

import org.jvnet.hk2.annotations.Service;

/**
 * A service annotated with a non-default RunLevel,
 * applying to some other RunLevelService.
 * 
 * @author Jeff Trent
 */
@ANonDefaultEnvRunLevel
@Service
public class ANonDefaultEnvServerService {
}
