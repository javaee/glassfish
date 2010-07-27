package org.jvnet.hk2.test.runlevel;

import org.jvnet.hk2.annotations.Service;

/**
 * A service annotated with a non-existant RunLevel-RunLevelService.
 * 
 * @author Jeff Trent
 */
@ANonExistantEnvRunLevel
@Service
public class ANonExistantEnvServerService {
}
