package org.jvnet.hk2.test.runlevel;

import org.jvnet.hk2.component.RunLevelService;
import org.jvnet.hk2.component.RunLevelState;

import com.sun.hk2.component.TestInhabitantListener;

/**
 * Used in Testing RunLevelService.
 * 
 * @author Jeff Trent
 */
public class SomeOtherRunLevelService extends TestInhabitantListener 
      implements RunLevelService<Object>, RunLevelState<Object> {

  Integer current;
  Integer planned;
  
  public SomeOtherRunLevelService() {
  }
  
  public SomeOtherRunLevelService(Integer current, Integer planned) {
    this.current = current;
    this.planned = planned;
  }
  
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
    return current;
  }

  @Override
  public Class<Object> getEnvironment() {
    return Object.class;
  }

  @Override
  public Integer getPlannedRunLevel() {
    return planned;
  }

  @Override
  public void interrupt() {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void interrupt(int runLevel) {
    // TODO Auto-generated method stub
    
  }

}
