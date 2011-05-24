package org.jvnet.hk2.component;

import static org.junit.Assert.*;

import org.junit.Test;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.junit.Hk2Test;

/**
 * Habitat default config & settings test
 * 
 * @author Jeff Trent
 */
public class HabitatConfigTest extends Hk2Test {

  @Inject
  Habitat h;
  
  @Test
  public void defaultConcurrencyMode() {
    assertFalse("concurrency controls default", h.concurrencyControls);
  }

  @Test
  public void managedInjectionPointMode() {
    assertFalse("managed injection point default", Habitat.MANAGED_INJECTION_POINTS_ENABLED);
  }
}
