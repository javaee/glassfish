/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2014 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package org.glassfish.hk2.testing.collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

/**
 * This class should be used to test the basic
 * Map functionality of those classes implementing
 * the Map interface.
 * <p>
 * Maps are generally constructed with either a zero
 * argument constructor or a constructor that
 * gives an initial map.  Subclasses of this class
 * must implement the methods to create the Maps
 * using these two common concrete schemes.
 * <p>
 * Other protected methods in this class control
 * whether or not the Map implementation implements
 * some of the optional features of the Map interface.
 */
@SuppressWarnings({"unchecked","rawtypes"})
public abstract class AbstractMapTest {
  protected final static String NULL_KEY = "null";
  protected final static String NULL_VALUE = "null";
  
  protected final static String ONE_KEY = "One";
  protected final static String ONE_VALUE = "1";
  
  protected final static String TWO_KEY = "Two";
  protected final static String TWO_VALUE = "2";
  
  protected final static String THREE_KEY = "Three";
  protected final static String THREE_VALUE = "3";
  
  protected final static String FOUR_KEY = "Four";
  protected final static String FOUR_VALUE = "4";
  
  private TestCollectionElement globalOneKey;
  private TestCollectionElement globalTwoKey;
  private TestCollectionElement globalThreeKey;
  
  private TestCollectionElement globalOneValue;
  private TestCollectionElement globalTwoValue;
  private TestCollectionElement globalThreeValue;
 
  /**
   * Subclasses should override this method if their Map
   * implementation does not support a null argument
   * constructor.  All tests having to use the null argument
   * constructor will pass silently
   * 
   * @return true if a null argument constructor is supported
   */
  protected boolean isNullArgumentConstructorSupported() {
    return true;
  }
  
  /**
   * Subclasses should override this method if their Map
   * implementation does not support a Map argument
   * constructor.  All tests having to use the Map argument
   * constructor will pass silently
   * 
   * @return true if a null argument constructor is supported
   */
  protected boolean isMapArgumentConstructorSupported() {
    return true;
  }
  
  /**
   * Subclasses should override this method if their Map
   * implementation delegates to the passed in Map.
   * Normally a collection makes a copy of the input, but
   * there are many map implementations that instead delegate to
   * the passed in map
   * 
   * @return true if the Map implementation delegates to the
   * Map passed into the constructor
   */
  protected boolean doesMapDelegateFromOriginalMap() {
    return false;
  }
  
  /**
   * Subclasses should override this method if their Map
   * implementation does not support null as an key in
   * the Map.
   * 
   * @return true if the Map allows null keys to be added
   * to the map
   */
  protected boolean doesMapAllowNullKeys() {
    return true;
  }
  
  /**
   * Subclasses should override this method if their Map
   * implementation does not support null as an value in
   * the Map.
   * 
   * @return true if the Map allows null values to be added
   * to the map
   */
  protected boolean doesMapAllowNullValues() {
    return true;
  }
  
  /**
   * Subclasses should override this method if their Map
   * does not support the optional remove method.
   * 
   * @return true if the Map supports the optional
   * remove operation, and false otherwise
   */
  protected boolean doesMapSupportRemove() {
    return true;
  }
  
  /**
   * Subclasses must override this method in order to construct
   * an empty Map with the null argument constructor.  If
   * isNullArgumentConstructorSupported returns false then this
   * method will never be called
   * 
   * @return An empty Map
   */
  
  protected abstract Map<?,?> createMap();
  
  /**
   * Subclasses must override this method in order to construct
   * a Map with the elements found in the input Map.
   * If isMapArgumentConstructorSupported returns false then
   * this method will never be called
   * 
   * @param input A possibly empty map of items
   * to seed the Map with
   * @return An Map that has all of the elements of the input
   * collection
   */
  protected abstract Map<?,?> createMap(Map<?,?> input);
  
  /**
   * This returns an implementation of TestCollectionElement that
   * will be put into the Collection implementation.  Note that
   * subclasses should override this if they are in a situation
   * where the collection must only contain a particular type of
   * Object.  In that case they may need to subclass the type or
   * have an implementation of the internal type that implements both
   * the necessary type and TestCollectionElement.
   * 
   * @param data The string that must be returned via the listElementReturn
   * method
   * @return A new instance of the ListElement type
   */
  protected TestCollectionElement getElement(String data) {
    return new DefaultTestCollectionElement(data);
  }
  
  private Map<?,?> createEmptyMap() {
    if (isNullArgumentConstructorSupported()) {
      return createMap();
    }
    if (isMapArgumentConstructorSupported()) {
      return createMap(new HashMap());
    }
    
    Assert.fail("Neither null nor Collection constructor is supported by the List implementation");
    return null;
  }
  
  /**
   * Tests that a list constructed from the null constructor has
   * size zero
   */
  @Test
  public void testMapFromNullConstructorHasZeroSize() {
    if (!isNullArgumentConstructorSupported()) {
      return;
    }
    
    Map myMap = createMap();
    assertEquals(0, myMap.size());
    assertTrue(myMap.isEmpty());
  }
  
  /**
   * Tests that a list constructed from the empty collection
   * constructor has size zero
   */
  @Test
  public void testMapFromMapConstructorHasZeroSize() {
    if (!isMapArgumentConstructorSupported()) {
      return;
    }
    
    Map myMap = createMap(Collections.emptyMap());
    assertEquals(0, myMap.size());
    assertTrue(myMap.isEmpty());
  }
  
  /**
   * Tests that a list constructed from the empty collection
   * constructor has size zero
   */
  @Test(expected=NullPointerException.class)
  public void testMapConstructedWithNullMapThrows() {
    if (!isMapArgumentConstructorSupported()) {
      throw new NullPointerException();
    }
    
    createMap(null);
  }
  
  /**
   * Tests that a single element can be added to the list
   */
  @Test
  public void testCanAddToMap() {
    Map map = createEmptyMap();

    TestCollectionElement oneKey = getElement(ONE_KEY);
    TestCollectionElement oneValue = getElement(ONE_VALUE);
    map.put(oneKey, oneValue);
    
    assertEquals(1, map.size());
    assertFalse(map.isEmpty());
    
    assertTrue(map.containsKey(oneKey));
    assertTrue(map.containsValue(oneValue));
    assertEquals(oneValue, map.get(oneKey));
  }
  
  /**
   * Tests that modifying the elements in the original collection
   * do not affect the elements in the newly created collection
   */
  @Test
  public void testModifyingOriginalCollectionDoesNotAffectNewCollection() {
    if (!isMapArgumentConstructorSupported() ||
        doesMapDelegateFromOriginalMap()) {
      return;
    }
    
    HashMap<TestCollectionElement, TestCollectionElement> originalList =
      new HashMap<TestCollectionElement, TestCollectionElement>();
    originalList.put(getElement(ONE_KEY), getElement(ONE_VALUE));
    originalList.put(getElement(TWO_KEY), getElement(TWO_VALUE));
    
    Map mapUnderTest = createMap(originalList);
    
    originalList.put(getElement(THREE_KEY), getElement(THREE_VALUE));
    
    // Make sure the original list did not get modified
    assertEquals(2, mapUnderTest.size());
    
    originalList.clear();
   
    // Check it a second way
    assertEquals(2, mapUnderTest.size());
  }
  
  /**
   * Tests that contains works properly
   */
  @Test
  public void testContainsAfterPut() {
    Map map = createEmptyMap();
    
    TestCollectionElement oneKey = getElement(ONE_KEY);
    TestCollectionElement twoKey = getElement(TWO_KEY);
    TestCollectionElement threeKey = getElement(THREE_KEY);
    TestCollectionElement fourKey = getElement(FOUR_KEY);  // not added
    
    TestCollectionElement oneValue = getElement(ONE_VALUE);
    TestCollectionElement twoValue = getElement(TWO_VALUE);
    TestCollectionElement threeValue = getElement(THREE_VALUE);
    TestCollectionElement fourValue = getElement(FOUR_VALUE);  // not added
    
    map.put(oneKey, oneValue);
    map.put(twoKey, twoValue);
    map.put(threeKey, threeValue);
    
    assertTrue(map.containsKey(threeKey));
    assertTrue(map.containsKey(twoKey));
    assertTrue(map.containsKey(oneKey));
    assertFalse(map.containsKey(fourKey));
    
    assertTrue(map.containsValue(threeValue));
    assertTrue(map.containsValue(twoValue));
    assertTrue(map.containsValue(oneValue));
    assertFalse(map.containsValue(fourValue));
    
    if (doesMapAllowNullKeys()) {
      assertFalse(map.containsKey(null));
    }
    if (doesMapAllowNullValues()) {
      assertFalse(map.containsValue(null));
    }
  }
  
  /**
   * Tests that the map functions properly with
   * a null key added
   */
  @Test
  public void testCanMapHaveNullKeys() {
    if (!doesMapAllowNullKeys()) {
      return;
    }
    
    Map map = createEmptyMap();
    
    TestCollectionElement oneValue = getElement(ONE_VALUE);
    map.put(null, oneValue);
    
    assertEquals(1, map.size());
    assertTrue(map.containsKey(null));
    assertTrue(map.containsValue(oneValue));
    assertEquals(oneValue, map.get(null));
  }
  
  /**
   * Tests that the map functions properly with
   * a null value added
   */
  @Test
  public void testCanMapHaveNullValues() {
    if (!doesMapAllowNullValues()) {
      return;
    }
    
    Map map = createEmptyMap();
    
    TestCollectionElement oneKey = getElement(ONE_KEY);
    map.put(oneKey, null);
    
    assertEquals(1, map.size());
    assertTrue(map.containsKey(oneKey));
    assertTrue(map.containsValue(null));
    assertNull(map.get(oneKey));
  }
  
  private TestCollectionElement getSafeNullKey() {
    if (doesMapAllowNullKeys()) return null;
    
    return getElement(NULL_KEY);
  }
  
  private TestCollectionElement getSafeNullValue() {
    if (doesMapAllowNullValues()) return null;
    
    return getElement(NULL_VALUE);
  }
  
  /**
   * Tests that maps can have null keys and values
   * interspersed with non-null entries
   */
  @Test
  public void testCanCollectionHaveNullAndNonNullEntries() {
    Map map = createEmptyMap();
    
    TestCollectionElement oneKey = getElement(ONE_KEY);
    TestCollectionElement twoKey = getElement(TWO_KEY);
    TestCollectionElement oneValue = getElement(ONE_VALUE);
    TestCollectionElement twoValue = getElement(TWO_VALUE);
    TestCollectionElement nullKey = getSafeNullKey();
    TestCollectionElement threeKey = getElement(THREE_KEY);
    TestCollectionElement threeValue = getElement(THREE_VALUE);
    TestCollectionElement nullValue = getSafeNullValue();
    TestCollectionElement fourKey = getElement(FOUR_KEY);
    TestCollectionElement fourValue = getElement(FOUR_VALUE);
    
    map.put(nullKey, twoValue);  // Careful, this mixes it up a bit
    map.put(oneKey, oneValue);
    map.put(twoKey, nullValue);
    map.put(threeKey, threeValue);
    map.put(fourKey, nullValue);  // put null in as value twice
    
    // Now some checking, first check size
    assertEquals(5, map.size());
    
    // Also check containsKey
    assertTrue(map.containsKey(nullKey));
    assertTrue(map.containsKey(oneKey));
    assertTrue(map.containsKey(twoKey));
    assertTrue(map.containsKey(threeKey));
    assertTrue(map.containsKey(fourKey));
    
    // Also check containsValue
    assertTrue(map.containsValue(nullValue));
    assertTrue(map.containsValue(oneValue));
    assertTrue(map.containsValue(twoValue));
    assertTrue(map.containsValue(threeValue));
    assertFalse(map.containsValue(fourValue));
    
    // And check the gets
    assertEquals(twoValue, map.get(nullKey));
    assertEquals(oneValue, map.get(oneKey));
    assertEquals(nullValue, map.get(twoKey));
    assertEquals(threeValue, map.get(threeKey));
    assertEquals(nullValue, map.get(fourKey));
  }
  
  /**
   * This test ensures that remove works properly in an empty map
   */
  @Test
  public void testRemoveFromEmptyMap() {
    if (!doesMapSupportRemove()) return;
    
    Map map = createEmptyMap();
    
    TestCollectionElement oneKey = getElement(ONE_KEY);
    
    assertNull(map.remove(oneKey));
    assertEquals(0, map.size());
    assertTrue(map.isEmpty());
  }
  
  /**
   * This test ensures that remove returns the proper value
   */
  @Test
  public void testRemoveFromOneElementMap() {
    if (!doesMapSupportRemove()) return;
    
    Map map = createEmptyMap();
    
    TestCollectionElement oneKey = getElement(ONE_KEY);
    TestCollectionElement oneValue = getElement(ONE_VALUE);
    map.put(oneKey, oneValue);
    
    assertEquals(oneValue, map.remove(oneKey));
    assertEquals(0, map.size());
    assertTrue(map.isEmpty());
  }
  
  /**
   * This test ensures that remove can remove the
   * first item added to the collection
   */
  @Test
  public void testRemoveFirstThingFromMultiElementMap() {
    if (!doesMapSupportRemove()) return;
    
    Map map = createEmptyMap();
    
    TestCollectionElement oneKey = getElement(ONE_KEY);
    TestCollectionElement twoKey = getElement(TWO_KEY);
    TestCollectionElement threeKey = getElement(THREE_KEY);
    TestCollectionElement oneValue = getElement(ONE_VALUE);
    TestCollectionElement twoValue = getElement(TWO_VALUE);
    TestCollectionElement threeValue = getElement(THREE_VALUE);
    
    map.put(oneKey, oneValue);
    map.put(twoKey, twoValue);
    map.put(threeKey, threeValue);
    
    assertEquals(oneValue, map.remove(oneKey));
    
    assertEquals(2, map.size());
    assertFalse(map.isEmpty());
    assertTrue(map.containsKey(twoKey));
    assertTrue(map.containsValue(threeValue));
    
    assertFalse(map.containsKey(oneKey));
    assertFalse(map.containsValue(oneValue));
  }
  
  /**
   * This test ensures that remove can remove the
   * first item added to the map
   */
  @Test
  public void testRemoveLastThingFromMultiElementMap() {
    if (!doesMapSupportRemove()) return;
    
    Map map = createEmptyMap();
    
    TestCollectionElement oneKey = getElement(ONE_KEY);
    TestCollectionElement twoKey = getElement(TWO_KEY);
    TestCollectionElement threeKey = getElement(THREE_KEY);
    TestCollectionElement oneValue = getElement(ONE_VALUE);
    TestCollectionElement twoValue = getElement(TWO_VALUE);
    TestCollectionElement threeValue = getElement(THREE_VALUE);
    
    map.put(oneKey, oneValue);
    map.put(twoKey, twoValue);
    map.put(threeKey, threeValue);
    
    assertEquals(threeValue, map.remove(threeKey));
    
    assertEquals(2, map.size());
    assertFalse(map.isEmpty());
    assertTrue(map.containsKey(twoKey));
    assertTrue(map.containsValue(oneValue));
    
    assertFalse(map.containsKey(threeKey));
    assertFalse(map.containsValue(threeValue));
  }
  
  /**
   * This test ensures that remove can remove the
   * first item added to the map
   */
  @Test
  public void testRemoveMiddleThingFromMultiElementMap() {
    if (!doesMapSupportRemove()) return;
    
    Map map = createEmptyMap();
    
    TestCollectionElement oneKey = getElement(ONE_KEY);
    TestCollectionElement twoKey = getElement(TWO_KEY);
    TestCollectionElement threeKey = getElement(THREE_KEY);
    TestCollectionElement oneValue = getElement(ONE_VALUE);
    TestCollectionElement twoValue = getElement(TWO_VALUE);
    TestCollectionElement threeValue = getElement(THREE_VALUE);
    
    map.put(oneKey, oneValue);
    map.put(twoKey, twoValue);
    map.put(threeKey, threeValue);
    
    assertEquals(twoValue, map.remove(twoKey));
    
    assertEquals(2, map.size());
    assertFalse(map.isEmpty());
    assertTrue(map.containsKey(threeKey));
    assertTrue(map.containsValue(oneValue));
    
    assertFalse(map.containsKey(twoKey));
    assertFalse(map.containsValue(twoValue));
  }
  
  private void addGlobalsToMap(Map aMap) {
    globalOneKey = getElement(ONE_KEY);
    globalTwoKey = getElement(TWO_KEY);
    globalThreeKey = getElement(THREE_KEY);
    
    globalOneValue = getElement(ONE_VALUE);
    globalTwoValue = getElement(TWO_VALUE);
    globalThreeValue = getElement(THREE_VALUE);
    
    aMap.put(globalOneKey, globalOneValue);
    aMap.put(globalTwoKey, globalTwoValue);
    aMap.put(globalThreeKey, globalThreeValue);
  }
  
  private Map<TestCollectionElement, TestCollectionElement> createMapWithOneTwoThree() {
    HashMap<TestCollectionElement, TestCollectionElement> retVal =
      new HashMap<TestCollectionElement, TestCollectionElement>();
    
    addGlobalsToMap(retVal);
    
    return retVal;
  }
  
  /**
   * This test ensures that putAll works with
   * an empty map and an empty adding map
   */
  @Test
  public void testPutAllEmptyOriginalEmptyAdding() {
    
    Map originalMap = createEmptyMap();
    Map putAllMap = createEmptyMap();
    
    originalMap.putAll(putAllMap);
    
    assertTrue(originalMap.isEmpty());
  }
  
  /**
   * This test ensures that putAll works with
   * an empty map and an non-empty adding map
   */
  @Test
  public void testPutAllEmptyOriginalNonEmptyAdding() {
    
    Map originalMap = createEmptyMap();
    Map putAllMap = createMapWithOneTwoThree();
    
    originalMap.putAll(putAllMap);
    
    assertEquals(3, originalMap.size());
    
    assertTrue(originalMap.containsKey(globalOneKey));
    assertTrue(originalMap.containsKey(globalTwoKey));
    assertTrue(originalMap.containsKey(globalThreeKey));
    
    assertTrue(originalMap.containsValue(globalOneValue));
    assertTrue(originalMap.containsValue(globalTwoValue));
    assertTrue(originalMap.containsValue(globalThreeValue));
    
    assertEquals(globalOneValue, originalMap.get(globalOneKey));
    assertEquals(globalTwoValue, originalMap.get(globalTwoKey));
    assertEquals(globalThreeValue, originalMap.get(globalThreeKey));
  }
  
  /**
   * This test ensures that putAll works with
   * an non-empty map and an empty adding map
   */
  @Test
  public void testPutAllNonEmptyOriginalEmptyAdding() {
    
    Map originalMap = createEmptyMap();
    Map putAllMap = createEmptyMap();
    
    addGlobalsToMap(originalMap);
    
    originalMap.putAll(putAllMap);
    
    assertEquals(3, originalMap.size());
    
    assertTrue(originalMap.containsKey(globalOneKey));
    assertTrue(originalMap.containsKey(globalTwoKey));
    assertTrue(originalMap.containsKey(globalThreeKey));
    
    assertTrue(originalMap.containsValue(globalOneValue));
    assertTrue(originalMap.containsValue(globalTwoValue));
    assertTrue(originalMap.containsValue(globalThreeValue));
    
    assertEquals(globalOneValue, originalMap.get(globalOneKey));
    assertEquals(globalTwoValue, originalMap.get(globalTwoKey));
    assertEquals(globalThreeValue, originalMap.get(globalThreeKey));
  }
  
  /**
   * This test ensures that putAll works with
   * an non-empty map and an non-empty adding map
   */
  @Test
  public void testPutAllNonEmptyOriginalNonEmptyAdding() {
    
    Map originalMap = createEmptyMap();
    Map putAllMap = createMapWithOneTwoThree();
    
    TestCollectionElement fourKey = getElement(FOUR_KEY);
    TestCollectionElement fourValue = getElement(FOUR_VALUE);
    
    originalMap.put(fourKey, fourValue);
    
    originalMap.putAll(putAllMap);
    
    assertEquals(4, originalMap.size());
    
    assertTrue(originalMap.containsKey(globalOneKey));
    assertTrue(originalMap.containsKey(globalTwoKey));
    assertTrue(originalMap.containsKey(globalThreeKey));
    assertTrue(originalMap.containsKey(fourKey));
    
    assertTrue(originalMap.containsValue(globalOneValue));
    assertTrue(originalMap.containsValue(globalTwoValue));
    assertTrue(originalMap.containsValue(globalThreeValue));
    assertTrue(originalMap.containsValue(fourValue));
    
    assertEquals(globalOneValue, originalMap.get(globalOneKey));
    assertEquals(globalTwoValue, originalMap.get(globalTwoKey));
    assertEquals(globalThreeValue, originalMap.get(globalThreeKey));
    assertEquals(fourValue, originalMap.get(fourKey));
  }
  
  /**
   * This test ensures that clear works on
   * an empty map
   */
  @Test
  public void testClearOnEmptyMap() {
    if (!doesMapSupportRemove()) return;
    
    Map originalMap = createEmptyMap();
    originalMap.clear();
    
    assertTrue(originalMap.isEmpty());
  }
  
  /**
   * This test ensures that putAll works with
   * an non-empty map and an non-empty adding map
   */
  @Test
  public void testClearOnNonEmptyMap() {
    if (!doesMapSupportRemove()) return;
    
    Map originalMap = createEmptyMap();
    
    addGlobalsToMap(originalMap);
    
    originalMap.clear();
    
    assertTrue(originalMap.isEmpty());
  }
  
  /**
   * This test ensures that keySet works with
   * an empty map
   */
  @Test
  public void testKeySetOnEmptyMap() {
    Map originalMap = createEmptyMap();
    
    Set set = originalMap.keySet();
    
    assertTrue(set.isEmpty());
  }
  
  /**
   * This test ensures that keySet works with
   * an non-empty map
   */
  @Test
  public void testKeySetOnNonEmptyMap() {
    Map originalMap = createEmptyMap();
    addGlobalsToMap(originalMap);
    
    Set set = originalMap.keySet();
    
    assertEquals(3, set.size());
    
    assertTrue(set.contains(globalOneKey));
    assertTrue(set.contains(globalTwoKey));
    assertTrue(set.contains(globalThreeKey));
  }
  
  /**
   * This test ensures that values works with
   * an empty map
   */
  @Test
  public void testValuesOnEmptyMap() {
    Map originalMap = createEmptyMap();
    
    Collection collection = originalMap.values();
    
    assertTrue(collection.isEmpty());
  }
  
  /**
   * This test ensures that values works with
   * an non-empty map
   */
  @Test
  public void testValuesOnNonEmptyMap() {
    Map originalMap = createEmptyMap();
    addGlobalsToMap(originalMap);
    
    Collection values = originalMap.values();
    
    assertEquals(3, values.size());
    
    assertTrue(values.contains(globalOneValue));
    assertTrue(values.contains(globalTwoValue));
    assertTrue(values.contains(globalThreeValue));
  }
  
  /**
   * This test ensures that entrySet works with
   * an empty map
   */
  @Test
  public void testEntrySetOnEmptyMap() {
    Map originalMap = createEmptyMap();
    
    Set set = originalMap.entrySet();
    
    assertTrue(set.isEmpty());
  }
  
  /**
   * This test ensures that entrySet works with
   * an non-empty map
   */
  @Test
  public void testEntrySetOnNonEmptyMap() {
    Map originalMap = createEmptyMap();
    addGlobalsToMap(originalMap);
    
    Set entrySet = originalMap.entrySet();
    
    assertEquals(3, entrySet.size());
    
    boolean gotOne = false;
    boolean gotTwo = false;
    boolean gotThree = false;
    for (Object rawEntry : entrySet) {
      assertTrue(rawEntry instanceof Map.Entry);
      
      Map.Entry entry = (Map.Entry) rawEntry;
      
      if (entry.getKey().equals(globalOneKey)) {
        assertFalse(gotOne);
        gotOne = true;
        assertEquals(entry.getValue(), globalOneValue);
      }
      else if (entry.getKey().equals(globalTwoKey)) {
        assertFalse(gotTwo);
        gotTwo = true;
        assertEquals(entry.getValue(), globalTwoValue);
      }
      else if (entry.getKey().equals(globalThreeKey)) {
        assertFalse(gotThree);
        gotThree = true;
        assertEquals(entry.getValue(), globalThreeValue);
      }
      else {
        fail("Unknown entry " + entry.getKey() + " value " + entry.getValue());
      }
    }
    
    assertTrue(gotOne);
    assertTrue(gotTwo);
    assertTrue(gotThree);
  }
  
  private static int keyToInt(TestCollectionElement key) {
    String value = key.testCollectionValue();
    try {
      return Integer.parseInt(value);
    }
    catch (NumberFormatException nfe) {
      return -1;
    }
  }
  
  /**
   * Tries to test the resizing of the collection
   * by adding 1000 items
   */
  @Test
  public void testAddOneThousandEntries() {
    Map map = createEmptyMap();
    HashSet<TestCollectionElement> keys = new HashSet<TestCollectionElement>();
    
    for (int lcv = 0; lcv < 1000; lcv++) {
      TestCollectionElement element = getElement("" + lcv);
      map.put(element, new Integer(lcv));
      keys.add(element);
    }
    
    for (TestCollectionElement key : keys) {
      assertTrue(map.containsKey(key));
      
      int keyValue = keyToInt(key);
      Integer value = (Integer) map.get(key);
      
      assertEquals(keyValue, value.intValue());
    }
    
  }
}
