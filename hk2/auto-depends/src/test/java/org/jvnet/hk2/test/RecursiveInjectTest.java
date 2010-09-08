package org.jvnet.hk2.test;

import static org.junit.Assert.*;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.component.ComponentException;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.component.Inhabitant;
import org.jvnet.hk2.junit.Hk2Runner;
import org.jvnet.hk2.junit.Hk2RunnerOptions;
import org.jvnet.hk2.test.contracts.TestingInfoService;
import org.jvnet.hk2.test.impl.RecursiveA2B2OptA;
import org.jvnet.hk2.test.impl.RecursiveB2OptA;

/**
 * Verifies the behavior of recursive injection.
 * 
 * Basically, all forms of recursive injection are not handled via direct @Inject.
 * 
 * @author Jeff Trent
 */
@RunWith(Hk2Runner.class)
@Hk2RunnerOptions(reinitializePerTest=true)
// TODO: cover Holder<> behavior with recursion
@Ignore("any and all recursion is not supported")
public class RecursiveInjectTest {

  @Inject
  Habitat h;
  
  
  /**
   * No optional dependencies; demand starting with A
   */
  @Test
  public void a2B() {
    Inhabitant<?> a = h.getInhabitant(TestingInfoService.class, "RecursiveA2B2A");
    assertNotNull(a);
    Inhabitant<?> b = h.getInhabitant(TestingInfoService.class, "RecursiveB2A");
    assertNotNull(b);

    assertFalse(a.isInstantiated());
    assertFalse(b.isInstantiated());

    try {
      fail("Expected exception, but got: " + a.get());
    } catch (ComponentException e) {
      // expected
      e.printStackTrace();
    }
    
    assertFalse(a.isInstantiated());
    assertFalse(b.isInstantiated());
  }
  
  /**
   * No optional dependencies; demand starting with B
   */
  @Test
  public void b2A() {
    Inhabitant<?> a = h.getInhabitant(TestingInfoService.class, "RecursiveA2B2A");
    assertNotNull(a);
    Inhabitant<?> b = h.getInhabitant(TestingInfoService.class, "RecursiveB2A");
    assertNotNull(b);

    assertFalse(a.isInstantiated());
    assertFalse(b.isInstantiated());

    try {
      fail("Expected exception, but got: " + b.get());
    } catch (ComponentException e) {
      // expected
      e.printStackTrace();
    }
    
    assertFalse(a.isInstantiated());
    assertFalse(b.isInstantiated());
  }
  
  /**
   * Optional dependencies from B to A; demand starting with A
   */
  @Test
  public void OptA2B() {
    Inhabitant<?> a = h.getInhabitant(TestingInfoService.class, "RecursiveA2B2OptA");
    assertNotNull(a);
    Inhabitant<?> b = h.getInhabitant(TestingInfoService.class, "RecursiveB2OptA");
    assertNotNull(b);

    assertFalse(a.isInstantiated());
    assertFalse(b.isInstantiated());

    assertNotNull(a.get());
    assertNotNull("A->B should be present", ((RecursiveA2B2OptA)a.get()).getB());
    assertNull("A->B->A should be null", ((RecursiveB2OptA)((RecursiveA2B2OptA)a.get()).getB()).getA());
    
    assertTrue(a.isInstantiated());
    assertTrue(b.isInstantiated());
  }
  
  /**
   * Optional dependencies from B to A; demand starting with B
   */
  @Test
  public void b2OptA() {
    Inhabitant<?> a = h.getInhabitant(TestingInfoService.class, "RecursiveA2B2OptA");
    assertNotNull(a);
    Inhabitant<?> b = h.getInhabitant(TestingInfoService.class, "RecursiveB2OptA");
    assertNotNull(b);

    assertFalse(a.isInstantiated());
    assertFalse(b.isInstantiated());

    assertNotNull(b.get());
    assertNull("B->A should be null", ((RecursiveB2OptA)b.get()).getA());
    
    assertFalse(a.isInstantiated());
    assertTrue(b.isInstantiated());
    
    // now, this is where it gets strange ... if we create new demand for A, A can now be created!
    assertNotNull(a.get());
    
    assertNull("B->A should still be null since it's not dynamic", ((RecursiveB2OptA)b.get()).getA());
    
    assertNotNull("A->B should be present", ((RecursiveA2B2OptA)a.get()).getB());
    assertNull("A->B->A should be null", ((RecursiveB2OptA)((RecursiveA2B2OptA)a.get()).getB()).getA());

    assertTrue(a.isInstantiated());
    assertTrue(b.isInstantiated());
  }


}
