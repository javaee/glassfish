package org.jvnet.hk2.test.runlevel;

import org.jvnet.hk2.annotations.RunLevel;
import org.jvnet.hk2.annotations.Service;

/**
 * Used in testing exception handling, also belonging to another RunLevel environment.
 * 
 * @author Jeff Trent
 */
@Service
@RunLevel(value=2, environment=Exception.class)
public class ExceptionRunLevelManagedService2a implements RunLevelContract {
}
