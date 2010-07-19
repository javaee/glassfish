package org.jvnet.hk2.test.runlevel;

import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.test.contracts.Simple;

/**
 * Non-RunLevel services musn't have dependencies to RunLevel services.
 * 
 * @author Jeff Trent
 */
@Service
public class NonRunLevelWithRunLevelDepService implements Simple {

  @Inject
  public SomeServerService ss;

  @Override
  public String get() {
    return null;
  }
  
}
