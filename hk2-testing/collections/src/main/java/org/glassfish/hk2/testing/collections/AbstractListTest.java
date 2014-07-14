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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

import org.junit.Assert;
import org.junit.Test;

/**
 * This class can be used to test any implementation
 * of java.util.List which follows the basic rules
 * of the List interface.
 * <p>
 * Those wishing to test their list implementations for
 * the basic functionality provided by lists should extend
 * this class and implement the abstract methods, which will
 * serve as a questionaire about the optional functionalities
 * of the List interface.
 * <p>
 * Abstract methods must also be overridden in order to construct
 * the list that is being tested.  Two basic construction methods
 * are used: null constructor and a constructor with a Collection.
 * If neither constructor is supported by your List implementation
 * then it may not be possible to use this class to test your
 * list implementation.  Subclasses of this class may also override
 * the "isNullArgumentConstructorSupported" or
 * "isCollectionArgumentConstructorSupported" methods if the list does
 * not support one of the constructors.
 * <p>
 *
 * @author Copyright 2011 Oracle
 */
@SuppressWarnings({"rawtypes","unchecked"})
public abstract class AbstractListTest extends AbstractCollectionTest {
  /**
   * Subclasses should override this method if the
   * ListIterator returned from this list does not
   * support the optional set operation
   * 
   * @return true if the List supports the optional
   * set operation, and false otherwise
   */
  protected boolean doesListIteratorSupportSet() {
    return true;
  }
  
  /**
   * Subclasses should override this method if the
   * ListIterator returned from this list does not
   * support the optional add operation
   * 
   * @return true if the List supports the optional
   * add operation, and false otherwise
   */
  protected boolean doesListIteratorSupportAdd() {
    return true;
  }
  
  /**
   * Subclasses must override this method in order to construct
   * an empty list with the null argument constructor.  If
   * isNullArgumentConstructorSupported returns false then this
   * method will never be called
   * 
   * @return An empty List
   */
  
  protected abstract List createList();
  
  /**
   * Subclasses must override this method in order to construct
   * a list with the elements found in the (possibly null) Collection.
   * If isCollectionArgumentConstructorSupported returns false then
   * this method will never be called
   * 
   * @param input A possibly null and possibly empty collection of items
   * to seed the list with
   * @return An empty List that has all of the elements of the input
   * collection in Iterator ordering in the list
   */
  protected abstract List createList(Collection input);
  
  /**
   * This overrides the abstract methods on the Collection test
   */
  protected Collection createCollection() {
    return createList();
  }
  
  /**
   * This overrides the abstract methods on the Collection test
   */
  protected Collection createCollection(Collection input) {
    return createList(input);
  }
  
  private List<?> createEmptyList() {
    if (isNullArgumentConstructorSupported()) {
      return createList();
    }
    if (isCollectionArgumentConstructorSupported()) {
      return createList(new LinkedList());
    }
    
    Assert.fail("Neither null nor Collection constructor is supported by the List implementation");
    return null;
  }
  
  /**
   * Tests that a single element can be added to the list
   */
  @Test
  public void testCanAddToAList() {
    List listMe = createEmptyList();

    listMe.add(getElement(ONE));
    assertEquals(1, listMe.size());

    TestCollectionElement ele = (TestCollectionElement) listMe.get(0);
    assertEquals(ONE, ele.testCollectionValue());
    assertFalse(listMe.isEmpty());
  }
  
  @Test
  public void testSecondElementIsPutAtEndOfList() {
    List listMe = createEmptyList();

    TestCollectionElement ele1 = getElement(ONE);
    TestCollectionElement ele2 = getElement(TWO);
    listMe.add(ele1);
    listMe.add(ele2);

    assertEquals(2, listMe.size());
    
    TestCollectionElement returnedEle1 = (TestCollectionElement) listMe.get(0);
    TestCollectionElement returnedEle2 = (TestCollectionElement) listMe.get(1);
    assertEquals(ONE, returnedEle1.testCollectionValue());
    assertEquals(TWO, returnedEle2.testCollectionValue());
  }
  
  @Test
  public void testAddToBeginningOfNonEmptyList() {
    List listMe = createEmptyList();
    
    TestCollectionElement one = getElement(ONE);
    TestCollectionElement two = getElement(TWO);
    
    listMe.add(two);
    
    // Adds to the beginning of non-empty list
    listMe.add(0, one);
    
    assertEquals(2, listMe.size());
    assertEquals(one, listMe.get(0));
    assertEquals(two, listMe.get(1));
  }
  
  /**
   * Tests that the elements of the original collection are
   * in the new List in the proper order
   */
  @Test
  public void testAllElementsOfCollectionInConstructedList() {
    if (!isCollectionArgumentConstructorSupported()) {
      return;
    }
    
    List<TestCollectionElement> originalList = new LinkedList<TestCollectionElement>();
    originalList.add(getElement(ONE));
    originalList.add(getElement(TWO));
    
    List listUnderTest = createList(originalList);
    assertEquals(2, listUnderTest.size());
    
    TestCollectionElement returnedEle1 = (TestCollectionElement) listUnderTest.get(0);
    TestCollectionElement returnedEle2 = (TestCollectionElement) listUnderTest.get(1);
    assertEquals(ONE, returnedEle1.testCollectionValue());
    assertEquals(TWO, returnedEle2.testCollectionValue());
  }
  
  /**
   * Tests that the list functions properly with
   * a null element added
   */
  @Override
  @Test
  public void testCanCollectionHaveNullElements() {
    if (!doesCollectionAllowNullElements()) {
      return;
    }
    
    List list = createEmptyList();
    
    list.add(null);
    assertEquals(1, list.size());
    assertNull(list.get(0));
    assertTrue(list.contains(null));
  }
  
  /**
   * Tests that lists can have null elements interspersed
   * with non-null elements
   */
  @Override
  @Test
  public void testCanCollectionHaveNullAndNonNullElements() {
    if (!doesCollectionAllowNullElements()) {
      return;
    }
    
    List list = createEmptyList();
    
    TestCollectionElement one = getElement(ONE);
    TestCollectionElement two = getElement(TWO);
    
    list.add(null);
    list.add(one);
    list.add(null);
    list.add(two);
    
    // Now some checking, first check size
    assertEquals(4, list.size());
    
    // Second check via get(int)
    TestCollectionElement returnOne = (TestCollectionElement) list.get(1);
    assertNotNull(returnOne);
    
    TestCollectionElement returnTwo = (TestCollectionElement) list.get(3);
    assertNotNull(returnTwo);
    
    assertNull(list.get(0));
    assertEquals(ONE, returnOne.testCollectionValue());
    assertNull(list.get(2));
    assertEquals(TWO, returnTwo.testCollectionValue());
    
    // Also check contains
    assertTrue(list.contains(null));
    assertTrue(list.contains(one));
    assertTrue(list.contains(two));
  }
  
  /**
   * Tests that an iterator from a list with
   * more than one element returns the proper values
   * in the proper order
   */
  @Override
  @Test
  public void testMultipleItemIterator() {
    List list = createEmptyList();
    
    TestCollectionElement one = getElement(ONE);
    TestCollectionElement two = getElement(TWO);
    
    list.add(one);
    list.add(two);
    
    Iterator iterator = list.iterator();
    assertTrue(iterator.hasNext());
    
    TestCollectionElement returnedOne = (TestCollectionElement) iterator.next();
    assertNotNull(returnedOne);
    assertEquals(ONE, returnedOne.testCollectionValue());
    
    assertTrue(iterator.hasNext());
    TestCollectionElement returnedTwo = (TestCollectionElement) iterator.next();
    assertNotNull(returnedTwo);
    assertEquals(TWO, returnedTwo.testCollectionValue());
    
    assertFalse(iterator.hasNext());
  }
  
  /**
   * In a List the order in the array is guaranteed, so test
   * that here
   */
  @Override
  @Test
  public void testCollectionToArrayWithCollectionOfMultipleItems() {
    List collection = createEmptyList();
    
    TestCollectionElement one = getElement(ONE);
    TestCollectionElement two = getElement(TWO);
    
    collection.add(one);
    collection.add(two);
    
    Object cArray[] = collection.toArray();
    assertNotNull(cArray);
    assertEquals(2, cArray.length);
    
    String firstValue = ((TestCollectionElement) cArray[0]).testCollectionValue();
    String secondValue = ((TestCollectionElement) cArray[1]).testCollectionValue();
    
    assertEquals(ONE, firstValue);
    assertEquals(TWO, secondValue);
  }
  
  // The following tests test List specific API
  /**
   * Tests that addAll with an index works properly
   */
  @Test
  public void testAddAllWithIndex() {
    TestCollectionElement one = getElement(ONE);
    TestCollectionElement two = getElement(TWO);
    TestCollectionElement three = getElement(THREE);
    TestCollectionElement four = getElement(FOUR);
    
    List list = createEmptyList();
    list.add(one);
    list.add(four);
    
    LinkedList addin = new LinkedList();
    addin.add(two);
    addin.add(three);
    
    // This is the test
    list.addAll(1, addin);
    
    assertEquals(4, list.size());
    
    assertEquals(one, list.get(0));
    assertEquals(two, list.get(1));
    assertEquals(three, list.get(2));
    assertEquals(four, list.get(3));
  }
  
  /**
   * Tests that set with an index works
   */
  @Test
  public void testSetWithIndex() {
    TestCollectionElement one = getElement(ONE);
    TestCollectionElement two = getElement(TWO);
    TestCollectionElement three = getElement(THREE);
    TestCollectionElement four = getElement(FOUR);
    
    List list = createEmptyList();
    list.add(one);
    list.add(four);
    list.add(three);
    
    // This is the test
    assertEquals(four, list.set(1, two));
    
    assertEquals(3, list.size());
    
    assertEquals(one, list.get(0));
    assertEquals(two, list.get(1));
    assertEquals(three, list.get(2));
  }
  
  /**
   * Tests that set with a bad (-1) index works
   */
  @Test(expected=IndexOutOfBoundsException.class)
  public void testSetWithBadLowIndex() {
    TestCollectionElement one = getElement(ONE);
    
    List list = createEmptyList();
    list.set(-1, one);
  }
  
  /**
   * Tests that set with a bad (high) index works
   */
  @Test(expected=IndexOutOfBoundsException.class)
  public void testSetWithBadHighIndex() {
    TestCollectionElement one = getElement(ONE);
    
    List list = createEmptyList();
    list.set(0, one);
  }
  
  /**
   * Tests that add with an index works
   */
  @Test
  public void testAddWithIndex() {
    TestCollectionElement one = getElement(ONE);
    TestCollectionElement two = getElement(TWO);
    TestCollectionElement three = getElement(THREE);
    TestCollectionElement four = getElement(FOUR);
    
    List list = createEmptyList();
    list.add(one);
    list.add(two);
    list.add(four);
    
    // This is the test
    list.add(2, three);
    
    assertEquals(4, list.size());
    
    assertEquals(one, list.get(0));
    assertEquals(two, list.get(1));
    assertEquals(three, list.get(2));
    assertEquals(four, list.get(3));
  }
  
  /**
   * Tests that add with an bad index (-1) fails
   */
  @Test(expected=IndexOutOfBoundsException.class)
  public void testAddWithBadLowIndexThrows() {
    TestCollectionElement one = getElement(ONE);
    TestCollectionElement two = getElement(TWO);
    
    List list = createEmptyList();
    list.add(one);
    
    // This is the test
    list.add(-1, two);
  }
  
  /**
   * Tests that add with an bad index (too high) fails
   */
  @Test(expected=IndexOutOfBoundsException.class)
  public void testAddWithBadHighIndexThrows() {
    TestCollectionElement one = getElement(ONE);
    TestCollectionElement two = getElement(TWO);
    
    List list = createEmptyList();
    list.add(one);
    
    // This is the test
    list.add(2, two);
  }
  
  /**
   * Tests that remove with an index works
   */
  @Test
  public void testRemoveWithIndex() {
    if (!doesCollectionSupportRemove()) {
      return;
    }
    
    TestCollectionElement one = getElement(ONE);
    TestCollectionElement two = getElement(TWO);
    TestCollectionElement three = getElement(THREE);
    
    List list = createEmptyList();
    list.add(one);
    list.add(three);
    list.add(two);
    
    // This is the test
    assertEquals(three, list.remove(1));
    
    assertEquals(2, list.size());
    
    assertEquals(one, list.get(0));
    assertEquals(two, list.get(1));
    
  }
  
  /**
   * Tests that add with an index works
   */
  @Test(expected=IndexOutOfBoundsException.class)
  public void testRemoveWithIndexThrowsOnBadIndex() {
    if (!doesCollectionSupportRemove()) {
      return;
    }
    
    TestCollectionElement one = getElement(ONE);
    TestCollectionElement two = getElement(TWO);
    TestCollectionElement three = getElement(THREE);
    
    List list = createEmptyList();
    list.add(one);
    list.add(three);
    list.add(two);
    
    list.remove(3);
  }
  
  /**
   * Tests indexOf functionality
   */
  @Test
  public void testIndexOf() {
    TestCollectionElement one = getElement(ONE);
    TestCollectionElement two = getElement(TWO);
    TestCollectionElement three = getElement(THREE);
    
    List list = createEmptyList();
    list.add(one);
    list.add(two);
    if (doesCollectionSupportDuplicateElements()) {
      list.add(one);
    }
    
    assertEquals(0, list.indexOf(one));
    assertEquals(1, list.indexOf(two));
    assertEquals(-1, list.indexOf(three));
  }
  
  /**
   * Tests lastIndexOf functionality
   */
  @Test
  public void testLastIndexOf() {
    TestCollectionElement one = getElement(ONE);
    TestCollectionElement two = getElement(TWO);
    TestCollectionElement three = getElement(THREE);
    
    List list = createEmptyList();
    list.add(one);
    list.add(two);
    if (doesCollectionSupportDuplicateElements()) {
      list.add(one);
      
      assertEquals(2, list.lastIndexOf(one));
    }
    else {
      assertEquals(0, list.lastIndexOf(one));
    }
    
    assertEquals(1, list.lastIndexOf(two));
    assertEquals(-1, list.lastIndexOf(three));
  }
  
  /**
   * Tests a ListIterator on an empty list
   */
  @Test
  public void testListIteratorOnEmptyList() {
    List list = createEmptyList();
    
    ListIterator li = list.listIterator();
    
    assertFalse(li.hasPrevious());
    assertFalse(li.hasNext());
  }
  
  /**
   * Tests we can go backward and forward with the ListIterator
   */
  @Test
  public void testListIteratorForwardAndBack() {
    TestCollectionElement one = getElement(ONE);
    TestCollectionElement two = getElement(TWO);
    TestCollectionElement three = getElement(THREE);
    
    List list = createEmptyList();
    list.add(one);
    list.add(two);
    list.add(three);
    
    ListIterator li = list.listIterator();
    
    // Forward
    assertTrue(li.hasNext());
    assertEquals(0, li.nextIndex());
    assertEquals(-1, li.previousIndex());
    assertEquals(one, li.next());
    
    assertTrue(li.hasNext());
    assertEquals(1, li.nextIndex());
    assertEquals(0, li.previousIndex());
    assertEquals(two, li.next());
    
    assertTrue(li.hasNext());
    assertEquals(2, li.nextIndex());
    assertEquals(1, li.previousIndex());
    assertEquals(three, li.next());
    
    assertFalse(li.hasNext());
    assertEquals(3, li.nextIndex());
    assertEquals(2, li.previousIndex());
    
    // Backwards
    assertTrue(li.hasPrevious());
    assertEquals(3, li.nextIndex());
    assertEquals(2, li.previousIndex());
    assertEquals(three, li.previous());
    
    assertTrue(li.hasPrevious());
    assertEquals(2, li.nextIndex());
    assertEquals(1, li.previousIndex());
    assertEquals(two, li.previous());
    
    assertTrue(li.hasPrevious());
    assertEquals(1, li.nextIndex());
    assertEquals(0, li.previousIndex());
    assertEquals(one, li.previous());
    
    assertFalse(li.hasPrevious());
    assertEquals(0, li.nextIndex());
    assertEquals(-1, li.previousIndex());
  }
  
  /**
   * Tests we can go backward and forward with the ListIterator,
   * using the index version of listIterator
   */
  @Test
  public void testListIteratorBackwardAndForward() {
    TestCollectionElement one = getElement(ONE);
    TestCollectionElement two = getElement(TWO);
    TestCollectionElement three = getElement(THREE);
    
    List list = createEmptyList();
    list.add(one);
    list.add(two);
    list.add(three);
    
    ListIterator li = list.listIterator(3);
    
    // Backwards
    assertTrue(li.hasPrevious());
    assertEquals(3, li.nextIndex());
    assertEquals(2, li.previousIndex());
    assertEquals(three, li.previous());
    
    assertTrue(li.hasPrevious());
    assertEquals(2, li.nextIndex());
    assertEquals(1, li.previousIndex());
    assertEquals(two, li.previous());
    
    assertTrue(li.hasPrevious());
    assertEquals(1, li.nextIndex());
    assertEquals(0, li.previousIndex());
    assertEquals(one, li.previous());
    
    assertFalse(li.hasPrevious());
    assertEquals(0, li.nextIndex());
    assertEquals(-1, li.previousIndex());
    
    // Forward
    assertTrue(li.hasNext());
    assertEquals(0, li.nextIndex());
    assertEquals(-1, li.previousIndex());
    assertEquals(one, li.next());
    
    assertTrue(li.hasNext());
    assertEquals(1, li.nextIndex());
    assertEquals(0, li.previousIndex());
    assertEquals(two, li.next());
    
    assertTrue(li.hasNext());
    assertEquals(2, li.nextIndex());
    assertEquals(1, li.previousIndex());
    assertEquals(three, li.next());
    
    assertFalse(li.hasNext());
    assertEquals(3, li.nextIndex());
    assertEquals(2, li.previousIndex());
  }
  
  /**
   * Tests we can go forward with the ListIterator,
   * using the index version of listIterator, from
   * the middle of the list
   */
  @Test
  public void testListIteratorForwardFromMiddle() {
    TestCollectionElement one = getElement(ONE);
    TestCollectionElement two = getElement(TWO);
    TestCollectionElement three = getElement(THREE);
    
    List list = createEmptyList();
    list.add(one);
    list.add(two);
    list.add(three);
    
    ListIterator li = list.listIterator(1);
    
    assertTrue(li.hasPrevious());
    assertTrue(li.hasNext());
    assertEquals(two, li.next());
  }
  
  /**
   * Tests we can go backward with the ListIterator,
   * using the index version of listIterator, from
   * the middle of the list
   */
  @Test
  public void testListIteratorBackwardFromMiddle() {
    TestCollectionElement one = getElement(ONE);
    TestCollectionElement two = getElement(TWO);
    TestCollectionElement three = getElement(THREE);
    
    List list = createEmptyList();
    list.add(one);
    list.add(two);
    list.add(three);
    
    ListIterator li = list.listIterator(2);
    
    assertTrue(li.hasNext());
    assertTrue(li.hasPrevious());
    assertEquals(two, li.previous());
  }
  
  @Test(expected=IndexOutOfBoundsException.class)
  public void testListIteratorWithBadIndexLowThrows() {
    List list = createEmptyList();
    list.listIterator(-1);
  }
  
  @Test(expected=IndexOutOfBoundsException.class)
  public void testListIteratorWithBadIndexHighThrows() {
    List list = createEmptyList();
    list.listIterator(1);
  }
  
  @Test(expected=NoSuchElementException.class)
  public void testPreviousGoneTooFar() {
    TestCollectionElement one = getElement(ONE);
    
    List list = createEmptyList();
    list.add(one);
    
    ListIterator itr = list.listIterator(1);
    itr.previous();
    itr.previous();
  }
  
  @Test(expected=NoSuchElementException.class)
  public void testPreviousOnEmptyListThrows() {
    List list = createEmptyList();
    
    ListIterator itr = list.listIterator(0);
    itr.previous();
  }
  
  /**
   * Tests the ListIterators remove operation
   */
  @Test
  public void testListIteratorRemoveAfterNext() {
    if (!doesCollectionsIteratorSupportRemove()) return;
    
    TestCollectionElement one = getElement(ONE);
    TestCollectionElement two = getElement(TWO);
    TestCollectionElement three = getElement(THREE);
    
    List list = createEmptyList();
    list.add(one);
    list.add(two);
    list.add(three);
    
    ListIterator li = list.listIterator();
    
    li.next();
    li.next();
    li.remove();
    
    assertEquals(2, list.size());
    assertEquals(one, list.get(0));
    assertEquals(three, list.get(1));
  }
  
  /**
   * Tests the ListIterators remove operation
   */
  @Test
  public void testListIteratorRemoveAfterPrevious() {
    if (!doesCollectionsIteratorSupportRemove()) return;
    
    TestCollectionElement one = getElement(ONE);
    TestCollectionElement two = getElement(TWO);
    TestCollectionElement three = getElement(THREE);
    
    List list = createEmptyList();
    list.add(one);
    list.add(two);
    list.add(three);
    
    ListIterator li = list.listIterator(3);
    
    li.previous();
    li.previous();
    li.remove();
    
    assertEquals(2, list.size());
    assertEquals(one, list.get(0));
    assertEquals(three, list.get(1));
  }
  
  /**
   * Tests the ListIterators remove operation
   */
  @Test(expected=IllegalStateException.class)
  public void testListIteratorThrowsIfNeverMoved() {
    if (!doesCollectionsIteratorSupportRemove()) {
      throw new IllegalStateException();
    }
    
    TestCollectionElement one = getElement(ONE);
    TestCollectionElement two = getElement(TWO);
    TestCollectionElement three = getElement(THREE);
    
    List list = createEmptyList();
    list.add(one);
    list.add(two);
    list.add(three);
    
    ListIterator li = list.listIterator(1);
    
    li.remove();
  }
  
  /**
   * Tests we can go forward with the ListIterator,
   * using the index version of listIterator, from
   * the middle of the list
   */
  @Test
  public void testListIteratorSetAfterNext() {
    if (!doesListIteratorSupportSet()) return;
    
    TestCollectionElement one = getElement(ONE);
    TestCollectionElement two = getElement(TWO);
    TestCollectionElement three = getElement(THREE);
    
    List list = createEmptyList();
    list.add(one);
    list.add(three);
    
    ListIterator li = list.listIterator();
    
    li.next();
    li.next();
    li.set(two);
    
    assertEquals(2, list.size());
    assertEquals(one, list.get(0));
    assertEquals(two, list.get(1));
  }
  
  /**
   * Tests we can go forward with the ListIterator,
   * using the index version of listIterator, from
   * the middle of the list
   */
  @Test
  public void testListIteratorSetAfterPrevious() {
    if (!doesListIteratorSupportSet()) return;
    
    TestCollectionElement one = getElement(ONE);
    TestCollectionElement two = getElement(TWO);
    TestCollectionElement three = getElement(THREE);
    
    List list = createEmptyList();
    list.add(three);
    list.add(two);
    
    ListIterator li = list.listIterator(2);
    
    li.previous();
    li.previous();
    li.set(one);
    
    assertEquals(2, list.size());
    assertEquals(one, list.get(0));
    assertEquals(two, list.get(1));
  }
  
  /**
   * Tests the ListIterators remove operation
   */
  @Test(expected=IllegalStateException.class)
  public void testListIteratorSetThrowsIfNeverMoved() {
    if (!doesListIteratorSupportSet()) {
      throw new IllegalStateException();
    }
    
    TestCollectionElement one = getElement(ONE);
    TestCollectionElement two = getElement(TWO);
    TestCollectionElement three = getElement(THREE);
    TestCollectionElement four = getElement(FOUR);
    
    List list = createEmptyList();
    list.add(one);
    list.add(two);
    list.add(three);
    
    ListIterator li = list.listIterator(1);
    
    li.set(four);
  }
  
  /**
   * Tests the ListIterators remove operation
   */
  @Test(expected=IllegalStateException.class)
  public void testListIteratorSetThrowsIfAfterRemove() {
    if (!doesListIteratorSupportSet() ||
        !doesCollectionsIteratorSupportRemove()) {
      throw new IllegalStateException();
    }
    
    TestCollectionElement one = getElement(ONE);
    TestCollectionElement two = getElement(TWO);
    TestCollectionElement three = getElement(THREE);
    TestCollectionElement four = getElement(FOUR);
    
    List list = createEmptyList();
    list.add(one);
    list.add(two);
    list.add(three);
    
    ListIterator li = list.listIterator(1);
    
    li.next();
    li.remove();
    
    li.set(four);
  }
  
  /**
   * Tests the ListIterators remove operation
   */
  @Test(expected=IllegalStateException.class)
  public void testListIteratorSetThrowsIfAfterAdd() {
    if (!doesListIteratorSupportSet() ||
        !doesListIteratorSupportAdd()) {
      throw new IllegalStateException();
    }
    
    TestCollectionElement one = getElement(ONE);
    TestCollectionElement two = getElement(TWO);
    TestCollectionElement three = getElement(THREE);
    TestCollectionElement four = getElement(FOUR);
    TestCollectionElement five = getElement(FIVE);
    
    List list = createEmptyList();
    list.add(one);
    list.add(two);
    list.add(four);
    
    ListIterator li = list.listIterator();
    
    li.next();
    li.next();
    li.add(three);
    
    li.set(five);
  }
  
  /**
   * Tests we can go forward with the ListIterator,
   * using the index version of listIterator, from
   * the middle of the list
   */
  @Test
  public void testListIteratorAddAfterNext() {
    if (!doesListIteratorSupportAdd()) return;
    
    TestCollectionElement one = getElement(ONE);
    TestCollectionElement two = getElement(TWO);
    TestCollectionElement three = getElement(THREE);
    
    List list = createEmptyList();
    list.add(one);
    list.add(three);
    
    ListIterator li = list.listIterator();
    
    li.next();
    li.add(two);
    
    assertEquals(3, list.size());
    assertEquals(one, list.get(0));
    assertEquals(two, list.get(1));
    assertEquals(three, list.get(2));
  }
  
  /**
   * Tests we can go forward with the ListIterator,
   * using the index version of listIterator, from
   * the middle of the list
   */
  @Test
  public void testListIteratorAddAfterPrevious() {
    if (!doesListIteratorSupportAdd()) return;
    
    TestCollectionElement one = getElement(ONE);
    TestCollectionElement two = getElement(TWO);
    TestCollectionElement three = getElement(THREE);
    
    List list = createEmptyList();
    list.add(one);
    list.add(three);
    
    ListIterator li = list.listIterator(2);
    
    li.previous();
    li.add(two);
    
    assertEquals(3, list.size());
    assertEquals(one, list.get(0));
    assertEquals(two, list.get(1));
    assertEquals(three, list.get(2));
  }
  
  /**
   * Tests basic functionality of subList
   */
  @Test
  public void testSubList() {
    TestCollectionElement one = getElement(ONE);
    TestCollectionElement two = getElement(TWO);
    TestCollectionElement three = getElement(THREE);
    TestCollectionElement four = getElement(FOUR);
    
    List list = createEmptyList();
    list.add(one);
    list.add(two);
    list.add(three);
    list.add(four);
    
    List subList = list.subList(1, 3);
    
    assertEquals(2, subList.size());
    
    assertEquals(two, subList.get(0));
    assertEquals(three, subList.get(1));
    
    assertTrue(subList.contains(two));
    assertTrue(subList.contains(three));
    assertFalse(subList.contains(one));
    assertFalse(subList.contains(four));
  }
  
  /**
   * Tests basic functionality of subList
   */
  @Test
  public void testSubListAffectsUnderlyingList() {
    TestCollectionElement one = getElement(ONE);
    TestCollectionElement two = getElement(TWO);
    TestCollectionElement three = getElement(THREE);
    TestCollectionElement four = getElement(FOUR);
    
    List list = createEmptyList();
    list.add(one);
    list.add(two);
    list.add(three);
    list.add(four);
    
    list.subList(1, 3).clear();
    
    assertEquals(2, list.size());
    
    assertEquals(one, list.get(0));
    assertEquals(four, list.get(1));
  }
}
