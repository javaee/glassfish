package com.sun.hk2.component;

import org.junit.Ignore;
import org.jvnet.hk2.component.Inhabitant;
import org.jvnet.hk2.component.InhabitantListener;
import org.jvnet.hk2.component.RunLevelState;

/**
 * Used in testing.
 * 
 * @author Jeff Trent
 */
@SuppressWarnings("unchecked")
@Ignore
public class TestRunLevelInhabitant extends RunLevelInhabitant {

  public TestRunLevelInhabitant(Inhabitant<?> delegate, int runLevel,
      RunLevelState<?> state, InhabitantListener listener) {
    super(delegate, runLevel, state, listener);
  }
  

}
