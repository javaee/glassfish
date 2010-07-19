package org.jvnet.hk2.test.runlevel;

import org.jvnet.hk2.annotations.Service;

/**
 * A properly declared RunLevel(10) service.
 * 
 * @author Jeff Trent
 */
@RunLevelTen()
@Service
public class RunLevelTenService implements RunLevelContract {

}
