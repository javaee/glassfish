package org.jvnet.hk2.test.runlevel;

import org.jvnet.hk2.annotations.Contract;

/**
 * The opposite of ShouldBeActivatable. These should never be active in the habitat under any circumstances.
 * 
 * @see RunLevelService#testOptionalDependencies()
 * 
 * @author Jeff Trent
 */
@Contract
public interface ShouldNotBeActivateable1 {

}
