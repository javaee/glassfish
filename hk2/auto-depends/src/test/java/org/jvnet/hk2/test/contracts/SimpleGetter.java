package org.jvnet.hk2.test.contracts;

import org.jvnet.hk2.annotations.Contract;

/**
 * A contract used to represent it supports a Simple getter.
 *  
 * @author Jeff Trent
 */
@Contract
public interface SimpleGetter extends TestingInfoService {

  public Simple getSimple();
  
}
