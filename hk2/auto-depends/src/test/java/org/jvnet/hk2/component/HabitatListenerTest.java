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
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jvnet.hk2.annotations.FactoryFor;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.component.HabitatListener;
import org.jvnet.hk2.component.HabitatListenerWeakProxy;
import org.jvnet.hk2.component.Inhabitant;
import org.jvnet.hk2.component.HabitatListener.EventType;
import org.jvnet.hk2.component.TestHabitatListener.Call;

import com.sun.hk2.component.ExistingSingletonInhabitant;

import junit.framework.TestCase;

/**
 * Inhabitant Listener Tests.
 * 
 * @author Jeff Trent
 */
//TODO: Settle on concurrency controls enabled
@SuppressWarnings("unchecked")
public class HabitatListenerTest extends TestCase {

  // concurrency controls disabled
  TestHabitat h = new TestHabitat(false);
  
  // concurrency controls enabled
  TestHabitat hc = new TestHabitat(true);

  
  /**
   * Verify the basics for adding a component while listening to the habitat
   */
  public void testHabitatListenerStdCase() {
    runTestHabitatListenerStdCase(h);
    runTestHabitatListenerStdCase(hc);
  }
  
  private void runTestHabitatListenerStdCase(TestHabitat h) {
    TestHabitatListener hl = new TestHabitatListener();
    h.addComponent("test", this);
    assertTrue("sanity check", hl.calls.isEmpty());
    
    h.addHabitatListener(hl);
    assertEquals("calls: " + hl.calls, 0, hl.calls.size());
    
    h.addComponent("test", this);
    assertEquals("calls: " + hl.calls, 1, hl.calls.size());
    assertCall(h, hl.calls.get(0), EventType.INHABITANT_ADDED, this, null, null);
    hl.calls.clear();

    Inhabitant entry = new ExistingSingletonInhabitant<TestCase>(TestCase.class, this);
    h.addIndex(entry, "index", "name");
    assertEquals("calls for index", 1, hl.calls.size());
    assertCall(h, hl.calls.get(0), EventType.INHABITANT_INDEX_ADDED, this, "index", "name");
    hl.calls.clear();
    
    h.removeIndex("index", "name");
    assertEquals("calls for index", 1, hl.calls.size());
    assertCall(h, hl.calls.get(0), EventType.INHABITANT_INDEX_REMOVED, this, "index", "name");
    hl.calls.clear();

    entry = new TestA(new Object());
    h.addIndex(entry, FactoryFor.class.getName(), "name");
    assertEquals("calls (self callbacks)", 3, hl.calls.size());
    hl.calls.clear();
    
    h.removeAllByType(getClass());
    assertEquals("calls", 2, hl.calls.size());
    hl.calls.clear();

    h.release();
    assertEquals("calls: " + hl.calls, 10, hl.calls.size());
  }

  /**
   * Verify the basics for adding a component while listening to the habitat using
   * a filtered set of contracts.
   */
  public void testHabitatListenerContractCase() {
    runTestHabitatListenerContractCase(h);
    runTestHabitatListenerContractCase(hc);
  }
  
  private void runTestHabitatListenerContractCase(TestHabitat h) {
    TestHabitatListener hl_all = new TestHabitatListener();
    TestHabitatListener hl_filtered = new TestHabitatListener();
    TestHabitatListener hl_none = new TestHabitatListener();
    h.addComponent("test", this);
    assertTrue("sanity check", hl_filtered.calls.isEmpty());
    
    h.addHabitatListener(hl_all);
    h.addHabitatListener(hl_filtered, "index");
    h.addHabitatListener(hl_none, "dummy");
    h.addComponent("test", this);
    assertEquals("calls", 0, hl_none.calls.size());
    assertEquals("calls", 0, hl_filtered.calls.size());
    assertEquals("calls", 3, hl_all.calls.size());
    hl_all.calls.clear();

    Inhabitant entry = new ExistingSingletonInhabitant<TestCase>(TestCase.class, this);
    h.addIndex(entry, "index", "name");
    assertEquals("calls", 0, hl_none.calls.size());
    assertEquals("calls for index", 1, hl_filtered.calls.size());
    assertCall(h, hl_filtered.calls.get(0), EventType.INHABITANT_INDEX_ADDED, this, "index", "name");
    hl_filtered.calls.clear();
    assertEquals("calls for index", 1, hl_all.calls.size());
    assertCall(h, hl_all.calls.get(0), EventType.INHABITANT_INDEX_ADDED, this, "index", "name");
    hl_all.calls.clear();
    
    h.removeIndex("index", "name");
    assertEquals("calls", 0, hl_none.calls.size());
    assertEquals("calls for index", 1, hl_filtered.calls.size());
    assertCall(h, hl_filtered.calls.get(0), EventType.INHABITANT_INDEX_REMOVED, this, "index", "name");
    hl_filtered.calls.clear();
    assertEquals("calls for index", 1, hl_all.calls.size());
    assertCall(h, hl_all.calls.get(0), EventType.INHABITANT_INDEX_REMOVED, this, "index", "name");
    hl_all.calls.clear();

    h.release();
    assertEquals("calls", 0, hl_none.calls.size());
    assertEquals("calls", 0, hl_filtered.calls.size());
    assertEquals("calls: " + hl_all.calls, 13, hl_all.calls.size());
  }

  public void testForcedListenerRemoval() throws Exception {
    runTestForcedListenerRemoval(h);
    runTestForcedListenerRemoval(hc);
  }

  private void runTestForcedListenerRemoval(TestHabitat h) {
    TestHabitatListener hl = new TestHabitatListener();
    h.addHabitatListener(hl);
    h.addComponent("test", this);
    assertEquals("calls", 1, hl.calls.size());
    assertCall(h, hl.calls.get(0), EventType.INHABITANT_ADDED, this, null, null);
    hl.calls.clear();

    h.removeHabitatListener(hl);
    assertEquals("calls for index", 1, hl.calls.size());
    assertCall(h, hl.calls.get(0), EventType.INHABITANT_REMOVED, hl, null, null);
    hl.calls.clear();

    Inhabitant entry = new ExistingSingletonInhabitant<TestCase>(TestCase.class, this);
    h.addIndex(entry, "index", "name");
    assertEquals("calls for index (after removal)", 0, hl.calls.size());

    // should have no affect
    h.removeHabitatListener(hl);
    assertEquals("calls after removed a second time", 0, hl.calls.size());
  }

  /**
   * Verify that returning false curtails listening
   */
  public void testListenerSelfRemoval() throws Exception {
    runTestListenerSelfRemoval(h);
    runTestListenerSelfRemoval(hc);
  }
  
  private void runTestListenerSelfRemoval(TestHabitat h) {
    TestHabitatListener hl = new TestHabitatListener(1);
    h.addHabitatListener(hl);
    Inhabitant entry = new ExistingSingletonInhabitant<TestCase>(TestCase.class, this);
    h.addIndex(entry, "index", "name");
    hl.calls.clear();
    
    // no more listener calls expected by now...
    h.addComponent("test", this);
    assertEquals("calls (after removing listener): " + hl.calls, 0, hl.calls.size());
    
    entry = new TestA(new Object());
    h.addIndex(entry, FactoryFor.class.getName(), "name");
    assertEquals("calls (after removing listener): " + hl.calls, 0, hl.calls.size());
    
    h.removeAllByType(getClass());
    assertEquals("calls (after removing listener): " + hl.calls, 0, hl.calls.size());

    h.release();
    assertEquals("calls (after removing listener): " + hl.calls, 0, hl.calls.size());
  }

  /**
   * handle the case where habitat listener removal happens simultaneously across two threads 
   */
  public void testHabitatListenerAsyncRemoval() throws Exception {
    runTestHabitatListenerAsyncRemoval(h);
    runTestHabitatListenerAsyncRemoval(hc);
  }

  private void runTestHabitatListenerAsyncRemoval(final TestHabitat h) throws Exception {
    ExecutorService exec = Executors.newFixedThreadPool(2);

    final List<Throwable> errors = Collections.synchronizedList(new ArrayList<Throwable>());
    final TestHabitatListener hl1 = new TestHabitatListener(1);
    final TestHabitatListener hl2 = new TestHabitatListener(2);
    exec.execute(new Runnable() {
      @Override
      public void run() {
        try {
          h.addHabitatListener(hl1);
        } catch (Throwable t) {
          errors.add(t);
        }
      }
    });
    exec.execute(new Runnable() {
      @Override
      public void run() {
        try {
          h.addHabitatListener(hl2);
        } catch (Throwable t) {
          errors.add(t);
        }
      }
    });
    
    Future f1 = exec.submit(new Runnable() {
      @Override
      public void run() {
        try {
          h.addComponent("test1", hl1);
        } catch (Throwable t) {
          errors.add(t);
        }
      }
    });

    Future f2 = exec.submit(new Runnable() {
      @Override
      public void run() {
        try {
          h.addComponent("test2", hl2);
        } catch (Throwable t) {
          errors.add(t);
        }
      }
    });

    f1.get();
    f2.get();

    // they should already be removed, but do it anyway
    h.removeHabitatListener(hl1);
    h.removeHabitatListener(hl2);
    
    if (!errors.isEmpty()) {
      throw new Exception("had multi-threaded errors: " + errors);
    }
  }

  /**
   * Look for concurrent modification errors.
   */
  public void testAsyncABunchOfTimes() throws Exception {
    // TODO: Expected to fail if concurrency controls are not enabled.
//    runTestAsyncABunchOfTimes(h);
    runTestAsyncABunchOfTimes(hc);
  }

  private void runTestAsyncABunchOfTimes(TestHabitat h) throws Exception {
    for (int i = 0; i < 200; i++) {
      runTestHabitatListenerAsyncRemoval(h);
    }
  }

  /**
   * No asserts, just checking for strange exceptions that might be thrown
   */
  public void testWeakProxyListener() throws Exception {
    runTestWeakProxyListener(h);
    runTestWeakProxyListener(hc);
  }
  
  private void runTestWeakProxyListener(TestHabitat h) {
    h.addHabitatListener(new HabitatListenerWeakProxy(new TestHabitatListener()));
    h.addComponent("test", this);
    System.gc();
    h.addComponent("test2", this);
  }

  /**
   * Verifies that an exception thrown in the listener doesn't affect callers into the habitat
   */
  public void testListenerExceptions() throws Exception {
    runTestListenerExceptions(h);
    runTestListenerExceptions(hc);
  }

  private void runTestListenerExceptions(TestHabitat h) {
    Logger logger = Logger.getLogger(Habitat.class.getName());
    Level prevLevel = logger.getLevel();
    logger.setLevel(Level.SEVERE);
    try {
      TestHabitatListener hl = new TestHabitatListener(new RuntimeException("forced"));
      h.addHabitatListener(hl);
      hl.calls.clear();
      
      h.addComponent("test", this);
      assertEquals("calls", 1, hl.calls.size());
      hl.calls.clear();
  
      Inhabitant entry = new ExistingSingletonInhabitant<TestCase>(TestCase.class, this);
      h.addIndex(entry, "index", "name");
      assertEquals("calls for index", 1, hl.calls.size());
      assertCall(h, hl.calls.get(0), EventType.INHABITANT_INDEX_ADDED, this, "index", "name");
    } finally {
      logger.setLevel(prevLevel);
    }
  }

  /**
   * Verifies that the listeners are actually managed from the habitat
   */
  public void testListenersLiveInHabitat() throws Exception {
    TestHabitatListener hl = new TestHabitatListener(1);
    h.addHabitatListener(hl);
    Collection<HabitatListener> coll = h.getAllByType(HabitatListener.class);
    assertNotNull(coll);
    assertFalse("shouldn't be empty but was", coll.isEmpty());
    assertTrue("expected listener to be found in habitat: " + hl + " in " + coll,
        coll.contains(hl));

    // stimulate a removal
    h.addComponent("test", this);
    
    coll = h.getAllByType(HabitatListener.class);
    assertFalse("expected to have been removed: " + coll, coll.contains(hl));
  }


  public static void assertCall(Habitat h, Call call, EventType expEventType, Object expObj,
      String expIndex, String expName) {
    assertSame("habitat", h, call.h);
    assertSame("object", expObj, call.obj.get());
    assertEquals("event type", expEventType, call.eventType);
    assertEquals("index", expIndex, call.index);
    assertEquals("name", expName, call.name);
  }

  @FactoryFor(Object.class)
  class TestA extends ExistingSingletonInhabitant {
    public TestA(Object object) {
      super(object);
    }
    
    @Override
    public Class type() {
      return TestA.class;
    }
  };

}
