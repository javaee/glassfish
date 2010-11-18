package org.jvnet.hk2.test.runlevel;

import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.ComponentException;

/**
 * Should never activate.
 * 
 * @see RunLevelService#testOptionalDependencies()
 * 
 * @author Jeff Trent
 */
@Service
public class ServiceImplOf_ContractWithComponentExceptionThrowingImplementers implements ContractWithExceptionThrowingImplementers {

  @Override
  public void postConstruct() {
    throw new ComponentException("forced");
  }

}
