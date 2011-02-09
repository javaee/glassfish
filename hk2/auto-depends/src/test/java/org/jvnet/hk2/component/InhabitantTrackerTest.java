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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.jvnet.hk2.component.Inhabitant;
import org.jvnet.hk2.component.InhabitantTracker;
import org.jvnet.hk2.component.InhabitantTrackerContext;
import org.jvnet.hk2.component.InhabitantTrackerContextBuilder;

import com.sun.hk2.component.ExistingSingletonInhabitant;

import junit.framework.Test;
import junit.framework.TestCase;

/**
 * Inhabitant Tracker Tests.
 * 
 * @author Jeff Trent
 */
// TODO: Settle on concurrency controls enabled
public class InhabitantTrackerTest extends TestCase {
  // concurrency controls disabled
  TestHabitat h = new TestHabitat(false);
  
  // concurrency controls enabled
  TestHabitat hc = new TestHabitat(true);
  
  public void testInitializationAndDestruction() throws Exception {
    try {
      fail("Exception expected: " + h.track(null, null));
    } catch (Exception e) {
      // expected
    }
    
    try {
      fail("Exception expected: " + h.trackFuture(null));
    } catch (Exception e) {
      // expected
    }

    InhabitantTrackerContextBuilder itcf = new InhabitantTrackerContextBuilder();
    TestCallback tc = new TestCallback();
    InhabitantTrackerContext itc = itcf.classNames(TestCase.class.getName()).build();
    InhabitantTracker it1 = h.track(itc, null);
    assertNotNull(it1);
    InhabitantTracker it2 = h.track(itc, tc);
    assertNotNull(it2);
    Future<InhabitantTracker> it3 = h.trackFuture(itc);
    assertNotNull(it3);
    Future<InhabitantTracker> it4 = h.trackFuture(itc);
    assertNotNull(it4);
    
    it1.release();
    it2.release();
    it3.get(0, TimeUnit.MILLISECONDS).release();
    it4.get(0, TimeUnit.MILLISECONDS).release();
  }
  
  public void testTrack_addAfterTracker() {
    runTestTrack_addAfterTracker(h);
    runTestTrack_addAfterTracker(hc);
  }
  
  @SuppressWarnings("unchecked")
  private void runTestTrack_addAfterTracker(TestHabitat h) {
    InhabitantTrackerContextBuilder itcf = new InhabitantTrackerContextBuilder();
    InhabitantTracker it = h.track(itcf.classNames(TestCase.class.getName()).build(), null);

    ExistingSingletonInhabitant i = new ExistingSingletonInhabitant(TestCase.class, this);
    h.add(i);
    h.addIndex(i, TestCase.class.getName(), null);

    assertSame(i, it.getInhabitant());
    assertEquals(1, it.getInhabitants().size());
    assertSame(i, it.getInhabitants().iterator().next());
    
    it.release();

    assertEquals(null, it.getInhabitant());
    assertEquals(Collections.EMPTY_SET, it.getInhabitants());
  }

  public void testTrack_addBeforeTracker() {
    runTestTrack_addBeforeTracker(h);
    runTestTrack_addBeforeTracker(hc);
  }
  
  @SuppressWarnings("unchecked")
  private void runTestTrack_addBeforeTracker(TestHabitat h) {
    ExistingSingletonInhabitant i = new ExistingSingletonInhabitant(TestCase.class, this);
    h.add(i);
    h.addIndex(i, TestCase.class.getName(), null);

    InhabitantTrackerContextBuilder itcf = new InhabitantTrackerContextBuilder();
    InhabitantTracker it = h.track(itcf.classNames(TestCase.class.getName()).build(), null);
    assertNotNull(it);

    assertSame(i, it.getInhabitant());
    assertEquals(1, it.getInhabitants().size());
    assertSame(i, it.getInhabitants().iterator().next());
  }

  public void testTrack_callback() {
    runTestTrack_callback(h);
    runTestTrack_callback(hc);
  }

  @SuppressWarnings("unchecked")
  private void runTestTrack_callback(TestHabitat h) {
    InhabitantHandle handle = InhabitantHandlerImpl.create(h, 
        true, this, null, null, TestCase.class.getName());
    InhabitantHandle handle2 = InhabitantHandlerImpl.create(h,
        true, this, null, null, Test.class.getName());
    
    TestCallback callback_anypresence = new TestCallback();
    InhabitantTrackerContextBuilder itcf_anypresence = 
      new InhabitantTrackerContextBuilder().classNames(TestCase.class.getName());
    TestCallback callback_presence = new TestCallback();
    InhabitantTrackerContextBuilder itcf_presence =
      new InhabitantTrackerContextBuilder().presence(true).classNames(TestCase.class.getName());
    TestCallback callback_nopresence = new TestCallback();
    InhabitantTrackerContextBuilder itcf_nopresence = 
      new InhabitantTrackerContextBuilder().presence(false).classNames(TestCase.class.getName());
 
    InhabitantTracker it_any = h.track(
        itcf_anypresence.classNames(TestCase.class.getName()).build(), callback_anypresence);
    assertNotNull(it_any);
    InhabitantTracker it_presence = h.track(
        itcf_presence.classNames(TestCase.class.getName()).build(), callback_presence);
    assertNotNull(it_any);
    InhabitantTracker it_nopresence = h.track(
        itcf_nopresence.classNames(TestCase.class.getName()).build(), callback_nopresence);
    assertNotNull(it_any);
    
    assertEquals("callback calls", 1, callback_anypresence.calls);
    assertEquals("callback calls", 1, callback_presence.calls);
    assertEquals("callback calls", 0, callback_nopresence.calls);

    handle.release();
    
    assertEquals("callback calls", 2, callback_anypresence.calls);
    assertEquals("callback calls", 1, callback_presence.calls);
    assertEquals("callback calls", 1, callback_nopresence.calls);
    
    // This is for "Test.class" which should have no affect
    handle2.release();
    assertEquals("callback calls", 2, callback_anypresence.calls);
    assertEquals("callback calls", 1, callback_presence.calls);
    assertEquals("callback calls", 1, callback_nopresence.calls);

    it_any.release();
    it_presence.release();
    it_nopresence.release();
  }

  @SuppressWarnings("static-access")
  public void testTrack_futurePositivePresence() throws Exception {
    TestHabitat h = new TestHabitat();
    InhabitantHandle<?> handle = InhabitantHandlerImpl.create(h,
        false, this, null, null, TestCase.class.getName());
    InhabitantTrackerContextBuilder itcf = new InhabitantTrackerContextBuilder();
    Future<InhabitantTracker> f = h.trackFuture(
        itcf.classNames(TestCase.class.getName()).presence(true).build());
    assertNull(f.get(10, TimeUnit.MILLISECONDS));
    assertFalse(f.isDone());

    handle.commit();
    Thread.currentThread().sleep(100);  // need to give release some time
    assertNotNull(f.get(100, TimeUnit.MILLISECONDS));
    assertNotNull(f.get().getInhabitant());
    assertTrue(f.isDone());
    assertEquals(1, f.get().getInhabitants().size());

    handle.release();
    Thread.currentThread().sleep(100);  // need to give release some time
    assertNull(f.get(0, TimeUnit.MILLISECONDS));
    assertFalse(f.isDone());

    handle.commit();
    Thread.currentThread().sleep(100);  // need to give release some time
    assertNotNull(f.get(10, TimeUnit.MILLISECONDS));
    assertEquals(1, f.get().getInhabitants().size());
    assertTrue(f.isDone());
  }
  
  @SuppressWarnings("static-access")
  public void testTrack_futureNegativePresence() throws Exception {
    TestHabitat h = new TestHabitat();
    InhabitantHandle<?> handle = InhabitantHandlerImpl.create(h,
        false, this, null, null, TestCase.class.getName());
    InhabitantTrackerContextBuilder itcf = new InhabitantTrackerContextBuilder();
    Future<InhabitantTracker> f = h.trackFuture(
        itcf.classNames(TestCase.class.getName()).presence(false).build());
    assertNotNull(f.get(10, TimeUnit.MILLISECONDS));
    assertTrue(f.isDone());

    handle.commit();
    Thread.currentThread().sleep(100);  // need to give release some time
    assertNull(f.get(100, TimeUnit.MILLISECONDS));
    assertFalse(f.isDone());

    handle.release();
    Thread.currentThread().sleep(100);  // need to give release some time
    assertNotNull(f.get(10, TimeUnit.MILLISECONDS));
    assertTrue(f.isDone());
  }

  @SuppressWarnings("static-access")
  public void testTrack_futureAnyPresence() throws Exception {
    TestHabitat h = new TestHabitat();
    InhabitantHandle<?> handle = InhabitantHandlerImpl.create(h,
        false, this, null, null, TestCase.class.getName());
    InhabitantTrackerContextBuilder itcf = new InhabitantTrackerContextBuilder();
    Future<InhabitantTracker> f = h.trackFuture(
        itcf.classNames(TestCase.class.getName()).build());
    assertNotNull(f.get(10, TimeUnit.MILLISECONDS));
    assertTrue(f.isDone());

    handle.commit();
    Thread.currentThread().sleep(100);  // need to give release some time
    assertNotNull(f.get(100, TimeUnit.MILLISECONDS));
    assertNotNull(f.get().getInhabitant());
    assertTrue(f.isDone());
    assertEquals(1, f.get().getInhabitants().size());

    handle.release();
    Thread.currentThread().sleep(100);  // need to give release some time
    assertNotNull(f.get(10, TimeUnit.MILLISECONDS));
    assertTrue(f.isDone());

    handle.commit();
    Thread.currentThread().sleep(100);  // need to give release some time
    assertNotNull(f.get(10, TimeUnit.MILLISECONDS));
    assertEquals(1, f.get().getInhabitants().size());
    assertTrue(f.isDone());
  }

  public void testTrack_isLazyListener() throws Exception {
    TestHabitat h = new TestHabitat();
    InhabitantTrackerContextBuilder itcf = new InhabitantTrackerContextBuilder();
    Future<InhabitantTracker> f = h.trackFuture(
        itcf.classNames(TestCase.class.getName()).presence(true).build());

    assertEquals(InhabitantTrackerJob.class, f.getClass());
    InhabitantTrackerJob itj = (InhabitantTrackerJob)f;
    assertNull("expect lazy listener / tracker", itj.it);
    assertFalse(itj.isDone());
    assertNotNull("expect lazy listener / tracker", itj.it);
    itj.cancel(false);

    Future<InhabitantTracker> f2 = h.trackFuture(
        itcf.classNames(TestCase.class.getName()).presence(true).build());
    assertNotSame(f, f2);
    itj = (InhabitantTrackerJob)f2;
    assertNull("expect lazy listener / tracker", itj.it);
    itj.cancel(false);
    assertNull("expect lazy listener / tracker", itj.it);
  }
  
  public void testTrack_withServiceRanking() throws Exception {
    testTrack_withServiceRanking(h);
    testTrack_withServiceRanking(hc);
  }

  @SuppressWarnings("unused")
  private void testTrack_withServiceRanking(TestHabitat h) {
    Map<String, Object> props = new HashMap<String, Object>();
    
    props.put(Constants.SERVICE_RANKING, 1);
    InhabitantHandle<?> h1 = 
      InhabitantHandlerImpl.create(h, true, this, null, props, Test.class.getName());
    
    props.put(Constants.SERVICE_RANKING, 100);
    InhabitantHandle<?> h2 = 
      InhabitantHandlerImpl.create(h, true, this, null, props, Test.class.getName());
    
    props.put(Constants.SERVICE_RANKING, 2);
    InhabitantHandle<?> h3 = 
      InhabitantHandlerImpl.create(h, true, this, null, props, Test.class.getName());
    
    InhabitantTrackerContextBuilder itcf = new InhabitantTrackerContextBuilder();
    InhabitantTrackerContext itc = itcf.classNames(Test.class.getName()).build();
    InhabitantTracker it = h.track(itc, null);
    Inhabitant<?> res = it.getInhabitant();
    assertNotNull(res);
    assertEquals("ranking inhabitant", h2.getInhabitant(), res);
  }
  

}
