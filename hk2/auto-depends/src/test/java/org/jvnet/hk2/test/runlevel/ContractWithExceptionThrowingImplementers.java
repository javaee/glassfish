package org.jvnet.hk2.test.runlevel;

import org.jvnet.hk2.annotations.Contract;
import org.jvnet.hk2.component.PostConstruct;

/**
 * A contract having implementers that throw exceptions in their PostConstruct
 * 
 * @see RunLevelService#testOptionalDependencies()
 * 
 * @author Jeff Trent
 */
@Contract
public interface ContractWithExceptionThrowingImplementers extends PostConstruct, ShouldNotBeActivateable1 {

}
