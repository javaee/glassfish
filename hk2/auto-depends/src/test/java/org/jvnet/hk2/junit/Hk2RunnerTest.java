package org.jvnet.hk2.junit;

import static org.junit.Assert.*;

import org.junit.Test;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.component.Enableable;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.component.RunLevelService;
import org.jvnet.hk2.component.TestHabitat;

/**
 * General tests of Hk2Runner & Hk2RunnerOptions.
 * 
 * @author Jeff Trent
 */
public class Hk2RunnerTest extends Hk2RunnerTestBase {

  @Inject Habitat h;
  
  @Inject(name="default") RunLevelService<?> rls;
  
  @Test
  public void testAll() {
    assertEquals("expected Hk2RunnerBase annotations to be honored", TestHabitat.class, h.getClass());
    assertTrue(rls instanceof Enableable);
    assertFalse("rls enabled but should be disabled", ((Enableable)rls).isEnabled());
  }
  
  
}
