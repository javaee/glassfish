package org.jvnet.hk2.test.runlevel;

import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.RunLevelService;
import org.jvnet.hk2.component.RunLevelState;

/**
 * A testing non-default RunLevelService.
 * 
 * @author Jeff Trent
 */
@SuppressWarnings("unchecked")
@Service
public class ANonDefaultRunLevelService implements RunLevelService, RunLevelState {

  Integer current;
  Integer planned;
  boolean interrupted;
  
  
  @Override
  public RunLevelState getState() {
    return this;
  }

  @Override
  public void proceedTo(int runLevel) {
    current = runLevel;
  }

  @Override
  public void interrupt() {
    interrupted = true;
  }
  
  @Override
  public void interrupt(int runLevel) {
    interrupted = true;
    proceedTo(runLevel);
  }
  
  @Override
  public Integer getCurrentRunLevel() {
    return current;
  }

  @Override
  public Class getEnvironment() {
    return Object.class;
  }

  @Override
  public Integer getPlannedRunLevel() {
    return planned;
  }

}
