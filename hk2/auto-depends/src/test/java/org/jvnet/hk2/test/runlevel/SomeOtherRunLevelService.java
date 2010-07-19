package org.jvnet.hk2.test.runlevel;

import org.jvnet.hk2.component.RunLevelService;
import org.jvnet.hk2.component.RunLevelState;

/**
 * Used in Testing RunLevelService.
 * 
 * @author Jeff Trent
 */
public class SomeOtherRunLevelService implements RunLevelService<Object>, RunLevelState<Object> {

  @Override
  public RunLevelState<Object> getState() {
    return this;
  }

  @Override
  public void proceedTo(int runLevel) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public Integer getCurrentRunLevel() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Class<Object> getEnvironment() {
    return Object.class;
  }

  @Override
  public Integer getPlannedRunLevel() {
    // TODO Auto-generated method stub
    return null;
  }

}
