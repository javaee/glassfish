package com.sun.enterprise.tools.classmodel;

import static org.junit.Assert.*;

import org.junit.Test;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.component.classmodel.ClassPath;

/**
 * Tests for CodeSourceFilter
 *
 * @author Jeff Trent
 *
 */
public class CodeSourceFilterTest {
  
  @Test
  public void sanityTest() {
    CodeSourceFilter filter = new CodeSourceFilter(ClassPath.create(null, true));
    assertTrue(Habitat.class.getCanonicalName(), filter.matches(Habitat.class.getCanonicalName()));
    assertFalse(filter.matches("bogus"));
    assertFalse(filter.matches(null));
    assertTrue(filter.matches(getClass().getName()));
  }
  
}
