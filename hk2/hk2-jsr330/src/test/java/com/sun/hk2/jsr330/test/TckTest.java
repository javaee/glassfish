package com.sun.hk2.jsr330.test;

import static org.junit.Assert.*;

import org.atinject.tck.Tck;
import org.atinject.tck.auto.Car;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.jvnet.hk2.junit.Hk2Runner;

/**
 * Bootstrap the Tck Test.
 * 
 * @author Jeff Trent
 */
@RunWith(Hk2Runner.class)
public class TckTest {

  @javax.inject.Inject
  Car car;
  
  @Test
  public void mainTckTest() {
    assertNotNull(car);
    Tck.testsFor(car, /*hk2 supports static*/true, /*hk2 supports private*/true);
  }
  
}
