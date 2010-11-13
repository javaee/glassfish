package com.sun.hk2.component;

import static org.junit.Assert.*;

import java.util.concurrent.Callable;
import java.util.logging.Logger;

import org.junit.Test;

public class SoftCacheTest {

  int count;
  
  @Test
  public void testIt() {
    SoftCache<String, Integer> cache = new SoftCache<String, Integer>();

    Callable<Integer> populator = new Callable<Integer>() {
      @Override
      public Integer call() throws Exception {
        return ++count;
      }
    };

    assertEquals(1, cache.get("x", populator).intValue());
    assertEquals(1, cache.get("x", populator).intValue());
    assertEquals(2, cache.get("y", populator).intValue());
    assertEquals(2, cache.get("y", populator).intValue());
    count++;
    assertEquals(2, cache.get("y", populator).intValue());
    int val = cache.get("z", populator).intValue();
    assertEquals(4, val);
    
    // hope for the best
    System.gc();
    System.gc();
    
    assertTrue(5 == val || 4 == val);
    if (4 == val) {
      Logger.getAnonymousLogger().fine("gc let us down");
    } else {
      Logger.getAnonymousLogger().finest("gc did not let us down");
    }
  }
}
