package org.jvnet.hk2.test.runlevel;

import org.jvnet.hk2.annotations.Contract;

/**
 * A contract having no implementers in the system.
 * 
 * @see RunLevelService#testOptionalDependencies()
 * 
 * @author Jeff Trent
 */
@Contract
public interface ContractWithNoImplementers extends ShouldNotBeActivateable1 {

}
