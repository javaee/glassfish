package com.sun.hk2.jsr330.test;

import static org.junit.Assert.*;

import org.atinject.tck.auto.Tire;
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
public class TireProviderTest {

  NeedForTire needForTireByProvider;
  
  @javax.inject.Inject
  Tire tire;
  
  @javax.inject.Inject
  void setIt(javax.inject.Provider<NeedForTire> needForTire) {
    this.needForTireByProvider = needForTire.get();
  }
  
  @Test
  public void tireProvider() {
    assertNotNull(needForTireByProvider);
    assertNotNull(needForTireByProvider.aTire);
    assertSame(tire, needForTireByProvider.aTire);
  }
}
