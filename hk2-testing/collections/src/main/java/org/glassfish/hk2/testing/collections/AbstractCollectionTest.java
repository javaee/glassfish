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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

import org.junit.Assert;
import org.junit.Test;

/**
 * This is a base class used for testing raw
 * collections.  Note that Collections generally
 * work quite differently from the classes that
 * extend them.  For example, a List has an ordered
 * Iterator, and hence would need to test that the
 * ordering of the Iterator is proper, whereas the
 * Collection class makes no guarantees about ordering
 * of the items in the Collection (like a Set for
 * example).
 * <p>
 * In general there are not many implementations of
 * Collection directly, but two common constructors
 * are the zero argument constructor and a constructor
 * that takes another Collection.  Subclasses of this
 * class must implement the methods that create these
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public abstract class AbstractCollectionTest {
  protected final static String ONE = "1";
  protected final static String TWO = "2";
  protected final static String THREE = "3";
  protected final static String FOUR = "4";
  protected final static String FIVE = "5";
  
  private TestCollectionElement ONE_CE;
  private TestCollectionElement TWO_CE;
  private TestCollectionElement THREE_CE;
  
  /**
   * Subclasses should override this method if their Collection
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
   * Subclasses should override this method if their Collection
   * implementation does not support a Collection argument
   * constructor.  All tests having to use the Collection argument
   * constructor will pass silently
   * 
   * @return true if a null argument constructor is supported
   */
  protected boolean isCollectionArgumentConstructorSupported() {
    return true;
  }
  
  /**
   * Subclasses should override this method if their Collection
   * implementation delegates to the passed in collection.
   * Normally a collection makes a copy of the input, but
   * there are many collection implementations that instead delegate to
   * the passed in collection
   * 
   * @return true if the Collection implementation delegates to the
   * Collection passed into the constructor
   */
  protected boolean doesCollectionDelegateFromOriginalCollection() {
    return false;
  }
  
  /**
   * Subclasses should override this method if their Collection
   * implementation does not support null as an element in
   * their Collection.
   * 
   * @return true if the Collection allows null elements to be added
   * to the list
   */
  protected boolean doesCollectionAllowNullElements() {
    return true;
  }
  
  /**
   * Subclasses should override this method if their Collection
   * implementation does not support duplicate elements.  In other words,
   * if you call "add(foo)" followed by "add(foo)" and the collection
   * will have two elements then your Collection supports duplicate
   * elements.  Otherwise (if duplicates are overwritten) then this
   * method should return false.
   * 
   * @return true if the collection allows an arbitrary number of
   * elements whose "equals" returns true to be in the collection, and
   * false if those objects will instead be overwritten
   */
  protected boolean doesCollectionSupportDuplicateElements() {
    return true;
  }
  
  /**
   * Subclasses should override this method if their Collection
   * does not support the optional remove method.
   * 
   * @return true if the Collection supports the optional
   * remove operation, and false otherwise
   */
  protected boolean doesCollectionSupportRemove() {
    return true;
  }
  
  /**
   * Subclasses should override this method if their Collection
   * does not support the optional empty method.
   * 
   * @return true if the Collection supports the optional
   * empty operation, and false otherwise
   */
  protected boolean doesCollectionSupportEmpty() {
    return true;
  }
  
  /**
   * Subclasses should override this method if the
   * Iterator returned from this collection does not
   * support the optional remove operation
   * 
   * @return true if the Collection supports the optional
   * remove operation, and false otherwise
   */
  protected boolean doesCollectionsIteratorSupportRemove() {
    return true;
  }
  
  /**
   * Subclasses should override this method if their Collection
   * does not support the optional retainAll method.
   * 
   * @return true if the Collection supports the optional
   * remove operation, and false otherwise
   */
  protected boolean doesCollectionSupportRetainAll() {
    return true;
  }
  
  /**
   * Subclasses must override this method in order to construct
   * an empty Collection with the null argument constructor.  If
   * isNullArgumentConstructorSupported returns false then this
   * method will never be called
   * 
   * @return An empty List
   */
  
  protected abstract Collection createCollection();
  
  /**
   * Subclasses must override this method in order to construct
   * a Collection with the elements found in the input Collection.
   * If isCollectionArgumentConstructorSupported returns false then
   * this method will never be called
   * 
   * @param input A possibly null and possibly empty collection of items
   * to seed the list with
   * @return An empty List that has all of the elements of the input
   * collection in Iterator ordering in the list
   */
  protected abstract Collection createCollection(Collection input);
  
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
  
  private Collection<?> createEmptyCollection() {
    if (isNullArgumentConstructorSupported()) {
      return createCollection();
    }
    if (isCollectionArgumentConstructorSupported()) {
      return createCollection(new HashSet());
    }
    
    Assert.fail("Neither null nor Collection constructor is supported by the List implementation");
    return null;
  }
  
  /**
   * Tests that a list constructed from the null constructor has
   * size zero
   */
  @Test
  public void testCollectionFromNullConstructorHasZeroSize() {
    if (!isNullArgumentConstructorSupported()) {
      return;
    }
    
    Collection myList = createCollection();
    assertEquals(0, myList.size());
    assertTrue(myList.isEmpty());
  }
  
  /**
   * Tests that a list constructed from the empty collection
   * constructor has size zero
   */
  @Test
  public void testCollectionFromCollectionConstructorHasZeroSize() {
    if (!isCollectionArgumentConstructorSupported()) {
      return;
    }
    
    Collection myList = createCollection(Collections.emptyList());
    assertEquals(0, myList.size());
    assertTrue(myList.isEmpty());
  }
  
  /**
   * Tests that a list constructed from the empty collection
   * constructor has size zero
   */
  @Test(expected=NullPointerException.class)
  public void testListColnstructedWithNullCollectionThrows() {
    if (!isCollectionArgumentConstructorSupported()) {
      throw new NullPointerException();
    }
    
    createCollection(null);
  }
  
  /**
   * Tests that a single element can be added to the list
   */
  @Test
  public void testCanAddToCollection() {
    Collection listMe = createEmptyCollection();

    TestCollectionElement one = getElement(ONE);
    listMe.add(one);
    assertEquals(1, listMe.size());
    assertFalse(listMe.isEmpty());
    
    assertTrue(listMe.contains(one));
  }
  
  /**
   * Tests that modifying the elements in the original collection
   * do not affect the elements in the newly created collection
   */
  @Test
  public void testModifyingOriginalCollectionDoesNotAffectNewCollection() {
    if (!isCollectionArgumentConstructorSupported() ||
        doesCollectionDelegateFromOriginalCollection()) {
      return;
    }
    
    List<TestCollectionElement> originalList = new LinkedList<TestCollectionElement>();
    originalList.add(getElement(ONE));
    originalList.add(getElement(TWO));
    
    Collection listUnderTest = createCollection(originalList);
    
    originalList.add(getElement(THREE));
    
    // Make sure the original list did not get modified
    assertEquals(2, listUnderTest.size());
    
    originalList.clear();
   
    // Check it a second way
    assertEquals(2, listUnderTest.size());
  }
  
  /**
   * Tests that contains works properly
   */
  @Test
  public void testContainsAfterAdd() {
    Collection list = createEmptyCollection();
    
    TestCollectionElement one = getElement(ONE);
    TestCollectionElement two = getElement(TWO);
    TestCollectionElement three = getElement(THREE);
    TestCollectionElement four = getElement(FOUR);  // not added
    
    list.add(one);
    list.add(two);
    list.add(three);
    
    assertTrue(list.contains(three));
    assertTrue(list.contains(two));
    assertTrue(list.contains(one));
    assertFalse(list.contains(four));
    
    if (doesCollectionAllowNullElements()) {
      assertFalse(list.contains(null));
    }
  }
  
  /**
   * Tests that the collection functions properly with
   * a null element added
   */
  @Test
  public void testCanCollectionHaveNullElements() {
    if (!doesCollectionAllowNullElements()) {
      return;
    }
    
    Collection list = createEmptyCollection();
    
    list.add(null);
    assertEquals(1, list.size());
    assertTrue(list.contains(null));
  }
  
  /**
   * Tests that lists can have null elements interspersed
   * with non-null elements
   */
  @Test
  public void testCanCollectionHaveNullAndNonNullElements() {
    if (!doesCollectionAllowNullElements()) {
      return;
    }
    
    Collection list = createEmptyCollection();
    
    TestCollectionElement one = getElement(ONE);
    TestCollectionElement two = getElement(TWO);
    
    list.add(null);
    list.add(one);
    list.add(null);
    list.add(two);
    
    // Now some checking, first check size
    if (doesCollectionSupportDuplicateElements()) {
      assertEquals(4, list.size());
    }
    else {
      assertEquals(3, list.size());
    }
    
    // Also check contains
    assertTrue(list.contains(null));
    assertTrue(list.contains(one));
    assertTrue(list.contains(two));
  }
  
  /**
   * Tests that the iterator from an empty list works
   */
  @Test
  public void testIteratorReturnedFromEmptyList() {
    Collection list = createEmptyCollection();
    
    Iterator iterator = list.iterator();
    assertFalse(iterator.hasNext());
  }
  
  /**
   * Tests that an iterator from a list with
   * one element returns the proper values
   */
  @Test
  public void testSingleItemIterator() {
    Collection list = createEmptyCollection();
    
    TestCollectionElement one = getElement(ONE);
    
    list.add(one);
    
    Iterator iterator = list.iterator();
    assertTrue(iterator.hasNext());
    
    TestCollectionElement returnedOne = (TestCollectionElement) iterator.next();
    assertNotNull(returnedOne);
    assertEquals(ONE, returnedOne.testCollectionValue());
    
    assertFalse(iterator.hasNext());
  }
  
  /**
   * Tests that an iterator from a Collection with
   * more than one element returns the proper values.
   * Note that in a Collection order of the elements
   * in the Iterator is NOT guaranteed
   */
  @Test
  public void testMultipleItemIterator() {
    Collection list = createEmptyCollection();
    
    TestCollectionElement one = getElement(ONE);
    TestCollectionElement two = getElement(TWO);
    
    list.add(one);
    list.add(two);
    
    Iterator iterator = list.iterator();
    assertTrue(iterator.hasNext());
    TestCollectionElement returnedFirst = (TestCollectionElement) iterator.next();
    assertTrue(iterator.hasNext());
    TestCollectionElement returnedSecond = (TestCollectionElement) iterator.next();
    assertFalse(iterator.hasNext());
    
    String returnedFirstValue = returnedFirst.testCollectionValue();
    String returnedSecondValue = returnedSecond.testCollectionValue();
    
    if (ONE.equals(returnedFirstValue)) {
      assertEquals(TWO, returnedSecondValue);
      return;
    }
    
    assertEquals(TWO, returnedFirstValue);
    assertEquals(ONE, returnedSecondValue);
  }
  
  /**
   * Tests that calling next off an empty
   * iterator throws the proper exception
   */
  @Test(expected=NoSuchElementException.class)
  public void testFallingOffEndOfEmptyIteratorThrows() {
    Collection list = createEmptyCollection();
    
    Iterator iterator = list.iterator();
    iterator.next();
  }
  
  /**
   * Tests that the iterator from a non-empty list works
   */
  @Test(expected=NoSuchElementException.class)
  public void testFallingOffEndOfNonEmptyIteratorThrows() {
    Collection list = createEmptyCollection();
    
    list.add(getElement(ONE));
    
    Iterator iterator = list.iterator();
    
    iterator.next();
    iterator.next();  // should throw
  }
  
  /**
   * Tests that removing an element from an
   * iterator with a single element works
   */
  @Test
  public void testSingleItemIteratorRemoval() {
    if (!doesCollectionsIteratorSupportRemove()) return;
    
    Collection list = createEmptyCollection();
    
    TestCollectionElement one = getElement(ONE);
    
    list.add(one);
    
    Iterator iterator = list.iterator();
    iterator.next();
    iterator.remove();
    
    assertTrue(list.isEmpty());
  }
  
  private static String findMissing(String a, String b) {
    LinkedList<String> holdMe = new LinkedList<String>();
    holdMe.add(ONE);
    holdMe.add(TWO);
    holdMe.add(THREE);
    
    LinkedList<String> removeMe = new LinkedList<String>();
    removeMe.add(a);
    removeMe.add(b);
    
    holdMe.removeAll(removeMe);
    
    return holdMe.get(0);
  }
  
  /**
   * Tests that an iterator from a Collection with
   * more than one element can remove the proper
   * values.
   * Note that in a Collection order of the elements
   * in the Iterator is NOT guaranteed
   */
  @Test
  public void testMultipleItemIteratorRemoval() {
    if (!doesCollectionsIteratorSupportRemove()) return;
    
    Collection list = createEmptyCollection();
    
    TestCollectionElement one = getElement(ONE);
    TestCollectionElement two = getElement(TWO);
    TestCollectionElement three = getElement(THREE);
    
    list.add(one);
    list.add(two);
    list.add(three);
    
    Iterator iterator = list.iterator();
    TestCollectionElement first = (TestCollectionElement)
      iterator.next();  // Not removing first one
    
    TestCollectionElement second = (TestCollectionElement)
      iterator.next();
    iterator.remove();
    assertEquals(2, list.size());
    assertTrue(list.contains(first));
    assertFalse(list.contains(second));
    
    String missing = findMissing(first.testCollectionValue(),
        second.testCollectionValue());
    
    TestCollectionElement missingElement;
    if (ONE.equals(missing)) {
      missingElement = one;
    }
    else if (TWO.equals(missing)) {
      missingElement = two;
    }
    else {
      missingElement = three;
    }
    
    assertTrue(list.contains(missingElement));
    
    TestCollectionElement third = (TestCollectionElement) iterator.next();
    iterator.remove();
    
    assertEquals(1, list.size());
    assertTrue(list.contains(first));
    assertFalse(list.contains(second));
    assertFalse(list.contains(third));
  }
  
  /**
   * Tests that an iterator from a Collection with
   * more than one element can remove the proper
   * values.
   * Note that in a Collection order of the elements
   * in the Iterator is NOT guaranteed
   */
  @Test(expected=IllegalStateException.class)
  public void testIteratorRemoveOnEmptyIteratorThrows() {
    if (!doesCollectionsIteratorSupportRemove()) {
      throw new IllegalStateException();
    }
    
    Collection collection = createEmptyCollection();
    
    Iterator iterator = collection.iterator();
    iterator.remove();
  }
  
  /**
   * Tests that removing an element from an
   * iterator with a single element works
   */
  @Test(expected=IllegalStateException.class)
  public void testDoubleIteratorRemoveThrows() {
    if (!doesCollectionsIteratorSupportRemove()) {
      throw new IllegalStateException();
    }
    
    Collection collection = createEmptyCollection();
    
    TestCollectionElement one = getElement(ONE);
    
    collection.add(one);
    
    Iterator iterator = collection.iterator();
    iterator.next();
    iterator.remove();
    iterator.remove();
  }
  
  /**
   * Tests that removing an element from an
   * iterator with a single element works
   */
  @Test(expected=UnsupportedOperationException.class)
  public void testUnsupporedRemoveIteratorThrows() {
    if (doesCollectionsIteratorSupportRemove()) {
      throw new UnsupportedOperationException();
    }
    
    Collection collection = createEmptyCollection();
    
    TestCollectionElement one = getElement(ONE);
    
    collection.add(one);
    
    Iterator iterator = collection.iterator();
    iterator.next();
    iterator.remove();
  }
  
  /**
   * Tests that toArray works on an empty collection
   */
  @Test
  public void testCollectionToArrayWithEmptyCollection() {
    Collection collection = createEmptyCollection();
    
    Object cArray[] = collection.toArray();
    assertNotNull(cArray);
    assertEquals(0, cArray.length);
  }
  
  /**
   * Tests that toArray works on an empty collection
   */
  @Test
  public void testCollectionToArrayWithCollectionOfOneItem() {
    Collection collection = createEmptyCollection();
    
    TestCollectionElement one = getElement(ONE);
    collection.add(one);
    
    Object cArray[] = collection.toArray();
    assertNotNull(cArray);
    assertEquals(1, cArray.length);
    
    assertEquals(one, cArray[0]);
  }
  
  /**
   * Tests that toArray works on an multi-valued
   * collection.  Note that order is not guaranteed
   * in a collection.
   */
  @Test
  public void testCollectionToArrayWithCollectionOfMultipleItems() {
    Collection collection = createEmptyCollection();
    
    TestCollectionElement one = getElement(ONE);
    TestCollectionElement two = getElement(TWO);
    
    collection.add(one);
    collection.add(two);
    
    Object cArray[] = collection.toArray();
    assertNotNull(cArray);
    assertEquals(2, cArray.length);
    
    String firstValue = ((TestCollectionElement) cArray[0]).testCollectionValue();
    String secondValue = ((TestCollectionElement) cArray[1]).testCollectionValue();
    
    if (ONE.equals(firstValue)) {
      assertEquals(TWO, secondValue);
      return;
    }
    
    assertEquals(TWO, firstValue);
    assertEquals(ONE, secondValue);
  }
  
  /**
   * Tests that toArray works on an empty collection
   */
  @Test
  public void testCollectionToArrayWithArgumentWithEmptyCollection() {
    Collection collection = createEmptyCollection();
    
    Object cArray[] = collection.toArray(new Object[5]);
    assertNotNull(cArray);
    assertEquals(5, cArray.length);
    assertNull(cArray[0]);
  }
  
  /**
   * Tests that toArray works on an empty collection
   */
  @Test
  public void testCollectionToArrayWithArgumentWithCollectionOfOneItem() {
    Collection collection = createEmptyCollection();
    
    TestCollectionElement one = getElement(ONE);
    collection.add(one);
    
    Object cArray[] = collection.toArray(new Object[1]);
    assertNotNull(cArray);
    assertEquals(1, cArray.length);
    
    assertEquals(one, cArray[0]);
  }
  
  /**
   * Tests that toArray works on an multi-valued
   * collection.  Note that order is not guaranteed
   * in a collection.
   */
  @Test
  public void testCollectionToArrayWithArgumentWithCollectionOfMultipleItems() {
    Collection collection = createEmptyCollection();
    
    TestCollectionElement one = getElement(ONE);
    TestCollectionElement two = getElement(TWO);
    
    collection.add(one);
    collection.add(two);
    
    Object cArray[] = collection.toArray(new Object[1]);
    assertNotNull(cArray);
    assertEquals(2, cArray.length);
    
    String firstValue = ((TestCollectionElement) cArray[0]).testCollectionValue();
    String secondValue = ((TestCollectionElement) cArray[1]).testCollectionValue();
    
    if (ONE.equals(firstValue)) {
      assertEquals(TWO, secondValue);
      return;
    }
    
    assertEquals(TWO, firstValue);
    assertEquals(ONE, secondValue);
  }
  
  /**
   * Tests that toArray works on an multi-valued
   * collection.  Note that order is not guaranteed
   * in a collection.
   */
  @Test(expected=NullPointerException.class)
  public void testCollectionToArrayWithArgumentWithNullThrows() {
    Collection collection = createEmptyCollection();
    
    collection.toArray(null);
  }
  
  /**
   * This test ensures that add returns the proper value
   */
  @Test
  public void testAddReturnsProperValueForNonDuplicateAdd() {
    Collection collection = createEmptyCollection();
    
    assertTrue(collection.add(getElement(ONE)));
  }
  
  /**
   * This test ensures that add returns the proper value
   */
  @Test
  public void testAddReturnsProperValueForDuplicateAdd() {
    Collection collection = createEmptyCollection();
    
    TestCollectionElement one = getElement(ONE);
    assertTrue(collection.add(one));
    
    if (doesCollectionSupportDuplicateElements()) {
      assertTrue(collection.add(one));
    }
    else {
      assertFalse(collection.add(one));
    }
  }
  
  /**
   * This test ensures that remove works properly in an empty list
   */
  @Test
  public void testRemoveFromEmptyList() {
    if (!doesCollectionSupportRemove()) return;
    
    Collection collection = createEmptyCollection();
    
    TestCollectionElement one = getElement(ONE);
    assertFalse(collection.remove(one));
    assertEquals(0, collection.size());
    assertTrue(collection.isEmpty());
  }
  
  /**
   * This test ensures that remove returns the proper value
   */
  @Test
  public void testRemoveFromOneElementList() {
    if (!doesCollectionSupportRemove()) return;
    
    Collection collection = createEmptyCollection();
    
    TestCollectionElement one = getElement(ONE);
    collection.add(one);
    
    assertTrue(collection.remove(one));
    assertEquals(0, collection.size());
    assertTrue(collection.isEmpty());
  }
  
  /**
   * This test ensures that remove can remove the
   * first item added to the collection
   */
  @Test
  public void testRemoveFirstThingFromMultiElementList() {
    if (!doesCollectionSupportRemove()) return;
    
    Collection collection = createEmptyCollection();
    
    TestCollectionElement one = getElement(ONE);
    TestCollectionElement two = getElement(TWO);
    TestCollectionElement three = getElement(THREE);
    
    collection.add(one);
    collection.add(two);
    collection.add(three);
    
    assertTrue(collection.remove(one));
    
    assertEquals(2, collection.size());
    assertFalse(collection.isEmpty());
    assertTrue(collection.contains(two));
    assertTrue(collection.contains(three));
  }
  
  /**
   * This test ensures that remove can remove the
   * last item added to the collection
   */
  @Test
  public void testRemoveLastThingFromMultiElementList() {
    if (!doesCollectionSupportRemove()) return;
    
    Collection collection = createEmptyCollection();
    
    TestCollectionElement one = getElement(ONE);
    TestCollectionElement two = getElement(TWO);
    TestCollectionElement three = getElement(THREE);
    
    collection.add(one);
    collection.add(two);
    collection.add(three);
    
    assertTrue(collection.remove(three));
    
    assertEquals(2, collection.size());
    assertFalse(collection.isEmpty());
    assertTrue(collection.contains(two));
    assertTrue(collection.contains(one));
  }

  /**
   * This test ensures that remove can remove a
   * first item added to the collection
   */
  @Test
  public void testRemoveMiddleThingFromMultiElementList() {
    if (!doesCollectionSupportRemove()) return;
    
    Collection collection = createEmptyCollection();
    
    TestCollectionElement one = getElement(ONE);
    TestCollectionElement two = getElement(TWO);
    TestCollectionElement three = getElement(THREE);
    
    collection.add(one);
    collection.add(two);
    collection.add(three);
    
    assertTrue(collection.remove(two));
    
    assertEquals(2, collection.size());
    assertFalse(collection.isEmpty());
    assertTrue(collection.contains(three));
    assertTrue(collection.contains(one));
  }
  
  
  
  private Collection createCollectionWithOneTwoThree() {
    Collection collection = createEmptyCollection();
    
    ONE_CE = getElement(ONE);
    TWO_CE = getElement(TWO);
    THREE_CE = getElement(THREE);
    
    collection.add(ONE_CE);
    collection.add(TWO_CE);
    collection.add(THREE_CE);
    
    return collection;
  }
  
  /**
   * Tests that an empty containsAll works on an empty list
   */
  @Test
  public void testContainsAllWithEmptyCollectionAndEmptyInput() {
    Collection collection = createEmptyCollection();
    HashSet<TestCollectionElement> input = new HashSet<TestCollectionElement>();
    
    assertTrue(collection.containsAll(input));
  }
  
  /**
   * Tests that an containsAll works with an empty input
   */
  @Test
  public void testContainsAllWithNonEmptyCollectionAndEmptyInput() {
    Collection collection = createCollectionWithOneTwoThree();
    
    HashSet<TestCollectionElement> input = new HashSet<TestCollectionElement>();
    
    assertTrue(collection.containsAll(input));
  }
  
  /**
   * Tests that an containsAll works with an non empty input
   */
  @Test
  public void testContainsAllWithNonEmptyCollectionAndCorrectInput() {
    Collection collection = createCollectionWithOneTwoThree();
    
    HashSet<TestCollectionElement> input = new HashSet<TestCollectionElement>();
    input.add(ONE_CE);
    input.add(THREE_CE);
    
    assertTrue(collection.containsAll(input));
  }
  
  /**
   * Tests that an containsAll works with an non empty input for
   * which the list does not contain at least one of the things
   */
  @Test
  public void testContainsAllWithNonEmptyCollectionAndIncorrectInput() {
    Collection collection = createCollectionWithOneTwoThree();
    
    HashSet<TestCollectionElement> input = new HashSet<TestCollectionElement>();
    input.add(ONE_CE);
    input.add(THREE_CE);
    input.add(getElement(FOUR));  // Not there
    
    assertFalse(collection.containsAll(input));
  }
  
  /**
   * Tests that an containsAll works with an non empty input for
   * which the list does not contain at least one of the things
   */
  @Test(expected=NullPointerException.class)
  public void testContainsAllThrows() {
    Collection collection = createCollectionWithOneTwoThree();
    
    collection.containsAll(null);
  }
  
  /**
   * Tests that an addAll works with an empty input
   */
  @Test
  public void testAddAllWithEmptyCollectionAndEmptyInput() {
    Collection collection = createEmptyCollection();
    
    HashSet<TestCollectionElement> input = new HashSet<TestCollectionElement>();
    
    assertFalse(collection.addAll(input));
    
    assertEquals(0, collection.size());
    assertTrue(collection.isEmpty());
  }
  
  /**
   * Tests that an addAll works with an empty input (non empty initial collection)
   */
  @Test
  public void testAddAllWithNonEmptyCollectionAndEmptyInput() {
    Collection collection = createCollectionWithOneTwoThree();
    
    HashSet<TestCollectionElement> input = new HashSet<TestCollectionElement>();
    
    assertFalse(collection.addAll(input));
    
    assertEquals(3, collection.size());
    assertFalse(collection.isEmpty());
    
    HashSet<TestCollectionElement> shouldBeIn =
      new HashSet<TestCollectionElement>(input);
    
    assertTrue(collection.containsAll(shouldBeIn));
  }
  
  /**
   * Tests that an addAll works with an non empty input
   */
  @Test
  public void testAddAllWithNonEmptyCollectionAndNonEmptyInputNoDups() {
    Collection collection = createCollectionWithOneTwoThree();
    
    HashSet<TestCollectionElement> input = new HashSet<TestCollectionElement>();
    input.add(getElement(FOUR));
    input.add(getElement(FIVE));
    
    assertTrue(collection.addAll(input));
    
    assertEquals(5, collection.size());
    
    HashSet<TestCollectionElement> shouldBeIn =
      new HashSet<TestCollectionElement>(input);
    shouldBeIn.add(ONE_CE);
    shouldBeIn.add(TWO_CE);
    shouldBeIn.add(THREE_CE);
    
    assertTrue(collection.containsAll(shouldBeIn));
  }
  
  /**
   * Tests that an addAll works with an non empty input with duplication
   */
  @Test
  public void testAddAllWithNonEmptyCollectionAndNonEmptyInputDups() {
    if (!doesCollectionSupportDuplicateElements()) return;
    
    Collection collection = createCollectionWithOneTwoThree();
    
    HashSet<TestCollectionElement> input = new HashSet<TestCollectionElement>();
    input.add(THREE_CE);
    input.add(getElement(FOUR));
    
    assertTrue(collection.addAll(input));
    
    HashSet<TestCollectionElement> shouldBeIn =
      new HashSet<TestCollectionElement>(input);
    shouldBeIn.add(ONE_CE);
    shouldBeIn.add(TWO_CE);
    
    assertTrue(collection.containsAll(shouldBeIn));
    
    if (doesCollectionSupportDuplicateElements()) {
      assertEquals(5, collection.size());
    }
    else {
      assertEquals(4, collection.size());
    }
  }
  
  /**
   * Tests that addAll throws with null input
   */
  @Test(expected=NullPointerException.class)
  public void testAddAllThrows() {
    Collection collection = createCollectionWithOneTwoThree();
    
    collection.addAll(null);
  }
  
  /**
   * Tests that an removaAll works with an empty input
   */
  @Test
  public void testRemoveAllWithEmptyCollectionAndEmptyInput() {
    if (!doesCollectionSupportRemove()) return;
    
    Collection collection = createEmptyCollection();
    
    HashSet<TestCollectionElement> input = new HashSet<TestCollectionElement>();
    
    assertFalse(collection.removeAll(input));
    
    assertEquals(0, collection.size());
    assertTrue(collection.isEmpty());
  }
  
  /**
   * Tests that an removeAll works with an empty input (non-empty collection)
   */
  @Test
  public void testRemoveAllWithNonEmptyCollectionAndEmptyInput() {
    if (!doesCollectionSupportRemove()) return;
    
    Collection collection = createCollectionWithOneTwoThree();
    
    HashSet<TestCollectionElement> input = new HashSet<TestCollectionElement>();
    
    assertFalse(collection.removeAll(input));
    
    assertEquals(3, collection.size());
    assertFalse(collection.isEmpty());
    
    HashSet<TestCollectionElement> shouldBeIn =
      new HashSet<TestCollectionElement>(input);
    
    assertTrue(collection.containsAll(shouldBeIn));
  }
  
  /**
   * Tests that an removeAll works with an non empty input and non
   * empty set of things to remove
   */
  @Test
  public void testRemoveAllWithNonEmptyCollectionAndNonEmptyInput() {
    if (!doesCollectionSupportRemove()) return;
    
    Collection collection = createCollectionWithOneTwoThree();
    
    HashSet<TestCollectionElement> input = new HashSet<TestCollectionElement>();
    input.add(ONE_CE);
    input.add(TWO_CE);
    
    assertTrue(collection.removeAll(input));
    
    assertEquals(1, collection.size());
    
    HashSet<TestCollectionElement> shouldBeIn =
      new HashSet<TestCollectionElement>();
    shouldBeIn.add(THREE_CE);
    
    assertTrue(collection.containsAll(shouldBeIn));
  }
  
  /**
   * Tests that an removeAll works with an non empty input and non
   * empty set of things to remove and the set has some extra elements
   * not in the original set
   */
  @Test
  public void testRemoveAllWithNonEmptyCollectionAndNonEmptyInputWithExtras() {
    if (!doesCollectionSupportRemove()) return;
    
    Collection collection = createCollectionWithOneTwoThree();
    
    HashSet<TestCollectionElement> input = new HashSet<TestCollectionElement>();
    input.add(THREE_CE);
    input.add(getElement(FOUR));
    
    assertTrue(collection.removeAll(input));
    
    HashSet<TestCollectionElement> shouldBeIn =
      new HashSet<TestCollectionElement>();
    shouldBeIn.add(ONE_CE);
    shouldBeIn.add(TWO_CE);
    
    assertTrue(collection.containsAll(shouldBeIn));
  }
  
  /**
   * Tests that removeAll throws an NPE when given null
   */
  @Test(expected=NullPointerException.class)
  public void testRemoveAllThrows() {
    if (!doesCollectionSupportRemove()) {
      throw new NullPointerException();
    }
    
    Collection collection = createCollectionWithOneTwoThree();
    
    collection.removeAll(null);
  }
  
  /**
   * Tests that an retainAll works with an empty input
   */
  @Test
  public void testRetainAllWithEmptyCollectionAndEmptyInput() {
    if (!doesCollectionSupportRetainAll()) return;
    
    Collection collection = createEmptyCollection();
    
    HashSet<TestCollectionElement> input = new HashSet<TestCollectionElement>();
    
    assertFalse(collection.retainAll(input));
    
    assertEquals(0, collection.size());
    assertTrue(collection.isEmpty());
  }
  
  /**
   * Tests that an retainAll works with an empty input (non-empty collection)
   */
  @Test
  public void testRetainAllWithNonEmptyCollectionAndEmptyInput() {
    if (!doesCollectionSupportRetainAll()) return;
    
    Collection collection = createCollectionWithOneTwoThree();
    
    HashSet<TestCollectionElement> input = new HashSet<TestCollectionElement>();
    
    assertTrue(collection.retainAll(input));
    
    assertEquals(0, collection.size());
    assertTrue(collection.isEmpty());
  }
  
  /**
   * Tests that an retainAll works with an non empty input and non
   * empty set of things to retain
   */
  @Test
  public void testRetainAllWithNonEmptyCollectionAndNonEmptyInput() {
    if (!doesCollectionSupportRetainAll()) return;
    
    Collection collection = createCollectionWithOneTwoThree();
    
    HashSet<TestCollectionElement> input = new HashSet<TestCollectionElement>();
    input.add(ONE_CE);
    input.add(TWO_CE);
    
    assertTrue(collection.retainAll(input));
    
    assertEquals(2, collection.size());
    
    HashSet<TestCollectionElement> shouldBeIn =
      new HashSet<TestCollectionElement>(input);
    
    assertTrue(collection.containsAll(shouldBeIn));
  }
  
  /**
   * Tests that an retainAll works with an non empty input and non
   * empty set of things to remove and the set has some extra elements
   * not in the original set
   */
  @Test
  public void testRetainAllWithNonEmptyCollectionAndNonEmptyInputWithExtras() {
    if (!doesCollectionSupportRetainAll()) return;
    
    Collection collection = createCollectionWithOneTwoThree();
    
    HashSet<TestCollectionElement> input = new HashSet<TestCollectionElement>();
    input.add(THREE_CE);
    input.add(getElement(FOUR));
    
    assertTrue(collection.retainAll(input));
    
    HashSet<TestCollectionElement> shouldBeIn =
      new HashSet<TestCollectionElement>();
    shouldBeIn.add(THREE_CE);
    
    assertTrue(collection.containsAll(shouldBeIn));
  }
  
  /**
   * Tests that retainAll throws an NPE when given null
   */
  @Test(expected=NullPointerException.class)
  public void testRetainAllThrows() {
    if (!doesCollectionSupportRetainAll()) {
      throw new NullPointerException();
    }
    
    Collection collection = createCollectionWithOneTwoThree();
    
    collection.retainAll(null);
  }
  
  /**
   * Tests that retainAll throws an NPE when given null
   */
  @Test(expected=UnsupportedOperationException.class)
  public void testUnsupportedRetainAllThrowsProperException() {
    if (doesCollectionSupportRetainAll()) {
      throw new UnsupportedOperationException();
    }
    
    Collection collection = createCollectionWithOneTwoThree();
    HashSet<TestCollectionElement> input = new HashSet<TestCollectionElement>();
    input.add(THREE_CE);
    
    collection.retainAll(input);
  }
  
  /**
   * Makes sure clear works on an already empty collection
   */
  @Test
  public void testClearWorksOnEmptyCollection() {
    if (!doesCollectionSupportEmpty()) return;
    
    Collection collection = createEmptyCollection();
    
    collection.clear();
    
    assertTrue(collection.isEmpty());
    assertEquals(0, collection.size());
    
  }
  
  /**
   * Makes sure clear works on an already empty collection
   */
  @Test
  public void testClearWorksOnNonEmptyCollection() {
    if (!doesCollectionSupportEmpty()) return;
    
    Collection collection = createEmptyCollection();
    
    collection.add(getElement(ONE));
    
    collection.clear();
    
    assertTrue(collection.isEmpty());
    assertEquals(0, collection.size());
  }
  
  /**
   * Tests that if you do NOT support empty that
   * you throw
   */
  @Test(expected=UnsupportedOperationException.class)
  public void testUnsupportedClearThrows() {
    if (doesCollectionSupportEmpty()) {
      throw new UnsupportedOperationException();
    }
    
    Collection collection = createEmptyCollection();
    
    collection.clear();
  }
  
  /**
   * Tries to test the resizing of the collection
   * by adding 1000 items
   */
  @Test
  public void testAddOneThousandEntries() {
    Collection collection = createEmptyCollection();
    HashSet<TestCollectionElement> elements = new HashSet<TestCollectionElement>();
    
    for (int lcv = 0; lcv < 1000; lcv++) {
      TestCollectionElement element = getElement("" + lcv);
      collection.add(element);
      elements.add(element);
    }
    
    for (TestCollectionElement element : elements) {
      assertTrue(collection.contains(element));
    }
    
  }

}
