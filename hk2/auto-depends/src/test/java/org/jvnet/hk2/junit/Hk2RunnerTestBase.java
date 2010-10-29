package org.jvnet.hk2.junit;

import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.jvnet.hk2.component.ComponentException;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.component.HabitatFactory;
import org.jvnet.hk2.component.TestHabitat;

/**
 * Testing Hk2Runner & Hk2RunnerOptions when annotations reside on parent class
 * 
 * @author Jeff Trent
 */
@RunWith(Hk2Runner.class)
@Hk2RunnerOptions(habitatFactory=TestHabitatFactory.class, enableDefaultRunLevelService=false)
public abstract class Hk2RunnerTestBase {

}

@Ignore
class TestHabitatFactory implements HabitatFactory {
  @Override
  public Habitat newHabitat() throws ComponentException {
    return new TestHabitat();
  }

}

