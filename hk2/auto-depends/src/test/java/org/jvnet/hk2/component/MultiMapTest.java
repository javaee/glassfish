package org.jvnet.hk2.component;

import java.util.List;

import static org.junit.Assert.*;
import org.junit.Ignore;
import org.junit.Test;

/**
 * MultiMap Tests.
 * 
 * @author Jeff Trent
 */
public class MultiMapTest {
  MultiMap<String, String> mm = new MultiMap<String, String>(false);
  MultiMap<String, String> mmc = new MultiMap<String, String>(true);

  @Ignore
  public void testGet_returnsReadOnlyMap() throws Exception {
    runTestGet_returnsReadOnlyMap(mm);
    runTestGet_returnsReadOnlyMap(mmc);
  }

  protected void runTestGet_returnsReadOnlyMap(MultiMap<String, String> mm) {
    List<String> list = mm.get("key");
    try {
      fail("add expected to fail: " + list.add("x"));
    } catch (Exception e) {
      // expected
    }

    mm.add("key", "val");
    list = mm.get("key");
    assertNotNull(list);
    assertEquals(1, list.size());
    try {
      fail("remove expected to fail: " + list.remove(0));
    } catch (Exception e) {
      // expected
    }
    try {
      fail("add expected to fail: " + list.add("x"));
    } catch (Exception e) {
      // expected
    }
  }

  @Test
  public void testRemove_KV() throws Exception {
    runTestRemove_KV(mm);
    runTestRemove_KV(mmc);
  }

  protected void runTestRemove_KV(MultiMap<String, String> mm) {
    String val = new String("val");
    assertFalse(mm.remove("key", val));
    mm.add("key", "val");
    mm.add("key", "val2");
    assertTrue(mm.remove("key", val));
    assertFalse(mm.remove("key", val));
    assertTrue(mm.remove("key", "val2"));
  }
  
  @Test
  public void testContainsKV() {
    runContainsKV(mm);
    runContainsKV(mmc);
  }

  protected void runContainsKV(MultiMap<String, String> mm) {
    assertFalse(mm.contains("a", "b"));
    mm.add("a", "b");
    assertFalse(mm.contains("a", null));
    assertFalse(mm.contains("a", "a"));
    assertTrue(mm.contains("a", "b"));
    assertFalse(mm.contains("b", "a"));
  }

}
