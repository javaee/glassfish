package org.jvnet.hk2.test.impl;

import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.test.contracts.DummyContract;
import org.jvnet.hk2.test.contracts.ErrorThrowingContract;
import org.jvnet.hk2.test.contracts.RandomContract;

/**
 * A service with a setter @Inject that throws an Exception
 * 
 * @author Jeff Trent
 */
@Service
public class ErrorThrowingService implements ErrorThrowingContract {

  @Inject(optional = true)
  DummyContract neverSatisfied;

  public RandomContract random;
  
  @Inject
  void fakeRandomContractThrowingUp(RandomContract rc) {
    throw new RuntimeException("forced exception in testing");
  }
  
}
