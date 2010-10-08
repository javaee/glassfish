package com.sun.hk2.jsr330.test;

import static org.junit.Assert.*;

import org.atinject.tck.auto.Tire;
import org.atinject.tck.auto.accessories.SpareTire;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.jvnet.hk2.junit.Hk2Runner;

import com.sun.hk2.jsr330.test.components.NeedForTire;

/**
 * Other sanity tests, sub portions of the Tck test.
 * 
 * @author Jeff Trent
 */
@RunWith(Hk2Runner.class)
public class TireTest {

  NeedForTire needForTire;
  
  @javax.inject.Inject
  @javax.inject.Named("spare")
  Tire spareTire;

  @javax.inject.Inject
  void setIt(NeedForTire needForTire) {
    this.needForTire = needForTire;
  }
  
  @Test
  public void tire() {
    assertNotNull(needForTire);
    assertNotNull(needForTire.aTire);
  }
  
  /**
   * @Named("spare")  Tire is implemented by SpareTire. 
   */
  @Test
  public void namedTire() {
    assertTrue("not a spareTire: " + spareTire, spareTire instanceof SpareTire);
  }
  
}
