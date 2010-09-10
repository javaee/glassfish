package org.jvnet.hk2.test.runlevel;

import org.jvnet.hk2.annotations.RunLevel;
import org.jvnet.hk2.annotations.Service;

/**
 * Used in testing interrupt handling, also belonging to another RunLevel environment.
 * 
 * @author Jeff Trent
 */
@Service
@RunLevel(value=2, environment=String.class)  // use of "String" is arbitrary --- just need a unique namespace
public class InterruptRunLevelManagedService2a implements RunLevelContract {

}
