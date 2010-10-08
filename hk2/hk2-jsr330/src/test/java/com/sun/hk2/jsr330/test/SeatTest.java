package com.sun.hk2.jsr330.test;

import static org.junit.Assert.*;

import org.atinject.tck.auto.Drivers;
import org.atinject.tck.auto.DriversSeat;
import org.atinject.tck.auto.Seat;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.jvnet.hk2.junit.Hk2Runner;

/**
 * Other sanity tests, sub portions of the Tck test.
 * 
 * @author Jeff Trent
 */
@RunWith(Hk2Runner.class)
public class SeatTest {

  @javax.inject.Inject @Drivers Seat driversSeat;
  
  @javax.inject.Inject Seat plainSeat;
  
  
  /**
# @Drivers  Seat is implemented by DriversSeat.
# Seat is implemented by Seat itself, and Tire by Tire itself (not subclasses). 
   */
  @Test
  public void seats() {
    assertNotNull(driversSeat);
    assertNotNull(plainSeat);
    assertTrue("expected to be a qualified DriversSeat: " + driversSeat, driversSeat instanceof DriversSeat);
    assertFalse("expected to be just a Seat: " + plainSeat, plainSeat instanceof DriversSeat);
  }
}
