package org.jvnet.hk2.component;

import java.util.concurrent.Executor;

import org.junit.Ignore;

/**
 * A Test-friendly Habitat
 *  
 * @author Jeff Trent
 */
@Ignore
public class TestHabitat extends Habitat {

  public final boolean CONCURRENCY_CONTROLS;

  public TestHabitat() {
    CONCURRENCY_CONTROLS = Habitat.CONCURRENCY_CONTROLS_DEFAULT; 
  }

  public TestHabitat(final Executor exec) {
    super(new TestExecutorService(exec), null);
    CONCURRENCY_CONTROLS = Habitat.CONCURRENCY_CONTROLS_DEFAULT; 
  }
  
  public TestHabitat(final Executor exec, boolean concurrency_controls) {
    super(new TestExecutorService(exec), concurrency_controls);
    CONCURRENCY_CONTROLS = concurrency_controls; 
  }
}
