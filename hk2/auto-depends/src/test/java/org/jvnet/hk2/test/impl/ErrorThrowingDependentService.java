package org.jvnet.hk2.test.impl;

import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.test.contracts.ErrorThrowingContract;
import org.jvnet.hk2.test.contracts.Simple;

/**
 * A Service dependent on a service that does not
 * get properly wired
 * 
 * @author Jeff Trent
 */
@Service(name="ErrorThrowingDependentService")
public class ErrorThrowingDependentService implements Simple {

  @Inject
  ErrorThrowingContract errorThrowing;

  @Override
  public String get() {
    return (null == errorThrowing) ? null : errorThrowing.toString();
  }
  
}
