/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2011 Oracle and/or its affiliates. All rights reserved.
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
package org.jvnet.hk2.component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.sun.hk2.component.ExistingSingletonInhabitant;

import junit.framework.Test;
import junit.framework.TestCase;

/**
 * Habitat Tests
 *  
 * @author Jeff Trent
 */
//TODO: Settle on concurrency controls enabled
@SuppressWarnings("unchecked")
public class HabitatTest extends TestCase {

  // concurrency controls disabled
  TestHabitat h = new TestHabitat(false);
  
  // concurrency controls enabled
  TestHabitat hc = new TestHabitat(true);
  
  public void testGetAllByType() {
    runTestGetAllByType(h);
    runTestGetAllByType(hc);
  }
  
  private void runTestGetAllByType(Habitat h) {
    Inhabitant entry = new ExistingSingletonInhabitant<TestCase>(TestCase.class, this);
    h.add(entry);

    Collection<TestCase> coll = h.getAllByType(TestCase.class);
    assertNotNull(coll);
    assertTrue("expect list type", coll instanceof List);
    assertEquals("size", 1, coll.size());
    assertSame("val", this, coll.iterator().next());
  }

  public void testGetAllByContract() {
    runTestGetAllByContract(h);
    runTestGetAllByContract(hc);
  }
  
  private void runTestGetAllByContract(Habitat h) {
    Inhabitant entry = new ExistingSingletonInhabitant<TestCase>(TestCase.class, this);
    h.add(entry);
    h.addIndex(entry, TestCase.class.getName(), null);

    Collection<TestCase> coll = h.getAllByContract(TestCase.class);
    assertNotNull(coll);
    assertTrue("expect list type", coll instanceof List);
    assertEquals("size", 1, coll.size());
    assertSame("val", this, coll.iterator().next());
  }

  public void testRemove() {
    runTestRemove(h);
    runTestRemove(hc);
  }
  
  private void runTestRemove(Habitat h) {
    Inhabitant entry = new ExistingSingletonInhabitant<TestCase>(TestCase.class, this);
    h.add(entry);
    h.addIndex(entry, TestCase.class.getName(), null);
    assertTrue(h.remove(entry));
    assertTrue("removal expected", h.removeIndex(TestCase.class.getName(), (String)null));
    assertFalse("already removed", h.removeIndex(TestCase.class.getName(), (String)null));
    
    Collection<TestCase> coll = h.getAllByContract(TestCase.class);
    assertNotNull(coll);
    assertTrue("expect list type", coll instanceof List);
    assertEquals("size", 0, coll.size());
  }

  public void testRemoveAllByType() {
    runTestRemoveAllByType(h);
    runTestRemoveAllByType(hc);
  }
  
  private void runTestRemoveAllByType(Habitat h) {
    Inhabitant entry = new ExistingSingletonInhabitant<TestCase>(TestCase.class, this);
    h.add(entry);
    h.addIndex(entry, TestCase.class.getName(), null);
    assertTrue(h.removeAllByType(TestCase.class));
    assertFalse(h.removeAllByType(TestCase.class));
    assertTrue("removal expected", h.removeIndex(TestCase.class.getName(), (String)null));
    assertFalse("already removed", h.removeIndex(TestCase.class.getName(), (String)null));
    
    Collection<TestCase> coll = h.getAllByContract(TestCase.class);
    assertNotNull(coll);
    assertTrue("expect list type", coll instanceof List);
    assertEquals("size", 0, coll.size());
  }

  public void testAddRemoveHabitatListeners() {
    runTestAddRemoveHabitatListeners(h);
    runTestAddRemoveHabitatListeners(hc);
  }
  
  private void runTestAddRemoveHabitatListeners(Habitat h) {
    Inhabitant i = h.getInhabitantByContract(Habitat.ListenersByTypeInhabitant.class.getName());
    assertNotNull(i);
    assertEquals("size", 2, ((Habitat.ListenersByTypeInhabitant)i).size());
    
    HabitatListener listener = new TestHabitatListener();
    h.addHabitatListener(listener);
    i = h.getInhabitantByContract(Habitat.ListenersByTypeInhabitant.class.getName());
    assertNotNull(i);
    assertEquals("size", 3, ((Habitat.ListenersByTypeInhabitant)i).size());

    assertTrue(h.removeHabitatListener(listener));
    i = h.getInhabitantByContract(Habitat.ListenersByTypeInhabitant.class.getName());
    assertNotNull(i);
    assertEquals("size", 2, ((Habitat.ListenersByTypeInhabitant)i).size());

    assertFalse(h.removeHabitatListener(listener));
    i = h.getInhabitantByContract(Habitat.ListenersByTypeInhabitant.class.getName());
    assertNotNull(i);
    assertEquals("size", 2, ((Habitat.ListenersByTypeInhabitant)i).size());
  }
  
  public void testAddRemoveHabitatListeners_filtered() {
    runTestAddRemoveHabitatListeners_filtered(h);
    runTestAddRemoveHabitatListeners_filtered(hc);
  }
  
  private void runTestAddRemoveHabitatListeners_filtered(Habitat h) {
    Inhabitant i = h.getInhabitantByContract(Habitat.ListenersByTypeInhabitant.class.getName(), 
            TestCase.class.getName());
    assertNull(i);

    HabitatListener listener = new TestHabitatListener();
    h.addHabitatListener(listener, Object.class.getName(), TestCase.class.getName());
    i = h.getInhabitantByContract(Habitat.ListenersByTypeInhabitant.class.getName(),
            TestCase.class.getName());
    assertNotNull(i);
    assertEquals("size", 1, ((Habitat.ListenersByTypeInhabitant)i).size());

    i = h.getInhabitantByContract(Habitat.ListenersByTypeInhabitant.class.getName(),
            HabitatListener.class.getName());
    assertNull(i);

    assertTrue(h.removeHabitatListener(listener));
    i = h.getInhabitantByContract(Habitat.ListenersByTypeInhabitant.class.getName(),
        TestCase.class.getName());
    assertNotNull(i);
    assertEquals("size", 0, ((Habitat.ListenersByTypeInhabitant)i).size());

    assertFalse(h.removeHabitatListener(listener));
    i = h.getInhabitantByContract(Habitat.ListenersByTypeInhabitant.class.getName(),
        TestCase.class.getName());
    assertNotNull(i);
    assertEquals("size", 0, ((Habitat.ListenersByTypeInhabitant)i).size());
  }

  public void xxxtestRemovePerf() {
    for (int i = 0; i < 10000000; i++) {
      Inhabitant entry = new ExistingSingletonInhabitant<TestCase>(TestCase.class, this);
      h.add(entry);
      h.addIndex(entry, TestCase.class.getName(), null);

      Collection<TestCase> coll = h.getAllByContract(TestCase.class);

      assertTrue(h.remove(entry));
      assertTrue(h.removeIndex(TestCase.class.getName(), (String)null));
      
      coll = h.getAllByContract(TestCase.class);
      assertNotNull(coll);
      assertTrue("expect list type", coll instanceof List);
      assertEquals("size", 0, coll.size());
    }
  }
  
  public void testRemoveIndex() {
    Inhabitant entry1 = new ExistingSingletonInhabitant<TestCase>(TestCase.class, this);
    h.add(entry1);
    h.addIndex(entry1, TestCase.class.getName(), null);
    h.addIndex(entry1, Test.class.getName(), null);

    Inhabitant entry2 = new ExistingSingletonInhabitant<TestCase>(TestCase.class, this);
    h.add(entry2);
    h.addIndex(entry2, TestCase.class.getName(), null);
    h.addIndex(entry2, Test.class.getName(), null);
    Collection<?> coll = h.getAllByContract(TestCase.class);
    assertNotNull(coll);
    assertEquals(2, coll.size());
    
    assertTrue(h.removeIndex(TestCase.class.getName(), entry1));
    coll = h.getAllByContract(TestCase.class);
    assertNotNull(coll);
    assertEquals(1, coll.size());

    coll = h.getAllByContract(Test.class);
    assertNotNull(coll);
    assertEquals(2, coll.size());
    
    h.removeIndex(Test.class.getName(), this);
    coll = h.getAllByContract(TestCase.class);
    assertNotNull(coll);
    assertEquals(1, coll.size());

    coll = h.getAllByContract(Test.class);
    assertNotNull(coll);
    assertEquals(0, coll.size());
  }

  public void testGetByContractAndName() {
    Inhabitant i1 = new ExistingSingletonInhabitant(String.class, "named");
    Inhabitant i2 = new ExistingSingletonInhabitant(String.class, "unnamed");

    h.add(i2);
    h.addIndex(i2, String.class.getName(), null);
    h.add(i1);
    h.addIndex(i1, String.class.getName(), "named");

    Collection<Inhabitant> coll = h.getAllByContract(String.class.getName());
    assertEquals("should have the named and the unnamed instance" + coll.toString(), 2, coll.size());

    String ret = (String)h.getComponent(String.class.getName(), "named");
    assertEquals("named", ret);
  }

  public void testGetAllByContractWithNoServiceRanking() {
    populateHabitatForServiceRankingTest(false);
    Collection<String> coll = h.getAllByContract(String.class);
    verifyEquals("natural order expected since no service ranking", coll, "a", "b", "c", "d", "e", "f");
  }

  // TODO
  public void xxx_testGetAllByContractWithServiceRanking() {
    populateHabitatForServiceRankingTest(true);
    Collection<String> coll = h.getAllByContract(String.class);
    verifyEquals("ranking order expected since service ranking present", coll, "d", "c", "f", "a", "b", "f");
  }
  
  public void populateHabitatForServiceRankingTest(boolean hasRankings) {
    Inhabitant i1, i2, i3, i4, i5, i6;
    
    if (hasRankings) {
      i1 = new ExistingSingletonInhabitant(String.class, "a");
      i2 = new ExistingSingletonInhabitant(String.class, "b", metdata(null, Constants.SERVICE_RANKING, "x"));
      i3 = new ExistingSingletonInhabitant(String.class, "c", metdata(null, Constants.SERVICE_RANKING, "2"));
      i4 = new ExistingSingletonInhabitant(String.class, "d", metdata(null, Constants.SERVICE_RANKING, "1"));
      i5 = new ExistingSingletonInhabitant(String.class, "e", metdata(null, Constants.SERVICE_RANKING, "3"));
      i6 = new ExistingSingletonInhabitant(String.class, "f");
    } else {
      i1 = new ExistingSingletonInhabitant(String.class, "a");
      i2 = new ExistingSingletonInhabitant(String.class, "b", metdata(null, "whatever", "x"));
      i3 = new ExistingSingletonInhabitant(String.class, "c", metdata(null, "whatever", "2"));
      i4 = new ExistingSingletonInhabitant(String.class, "d", metdata(null, "whatever", "1"));
      i5 = new ExistingSingletonInhabitant(String.class, "e", metdata(null, "whatever", "3"));
      i6 = new ExistingSingletonInhabitant(String.class, "f");
    }

    add(i1, "a");
    add(i2, null);
    add(i3, "c");
    add(i4, null);
    add(i5, "e");
    add(i6, null);
  }

  private void verifyEquals(String msg, Collection<?> coll, String... vals) {
    ArrayList x = new ArrayList(coll);
    ArrayList y = new ArrayList(Arrays.asList(vals));
    assertEquals(msg, x, y);
  }
  
  private void add(Inhabitant i, String name) {
    h.add(i);
    h.addIndex(i, String.class.getName(), name);
  }

  private MultiMap metdata(MultiMap mm, String key, String val) {
    if (null == mm) {
      mm = new MultiMap();
    }
    
    mm.add(key, val);
    
    return mm;
  }

}
