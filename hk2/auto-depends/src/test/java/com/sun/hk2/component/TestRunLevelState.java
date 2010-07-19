package com.sun.hk2.component;

import org.junit.Ignore;
import org.jvnet.hk2.component.RunLevelState;

/**
 * Test RunLevelState
 * @author Jeff Trent
 */
@Ignore
public class TestRunLevelState implements RunLevelState<Object> {
  Integer current;
  Integer planned;

  public TestRunLevelState(Integer current, Integer planned) {
    this.current = current;
    this.planned = planned;
  }
  
  @Override
  public Integer getCurrentRunLevel() {
    return current;
  }

  @SuppressWarnings("unchecked")
  @Override
  public Class getEnvironment() {
    return null;
  }

  @Override
  public Integer getPlannedRunLevel() {
    return planned;
  }
}
