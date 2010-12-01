package org.jvnet.hk2.test;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.component.Inhabitant;
import org.jvnet.hk2.junit.Hk2Runner;
import org.jvnet.hk2.junit.Hk2RunnerOptions;
import org.jvnet.hk2.test.impl.PerLookupService;
import org.jvnet.hk2.test.impl.PerLookupServiceNested1;
import org.jvnet.hk2.test.impl.PerLookupServiceNested2;
import org.jvnet.hk2.test.impl.PerLookupServiceNested3;

import com.sun.hk2.component.InjectInjectionResolver;

/**
 * Most of the managed injection point tests are found in {@link ExtendedInjectTest}.
 * 
 * This test was added in order to get a "fresh" habitat.
 * 
 * @author Jeff Trent
 */
@RunWith(Hk2Runner.class)
@Hk2RunnerOptions(reinitializePerTest=true)
public class ManagedInjectTest {

  @Inject
  Habitat h;

  @Before
  public static void reset() {
    ExtendedInjectTest.reset();
  }
  
  /**
   * Very similar to {@link ExtendedInjectTest#managedip_perLookupByType_scopedClone_PostConstructAndPreDestroys()}
   * except that we manually get the inhabitant out of the habitat instead of through injection.
   * 
   * Also, this does not scopeClone() so in essence the old behavior applies.
   */
  // TODO: we should consider having the habitat return scopeClone's for casual callers.  Since we recommend inject this is not that big of a problem.
  @Test
  public void getFromHabitat_noScoping() {
    Inhabitant<PerLookupService> i = h.getInhabitantByType(PerLookupService.class);
    assertNotNull(i);
    assertFalse(i.isInstantiated());
   
    assertEquals("PostConstruct(s)", 0, PerLookupService.constructs);
    assertEquals("PostConstruct(s)", 0, PerLookupServiceNested1.constructs);
    assertEquals("PostConstruct(s)", 0, PerLookupServiceNested2.constructs);
    assertEquals("PostConstruct(s)", 0, PerLookupServiceNested3.constructs);
    assertEquals("PreDestroy(s)", 0, PerLookupService.destroys);
    assertEquals("PreDestroy(s)", 0, PerLookupServiceNested1.destroys);
    assertEquals("PreDestroy(s)", 0, PerLookupServiceNested2.destroys);
    assertEquals("PreDestroy(s)", 0, PerLookupServiceNested3.destroys);

    i.get();
    
    assertEquals("PostConstruct(s)", 1, PerLookupService.constructs);
    assertEquals("PostConstruct(s)", 1, PerLookupServiceNested1.constructs);
    assertEquals("PostConstruct(s)", 1, PerLookupServiceNested2.constructs);
    assertEquals("PostConstruct(s)", 1, PerLookupServiceNested3.constructs);
    assertEquals("PreDestroy(s)", 0, PerLookupService.destroys);
    assertEquals("PreDestroy(s)", 0, PerLookupServiceNested1.destroys);
    assertEquals("PreDestroy(s)", 0, PerLookupServiceNested2.destroys);
    assertEquals("PreDestroy(s)", 0, PerLookupServiceNested3.destroys);

    i.release();

    // old behavior because we are not using a scoped clone
    assertEquals("PreDestroy(s) after 1 releases", 0, PerLookupService.destroys);
    if (InjectInjectionResolver.MANAGED_ENABLED) {
      // at least the inner perLookup injection points get cleaned up under the new behavior
      assertEquals("PreDestroy(s) after 1 releases", 1, PerLookupServiceNested1.destroys);
      assertEquals("PreDestroy(s) after 1 releases", 1, PerLookupServiceNested2.destroys);
      assertEquals("PreDestroy(s) after 1 releases", 1, PerLookupServiceNested3.destroys);
    } else {
      assertEquals("PreDestroy(s) after 1 releases", 0, PerLookupServiceNested1.destroys);
      assertEquals("PreDestroy(s) after 1 releases", 0, PerLookupServiceNested2.destroys);
      assertEquals("PreDestroy(s) after 1 releases", 0, PerLookupServiceNested3.destroys);
    }
  }

  /**
   * Very similar to {@link ExtendedInjectTest#managedip_perLookupByType_scopedClone_PostConstructAndPreDestroys()}
   * except that we manually get the inhabitant out of the habitat instead of through injection.
   * 
   * Also, this uses scopeClone() getting the new behavior.
   */
  @Test
  public void getFromHabitat_withScoping() {
    Inhabitant<PerLookupService> i = h.getInhabitantByType(PerLookupService.class);
    assertNotNull(i);
    assertFalse(i.isInstantiated());
    
    i = i.scopedClone();
   
    assertEquals("PostConstruct(s)", 0, PerLookupService.constructs);
    assertEquals("PostConstruct(s)", 0, PerLookupServiceNested1.constructs);
    assertEquals("PostConstruct(s)", 0, PerLookupServiceNested2.constructs);
    assertEquals("PostConstruct(s)", 0, PerLookupServiceNested3.constructs);
    assertEquals("PreDestroy(s)", 0, PerLookupService.destroys);
    assertEquals("PreDestroy(s)", 0, PerLookupServiceNested1.destroys);
    assertEquals("PreDestroy(s)", 0, PerLookupServiceNested2.destroys);
    assertEquals("PreDestroy(s)", 0, PerLookupServiceNested3.destroys);

    i.get();
    
    assertEquals("PostConstruct(s)", 1, PerLookupService.constructs);
    assertEquals("PostConstruct(s)", 1, PerLookupServiceNested1.constructs);
    assertEquals("PostConstruct(s)", 1, PerLookupServiceNested2.constructs);
    assertEquals("PostConstruct(s)", 1, PerLookupServiceNested3.constructs);
    assertEquals("PreDestroy(s)", 0, PerLookupService.destroys);
    assertEquals("PreDestroy(s)", 0, PerLookupServiceNested1.destroys);
    assertEquals("PreDestroy(s)", 0, PerLookupServiceNested2.destroys);
    assertEquals("PreDestroy(s)", 0, PerLookupServiceNested3.destroys);

    i.release();
    
    assertEquals("PreDestroy(s) after 1 releases (perLookup scoped should be released)", 1, PerLookupService.destroys);
    if (InjectInjectionResolver.MANAGED_ENABLED) {
      assertEquals("PreDestroy(s) after 1 releases (perLookup scoped should be released)", 1, PerLookupServiceNested1.destroys);
      assertEquals("PreDestroy(s) after 1 releases (perLookup scoped should be released)", 1, PerLookupServiceNested2.destroys);
      assertEquals("PreDestroy(s) after 1 releases (perLookup scoped should be released)", 1, PerLookupServiceNested3.destroys);
    } else {  // old behavior
      assertEquals("PreDestroy(s) after 1 releases", 0, PerLookupServiceNested1.destroys);
      assertEquals("PreDestroy(s) after 1 releases", 0, PerLookupServiceNested2.destroys);
      assertEquals("PreDestroy(s) after 1 releases", 0, PerLookupServiceNested3.destroys);
    }
  }

  /**
   * Demonstrates why habitat#getComponent() is good to use, no lifecycle!
   */
  // TODO: We should probably deprecate all habitat methods that return services directly!
  @Test
  public void getComponentFromHabitat() {
    PerLookupService plService = h.getComponent(PerLookupService.class);
    assertNotNull(plService);
    assertEquals("PostConstruct(s)", 1, PerLookupService.constructs);
    assertEquals("PostConstruct(s)", 1, PerLookupServiceNested1.constructs);
    assertEquals("PostConstruct(s)", 1, PerLookupServiceNested2.constructs);
    assertEquals("PostConstruct(s)", 1, PerLookupServiceNested3.constructs);
    assertEquals("PreDestroy(s)", 0, PerLookupService.destroys);
    assertEquals("PreDestroy(s)", 0, PerLookupServiceNested1.destroys);
    assertEquals("PreDestroy(s)", 0, PerLookupServiceNested2.destroys);
    assertEquals("PreDestroy(s)", 0, PerLookupServiceNested3.destroys);
  }
  
}
