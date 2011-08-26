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
package org.jvnet.hk2.component.internal.runlevel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.hk2.Binding;
import org.glassfish.hk2.ManagedComponentProvider;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.RunLevel;
import org.jvnet.hk2.component.AbstractRunLevelService;
import org.jvnet.hk2.component.ComponentException;
import org.jvnet.hk2.component.DescriptorImpl;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.component.Inhabitant;
import org.jvnet.hk2.component.InhabitantActivator;
import org.jvnet.hk2.component.InhabitantSorter;
import org.jvnet.hk2.component.Reference;
import org.jvnet.hk2.component.RunLevelListener;
import org.jvnet.hk2.component.RunLevelService;
import org.jvnet.hk2.component.RunLevelState;
import org.jvnet.hk2.component.ServiceContext;
import org.jvnet.hk2.junit.Hk2Runner;
import org.jvnet.hk2.junit.Hk2RunnerOptions;
import org.jvnet.hk2.test.runlevel.AHolderBasedRegularService1;
import org.jvnet.hk2.test.runlevel.AHolderBasedServerService1;
import org.jvnet.hk2.test.runlevel.AHolderBasedServerService2;
import org.jvnet.hk2.test.runlevel.ANonDefaultEnvServerService;
import org.jvnet.hk2.test.runlevel.AnotherNonDefaultEnvServerService;
import org.jvnet.hk2.test.runlevel.AnotherNonDefaultRunLevelService;
import org.jvnet.hk2.test.runlevel.ExceptionRunLevelManagedService;
import org.jvnet.hk2.test.runlevel.ExceptionRunLevelManagedService2b;
import org.jvnet.hk2.test.runlevel.InitService1;
import org.jvnet.hk2.test.runlevel.InitService2;
import org.jvnet.hk2.test.runlevel.InterruptRunLevelManagedService1a;
import org.jvnet.hk2.test.runlevel.InterruptRunLevelManagedService2b;
import org.jvnet.hk2.test.runlevel.NonRunLevelWithRunLevelDepService;
import org.jvnet.hk2.test.runlevel.OptionalRunLevelTstEnv;
import org.jvnet.hk2.test.runlevel.RunLevelContract;
import org.jvnet.hk2.test.runlevel.RunLevelServiceBase;
import org.jvnet.hk2.test.runlevel.RunLevelServiceNegOne;
import org.jvnet.hk2.test.runlevel.ServiceA;
import org.jvnet.hk2.test.runlevel.ServiceB;
import org.jvnet.hk2.test.runlevel.ServiceC;
import org.jvnet.hk2.test.runlevel.ShouldBeActivateable1;
import org.jvnet.hk2.test.runlevel.ShouldNotBeActivateable1;
import org.jvnet.hk2.test.runlevel.TestRunLevelListener;

import com.sun.hk2.component.AbstractInhabitantImpl;
import com.sun.hk2.component.ExistingSingletonInhabitant;

/**
 * Testing around the default RunLevelService impl.
 * 
 * @author Jeff Trent
 */
@SuppressWarnings("rawtypes")
@RunWith(Hk2Runner.class)
@Hk2RunnerOptions(reinitializePerTest=true)
public class RunLevelServiceTest {

  @Inject
  Habitat h;
  
  @Inject(name="default")
  RunLevelService<?> rls;
  
  @Inject
  RunLevelListener listener;

  private TestRunLevelListener defRLlistener;
  
  private HashMap<Integer, Recorder> recorders;

  private DefaultRunLevelService defRLS;

  
  /**
   * Verifies the state of the habitat
   */
  @Test
  public void validInitialHabitatState() {
    Collection<RunLevelListener> coll1 = h.getAllByContract(RunLevelListener.class);
    assertNotNull(coll1);
    assertEquals(1, coll1.size());
    assertSame(listener, coll1.iterator().next());
    assertTrue(coll1.iterator().next() instanceof TestRunLevelListener);
    
    Collection<RunLevelService> coll2 = h.getAllByContract(RunLevelService.class);
    assertNotNull(coll2);
    assertTrue(coll2.toString(), coll2.size() > 2);
    
    RunLevelService rls = h.getComponent(RunLevelService.class);
    assertNotNull(rls);
    assertNotNull(rls.getState());
    assertEquals(-1, rls.getState().getCurrentRunLevel());
    assertEquals(null, rls.getState().getPlannedRunLevel());
    assertEquals(DefaultRunLevelService.DEFAULT_SCOPE.getName(), rls.getState().getScopeName());
    
    RunLevelService rls2 = h.getComponent(RunLevelService.class, "default");
    assertSame(rls, rls2);
    assertSame(this.rls, rls);
    assertTrue(rls instanceof DefaultRunLevelService);
  }
  
  /**
   * The Init-based services exercises inheritance-based RunLevel startup
   * with @RunLevel(-1) instead of using @Immediate.
   */
  @Ignore(/* ignored because annotation @RunLevel currently does not get inherited on the Service) */)
  @Test
  public void initBasedServiceUsingRunLevelDirectly() {
      DescriptorImpl descriptor = new DescriptorImpl(InitService2.class);
      Collection<Binding<?>> bindings = h.getBindings(descriptor);
      assertEquals(bindings.toString(), 1, bindings.size());
      ManagedComponentProvider<?> provider = 
          (ManagedComponentProvider<?>) bindings.iterator().next().getProvider();
      assertTrue("expected active: " + provider, provider.isActive());
  }
  
  /**
   * The Init-based services exercises inheritance-based RunLevel startup
   * combined with @Immediate (kernel level) startup.
   */
  @Ignore(/* ignored because meta-annotation @Immediate currently does not get inherited on the Service) */)
  @Test
  public void initBasedServiceUsingImmediate() {
      DescriptorImpl descriptor = new DescriptorImpl(InitService1.class);
      Collection<Binding<?>> bindings = h.getBindings(descriptor);
      assertEquals(bindings.toString(), 1, bindings.size());
      ManagedComponentProvider<?> provider = 
          (ManagedComponentProvider<?>) bindings.iterator().next().getProvider();
      assertTrue("expected active: " + provider, provider.isActive());
  }
  
  /**
   * Verifies that RunLevel -1 inhabitants are created immediately
   */
  @Test
  public void validateRunLevelNegOneInhabitants() {
    assertTrue(h.isInitialized());
    Inhabitant<RunLevelServiceNegOne> i = h.getInhabitantByType(RunLevelServiceNegOne.class);
    assertNotNull(i);
    assertTrue(i.toString() + "expected to have been instantiated", i.isActive());
  }
  
  /**
   * verifies that RunLevel metadata exists on the inhabitants (using classmodel introspection)
   */
  @Test
  public void inhabitantMetaDataIncludesRunLevel() {
    Iterable<Inhabitant<?>> inhabs = h.getInhabitantsByAnnotation(RunLevel.class, null);
    assertNotNull(inhabs);
    int count = 0;
    for (Inhabitant<?> i : inhabs) {
      count++;
      assertNotNull(i.metadata());
      String val = i.metadata().getOne("runLevel");
      assertNotNull(i.toString(), val);
      assertTrue(i + " runLevel val=" + val, Integer.valueOf(val) >= RunLevel.KERNEL_RUNLEVEL);
    }
    assertTrue(String.valueOf(count), count >= 5);
  }

  /**
   * Garbage in produces garbage out (by way of exception) in proceedTo() operations.
   */
  @Test
  public void proceedToInvalidNegNum() {
    try {
      rls.proceedTo(-2);
      fail("Expected -1 to be a problem");
    } catch (IllegalArgumentException e) {
      // expected
    }
  }
  
  /**
   * There should be no runlevel services at this level in the current set of test material.
   */
  @Test
  public void proceedTo0() {
    installTestRunLevelService(false);
    rls.proceedTo(0);
    assertEquals(recorders.toString(), 0, recorders.size());
    assertEquals(0, defRLS.getCurrentRunLevel());
    assertEquals(null, defRLS.getPlannedRunLevel());
  }
  
  /**
   * Proceed to level 5, and ensure we have the right # of services that were started.
   */
  @Test
  public void proceedUpTo5_basics() {
    installTestRunLevelService(false);
    rls.proceedTo(5);
    assertEquals(1, recorders.size());
    Recorder recorder = recorders.get(5);
    assertNotNull(recorder);

    assertEquals(5, defRLS.getCurrentRunLevel());
    assertEquals(null, defRLS.getPlannedRunLevel());
  }

  /**
   * Do the same thing, but in async mode.
   */
  @Ignore
  @Test
  public void proceedUpTo5Async() throws InterruptedException {
    installTestRunLevelService(true);
    rls.proceedTo(5);
    assertEquals(5, defRLS.getPlannedRunLevel());
    Integer tmp = defRLS.getCurrentRunLevel();
    synchronized (rls) {
      rls.wait(1000);
    }
    assertTrue("too fast!", (null == tmp ? -1 : tmp) < 5);

    assertEquals(1, recorders.size());
    Recorder recorder = recorders.get(5);
    assertNotNull(recorder);

    assertEquals(5, defRLS.getCurrentRunLevel());
    assertEquals(null, defRLS.getPlannedRunLevel());
    
    assertInhabitantsState(5);
    assertListenerState(false, true, false);
    assertRecorderState();
  }
  
  @Test
  public void proceedUpTo10() {
    installTestRunLevelService(false);
    rls.proceedTo(10);
    assertEquals(null, defRLS.getPlannedRunLevel());

    assertEquals(recorders.toString(), 2, recorders.size());
    Recorder recorder = recorders.get(5);
    assertNotNull(recorder);

    recorder = recorders.get(10);
    assertNotNull(recorder);
    
    assertEquals(10, defRLS.getCurrentRunLevel());
    assertEquals(null, defRLS.getPlannedRunLevel());

    assertInhabitantsState(10);
    assertListenerState(false, true, false);
    assertRecorderState();
  }
  
  @Test
  public void proceedUpTo49() throws InterruptedException {
    installTestRunLevelService(false);
    rls.proceedTo(49);
    assertEquals(null, defRLS.getPlannedRunLevel());

    assertEquals(recorders.toString(), 3, recorders.size());
    Recorder recorder = recorders.get(5);
    assertNotNull(recorder);

    recorder = recorders.get(10);
    assertNotNull(recorder);
    
    recorder = recorders.get(20);
    assertNotNull(recorder);

    assertEquals(49, defRLS.getCurrentRunLevel());
    assertEquals(null, defRLS.getPlannedRunLevel());

    assertInhabitantsState(49);
    assertListenerState(false, true, false);
    assertRecorderState();
  }

  @Test
  public void proceedUpTo49ThenDownTo11() {
    installTestRunLevelService(false);
    rls.proceedTo(49);
    assertInhabitantsState(49);
    rls.proceedTo(11);
    assertEquals(null, defRLS.getPlannedRunLevel());

    assertEquals(3, recorders.size());
    Recorder recorder = recorders.get(5);
    assertNotNull(recorder);
    recorder = recorders.get(10);
    assertNotNull(recorder);

    assertEquals(11, defRLS.getCurrentRunLevel());
    assertEquals(null, defRLS.getPlannedRunLevel());

    assertInhabitantsState(11);
    assertListenerState(true, true, false);
    assertRecorderState();
  }
  
  @Ignore // TODO: has been intermittently failing
  @Test
  public void proceedUpTo49ThenDownTo11Async() throws InterruptedException {
    installTestRunLevelService(true);
    
    rls.proceedTo(49);
    rls.proceedTo(11);
    
    assertEquals(11, defRLS.getPlannedRunLevel());
    
    synchronized (rls) {
      rls.wait(1000);
    }
    assertEquals(11, defRLS.getCurrentRunLevel());

    assertEquals(2, recorders.size());
    Recorder recorder = recorders.get(5);
    assertNotNull(recorder);
    recorder = recorders.get(10);
    assertNotNull(recorder);

    assertEquals(11, defRLS.getCurrentRunLevel());
    assertEquals(null, defRLS.getPlannedRunLevel());

    assertInhabitantsState(11);
    assertListenerState(true, true, false);
    assertRecorderState();
  }
  
  @Test
  public void proceedUpTo49ThenDownTo11ThenDownToZero() {
    installTestRunLevelService(false);
    rls.proceedTo(49);
    rls.proceedTo(11);
    rls.proceedTo(0);
    assertEquals(null, defRLS.getPlannedRunLevel());

    assertEquals(3, recorders.size());

    assertInhabitantsState(0);
    assertListenerState(true, true, false);
//    assertRecorderState();
  }
  
  /**
   * This guy tests the underling Recorder Ordering.
   * 
   * Note that: ServiceA -> ServiceB -> ServiceC
   * 
   * So, if we active B, A, then C manually, we still expect
   * the recorder to have A, B, C only in that order.
   * 
   * This takes some "rigging" on the runLevelService (to ignore proceedTo)
   * @throws NoSuchFieldException 
   * @throws Exception 
   */
  @Test
  public void serviceABC_startUp_and_shutDown() throws Exception {
    installTestRunLevelService(false);
    
    RunLevelServiceBase.count = 0;
    rls.proceedTo(9);
    assertEquals(0, RunLevelServiceBase.count);

    recorders.clear();

    RunLevelServiceBase.count = 0;
    rls.proceedTo(10);
    
    assertNotNull(h.getComponent(ServiceB.class));
    assertNotNull(h.getComponent(ServiceA.class));
    assertNotNull(h.getComponent(ServiceC.class));

    assertEquals(recorders.toString(), 1, recorders.size());
    assertEquals("count", 3, RunLevelServiceBase.count);

    Recorder recorder = recorders.get(10);
    assertNotNull(recorder);

    List<Inhabitant<?>> activations = recorder.getActivations();
    assertFalse("activations empty", activations.isEmpty());
    try {
      Iterator<Inhabitant<?>> iter = activations.iterator();
      iter.remove();
      fail("expected read-only collection");
    } catch (UnsupportedOperationException e) {
      // expected
    }
    
    activations = new ArrayList<Inhabitant<?>>(activations);
    Iterator<Inhabitant<?>> iter = activations.iterator();
    while (iter.hasNext()) {
      Inhabitant<?> i = iter.next();
      if (!i.typeName().contains(".RunLevelService")) {
        iter.remove();
      }
    }
    assertEquals("activations: " + activations, 3, activations.size());
    
    Inhabitant<?> iB = h.getInhabitantByContract(ServiceB.class.getName(), null);
    Inhabitant<?> iA = h.getInhabitantByContract(ServiceA.class.getName(), null);
    Inhabitant<?> iC = h.getInhabitantByContract(ServiceC.class.getName(), null);

    assertTrue(iB.isActive());
    assertTrue(iA.isActive());
    assertTrue(iC.isActive());
    
    iter = activations.iterator();
    assertSame("order is important", iC, iter.next());
    assertSame("order is important", iB, iter.next());
    assertSame("order is important", iA, iter.next());

    RunLevelServiceBase a = (RunLevelServiceBase) iA.get();
    RunLevelServiceBase b = (RunLevelServiceBase) iB.get();
    RunLevelServiceBase c = (RunLevelServiceBase) iC.get();

    RunLevelServiceBase.count = 0;
    defRLS.proceedTo(0);
    assertFalse(iB.isActive());
    assertFalse(iA.isActive());
    assertFalse(iC.isActive());

    assertEquals(recorders.toString(), 1, recorders.size());
    assertNotNull(recorders.toString(), recorders.get(10));
    assertTrue(recorders.toString(), recorders.get(10).getActivations().isEmpty());

    assertEquals("count", 3, RunLevelServiceBase.count);
    assertEquals("order is important on shutdown too: A", 0, a.countStamp);
    assertEquals("order is important on shutdown too: B", 1, b.countStamp);
    assertEquals("order is important on shutdown too: C", 2, c.countStamp);

    assertListenerState(true, false, false);
  }
 
  /**
   * Dependencies from from a non-RLS-annotated service to a RLS service is a violation.
   */
  @Test
  public void dependenciesFromNonRunLevelToRunLevelService() {
    rls.proceedTo(10);
  
    Inhabitant<NonRunLevelWithRunLevelDepService> i = 
      h.getInhabitantByType(NonRunLevelWithRunLevelDepService.class);
    assertNotNull(i);
    assertFalse(i.isActive());
    
    try {
      fail("Expected get() to fail, bad dependency to a RunLevel service: " + i.get());
    } catch (Exception e) {
      // expected
    }

    assertFalse(i.isActive());
  }
  
  @Test
  public void dependenciesFromNonRunLevelToRunLevelServiceAsync() {
    installTestRunLevelService(true);
    
    defRLS.proceedTo(10);
  
    Inhabitant<NonRunLevelWithRunLevelDepService> i = 
      h.getInhabitantByType(NonRunLevelWithRunLevelDepService.class);
    assertNotNull(i);
    assertFalse(i.isActive());
    
    try {
      fail("Expected get() to fail, bad dependency to a RunLevel service: " + i.get());
    } catch (Exception e) {
      // expected
    }

    assertFalse(i.isActive());
  }
  
  /**
   * Verifies the behavior of an OnProgress recipient, calling proceedTo()
   */
  @Test
  public void chainedStartupProceedToCalls() throws Exception {
    installTestRunLevelService(false);

    defRLlistener.setProgressProceedTo(1, 4, rls);
    
    rls.proceedTo(1);
//    synchronized (rls) {
//      rls.wait(1000);
//    }
    assertEquals(4, defRLS.getCurrentRunLevel());
    assertEquals(null, defRLS.getPlannedRunLevel());

    assertInhabitantsState(4);
    assertListenerState(true, false, false);
  }
  
  /**
   * Verifies the behavior of an OnProgress recipient, calling proceedTo()
   */
  @Test
  public void chainedStartupProceedToCallsAsync() throws Exception {
    installTestRunLevelService(true);
    
    defRLlistener.setProgressProceedTo(1, 4, rls);
    
    rls.proceedTo(1);
    synchronized (rls) {
      rls.wait(1000);
    }
    if (4 != defRLS.getCurrentRunLevel()) {
      synchronized (rls) {
        rls.wait(100);
      }
    }
    
    assertEquals(4, defRLS.getCurrentRunLevel());
    assertEquals(null, defRLS.getPlannedRunLevel());

    assertInhabitantsState(4);
    assertListenerState(true, false, false);
  }
  
  /**
   * Verifies the behavior of an OnProgress recipient, calling proceedTo()
   */
  @Test
  public void chainedShutdownProceedToCalls() throws Exception {
    installTestRunLevelService(false);

    defRLlistener.setProgressProceedTo(4, 0, rls);
    rls.proceedTo(4);
    
    assertEquals(0, defRLS.getCurrentRunLevel());
    assertEquals(null, defRLS.getPlannedRunLevel());

    assertInhabitantsState(4);
    assertListenerState(true, false, false);
  }
  
  /**
   * Verifies the behavior of an OnProgress recipient, calling proceedTo()
   */
  @Test
  public void chainedShutdownProceedToCallsAsync() throws Exception {
    installTestRunLevelService(true);

    defRLlistener.setProgressProceedTo(4, 0, rls);
    rls.proceedTo(4);
    
    synchronized (rls) {
      rls.wait(1000);
    }
    if (defRLS.getCurrentRunLevel() > 0) {
      synchronized (rls) {
        rls.wait(100);
      }
    }
    
    assertEquals(0, defRLS.getCurrentRunLevel());
    assertEquals(null, defRLS.getPlannedRunLevel());

    assertInhabitantsState(4);
    assertListenerState(true, false, false);
  }

  /**
   * RLS supports other subcomponent lifecycle through what is known as
   * "run level scopes".  This tests another run level scope startup.
   * 
   * @throws Exception
   */
  @Test
  public void exceptionTypeEnvRunLevelService() throws Exception {
    installTestRunLevelService(false);
    
    this.defRLlistener = (TestRunLevelListener) listener;
    defRLlistener.calls.clear();

    rls = new TestDefaultRunLevelService(h, false, Exception.class, recorders); 

    ExceptionRunLevelManagedService.constructCount = 0;
    
    rls.proceedTo(1);
    
    assertEquals(1, ExceptionRunLevelManagedService.constructCount);
    assertEquals(0, ExceptionRunLevelManagedService.destroyCount);
    assertEquals(defRLlistener.calls.toString(), 3, defRLlistener.calls.size());
    assertListenerState(false, false, false);
  }
  
  @Test
  public void exceptionsEncounteredOnUpSide() throws Exception {
    this.defRLlistener = (TestRunLevelListener) listener;
    defRLlistener.calls.clear();

    recorders = new LinkedHashMap<Integer, Recorder>();
    rls = new TestDefaultRunLevelService(h, false, Exception.class, recorders); 

    ExceptionRunLevelManagedService.exceptionCtor = 
      RuntimeException.class.getConstructor((Class<?>[])null);
    ExceptionRunLevelManagedService.constructCount = 0;
    
    rls.proceedTo(1);
    
    assertEquals(1, ExceptionRunLevelManagedService.constructCount);
    assertEquals(0, ExceptionRunLevelManagedService.destroyCount);
    assertEquals(defRLlistener.calls.toString(), 4, defRLlistener.calls.size());
    assertListenerState(false, true, false);
  }
  
  @Test
  public void exceptionsEncounteredOnDownSide() throws Exception {
    recorders = new LinkedHashMap<Integer, Recorder>();
    rls = new TestDefaultRunLevelService(h, false, Exception.class, recorders); 

    ExceptionRunLevelManagedService.constructCount = 0;
    ExceptionRunLevelManagedService.exceptionCtor = null;

    rls.proceedTo(5);

    assertEquals(1, ExceptionRunLevelManagedService.constructCount);
    
    this.defRLlistener = (TestRunLevelListener) listener;
    defRLlistener.calls.clear();

    ExceptionRunLevelManagedService.exceptionCtor = 
      RuntimeException.class.getConstructor((Class<?>[])null);
    ExceptionRunLevelManagedService.destroyCount = 0;
    
    rls.proceedTo(0);
    
    assertEquals(1, ExceptionRunLevelManagedService.destroyCount);
    assertEquals(defRLlistener.calls.toString(), 7, defRLlistener.calls.size());
    assertListenerState(true, true, false);
  }
  
  /**
   * Proceeds to level 5, encountering an exception along the way, and the onError
   * nests a call to proceedTo level 0.
   */
  @Test
  public void exceptionsEncounteredOnUpSideWithChainedShutdown() throws Exception {
    recorders = new LinkedHashMap<Integer, Recorder>();
    rls = defRLS = new TestDefaultRunLevelService(h, false, Exception.class, recorders); 

    this.defRLlistener = (TestRunLevelListener) listener;
    defRLlistener.calls.clear();
    defRLlistener.setErrorProceedTo(0, rls);

    ExceptionRunLevelManagedService.exceptionCtor = 
      RuntimeException.class.getConstructor((Class<?>[])null);
    ExceptionRunLevelManagedService.constructCount = 0;
    ExceptionRunLevelManagedService.destroyCount = 0;
    
    rls.proceedTo(5);
    
    assertEquals(1, ExceptionRunLevelManagedService.constructCount);
    assertEquals(0, ExceptionRunLevelManagedService.destroyCount);
    assertListenerState(false, true, false);

    assertEquals(0, defRLS.getCurrentRunLevel());
    assertEquals(null, defRLS.getPlannedRunLevel());
  }
  
  @Test
  public void testGetRecordersToRelease() {
    recorders = new LinkedHashMap<Integer, Recorder>();
    recorders.put(1, null);
    recorders.put(3, null);
    recorders.put(2, null);
    recorders.put(0, null);
    defRLS = new TestDefaultRunLevelService(h, false, Exception.class, recorders); 
    
    List<Integer> list = defRLS.getRecordersToRelease(recorders, 2);
    assertNotNull(list);
    assertEquals("size", 2, list.size());
    assertEquals(3, list.get(0));
    assertEquals(2, list.get(1));
  }
  
  /**
   * Proceeds to level 5, encountering an exception along the way, and the onError
   * nests a call to proceedTo level 1.  But the onCancelled event that gets called
   * after the onError also issues a proceedTo level 0.  The last proceedTo (in
   * this case onCancelled) should take precedence.
   */
  @Test
  public void exceptionsEncounteredOnUpSideWithOnError() throws Exception {
    recorders = new LinkedHashMap<Integer, Recorder>();
    rls = defRLS = new TestDefaultRunLevelService(h, false, Exception.class, recorders); 

    this.defRLlistener = (TestRunLevelListener) listener;
    defRLlistener.calls.clear();
    defRLlistener.setErrorProceedTo(1, rls);
    defRLlistener.setCancelProceedTo(0, rls);

    ExceptionRunLevelManagedService.exceptionCtor = 
      RuntimeException.class.getConstructor((Class<?>[])null);
    ExceptionRunLevelManagedService.constructCount = 0;
    ExceptionRunLevelManagedService.destroyCount = 0;
    
    rls.proceedTo(5);
    
    assertEquals("current run level", 0, defRLS.getCurrentRunLevel());
    assertEquals(null, defRLS.getPlannedRunLevel());
    
    assertEquals(1, ExceptionRunLevelManagedService.constructCount);
    assertEquals(0, ExceptionRunLevelManagedService.destroyCount);
    assertListenerState(false, true, true);
  }
  
  /**
   * Imagine the situation where you are at current run level 0, and you issue a proceedTo(2).
   * 
   * RunLevel 1 was successfully reached, but RunLevel 2 experienced an error in one of the
   * RunLevel annotated services for that run level (leaving it in an indeterminate state).
   * 
   * onError() calling proceedTo(1 --- the last good run level) should close down all services
   * at level 2 and above that were successfully started.
   */
  @Test
  public void exceptionsEncounteredOnUpSideWithChainedShutdownToLastGoodRunLevel() throws Exception {
    recorders = new LinkedHashMap<Integer, Recorder>();
    rls = defRLS = new TestDefaultRunLevelService(h, false, Exception.class, recorders); 

    this.defRLlistener = (TestRunLevelListener) listener;
    defRLlistener.calls.clear();
    defRLlistener.setErrorProceedTo(1, rls);

    ExceptionRunLevelManagedService2b.exceptionCtor = 
      RuntimeException.class.getConstructor((Class<?>[])null);
    ExceptionRunLevelManagedService2b.constructCount = 0;
    ExceptionRunLevelManagedService2b.destroyCount = 0;
    
    rls.proceedTo(2);
    
    assertEquals(1, defRLS.getCurrentRunLevel());
    assertEquals(null, defRLS.getPlannedRunLevel());
    
    Collection<Inhabitant<?>> coll = h.getInhabitantsByContract(RunLevelContract.class.getName());
    assertTrue(coll.size() >= 3);
    boolean gotOne = false;
    for (Inhabitant<?> i : coll) {
      String typeName = i.typeName();
      if (typeName.contains("ExceptionRunLevelManagedService2")) {
        gotOne = true;
        assertFalse("expected to be in released state: " + i, i.isActive());
      }
    }
    assertTrue(gotOne);
  }
  
  /**
   * This is testing the non-async version of RLS in the following scenario:
   * 
   *  - Thread #1 issues a proceedTo(2)
   *  - A service {@link InterruptRunLevelManagedService2b} hangs
   *  - Thread #2 (a watchdog thread) finds that Thread #1 is hung, and issues a proceedTo(1)
   */
  @SuppressWarnings("unchecked")
  @Test
  public void multiThreadedSoftInterrupt() throws Exception {
    recorders = new LinkedHashMap<Integer, Recorder>();
    rls = defRLS = new TestDefaultRunLevelService(h, false, String.class, recorders); 

    this.defRLlistener = (TestRunLevelListener) listener;
    defRLlistener.calls.clear();
    
    // we want 1a to not be involved in this test
    InterruptRunLevelManagedService1a.rls = null;

    InterruptRunLevelManagedService2b.i = 0;
    
    final List problems = new ArrayList();
    Thread watchDog = new Thread() {
      @Override
      public void run() {
        try {
          sleep(400);
          Logger.getAnonymousLogger().log(Level.INFO, "issuing proceedTo(1) interrupt from thread: " + this);
          // this will hang {@link InterruptRunLevelManagedService2b}
          rls.proceedTo(1);
        } catch (Exception e) {
          problems.add(e);
        }
      }
    };
    watchDog.start();
    
    Logger.getAnonymousLogger().log(Level.INFO, "issuing proceedTo(2) from main thread: " + this);
    // this main thread will be interrupted to go to 1 (see above)
    rls.proceedTo(2);
    
    watchDog.join();
    
    assertTrue("problems: " + problems, problems.isEmpty());
    assertEquals(1, defRLS.getCurrentRunLevel());
    assertEquals(null, defRLS.getPlannedRunLevel());
    assertTrue("hanging service not reached", InterruptRunLevelManagedService2b.i > 0);
    
    Collection<Inhabitant<?>> coll = h.getInhabitantsByContract(RunLevelContract.class.getName());
    assertTrue(coll.size() >= 3);
    boolean gotOne = false;
    boolean gotTwo = false;
    for (Inhabitant<?> i : coll) {
      String typeName = i.typeName();
      if (typeName.contains("InterruptRunLevelManagedService1")) {
        gotOne = true;
        assertTrue("expected to be in active state: " + i, i.isActive());
      }
      if (typeName.contains("InterruptRunLevelManagedService2")) {
        gotTwo = true;
        assertFalse("expected to be in released state: " + i, i.isActive());
      }
    }
    assertTrue(gotOne);
    assertTrue(gotTwo);

    assertNeverGotToRunLevel(2);
    
    assertListenerState(false, false, true);
  }
  
  /**
   * This is testing the non-async version of RLS in the following scenario:
   * 
   *  - Thread #1 issues a proceedTo(whatever)
   *  - One of the services activated {@link InterruptRunLevelManagedService1a} calls
   *      proceedTo(2)
   *  - A service {@link InterruptRunLevelManagedService2b} hangs
   *  - Thread #2 (a watchdog thread) finds that Thread #1 is hung, and issues a proceedTo(0)
   *  
   *  This has parallels to the Hard interrupt example
   *  
   *  @see #multiThreadedHardInterrupt2a()
   *  @see #multiThreadedHardInterrupt2b()
   */
  @SuppressWarnings("unchecked")
  @Test
  public void multiThreadedSoftInterrupt2() throws Exception {
    recorders = new LinkedHashMap<Integer, Recorder>();
    rls = defRLS = new TestDefaultRunLevelService(h, false, String.class, recorders); 

    this.defRLlistener = (TestRunLevelListener) listener;
    defRLlistener.calls.clear();
    
    InterruptRunLevelManagedService1a.rls = rls;
    InterruptRunLevelManagedService1a.swallowExceptionsInProceedTo = false;
    InterruptRunLevelManagedService2b.i = 0;
    
    final List problems = new ArrayList();
    Thread watchDog = new Thread() {
      @Override
      public void run() {
        try {
          sleep(400);
          Logger.getAnonymousLogger().log(Level.INFO, "issuing proceedTo(0) interrupt from thread: " + this);
          rls.proceedTo(0);
        } catch (Exception e) {
          problems.add(e);
        }
      }
    };
    watchDog.start();
    
    Logger.getAnonymousLogger().log(Level.INFO, "issuing proceedTo(1) from main thread: " + this);
    // this main thread will be interrupted to go to 0 but only after proceedTo(2) is sent (see InterruptRunLevelManagedService1a)
    rls.proceedTo(1);
    
    watchDog.join();
    
    assertTrue("problems: " + problems, problems.isEmpty());
    assertEquals(0, defRLS.getCurrentRunLevel());
    assertEquals(null, defRLS.getPlannedRunLevel());
    assertTrue("hanging service not reached", InterruptRunLevelManagedService2b.i > 0);
    
    Collection<Inhabitant<?>> coll = h.getInhabitantsByContract(RunLevelContract.class.getName());
    assertTrue(coll.size() >= 3);
    boolean gotOne = false;
    boolean gotTwo = false;
    for (Inhabitant<?> i : coll) {
      String typeName = i.typeName();
      if (typeName.contains("InterruptRunLevelManagedService1")) {
        gotOne = true;
        assertFalse("expected to be in released state: " + i, i.isActive());
      }
      if (typeName.contains("InterruptRunLevelManagedService2")) {
        gotTwo = true;
        assertFalse("expected to be in released state: " + i, i.isActive());
      }
    }
    assertTrue(gotOne);
    assertTrue(gotTwo);

    assertNeverGotToRunLevel(2);
    
    assertListenerState(true, false, 1, 0);
  }
  
  /**
   * This is testing the non-async version of RLS in the following scenario:
   * 
   *  - Thread #1 issues a proceedTo(whatever)
   *  - One of the services activated {@link InterruptRunLevelManagedService1a} calls
   *      proceedTo(2)
   *  - A service {@link InterruptRunLevelManagedService2b} hangs
   *  - Thread #2 (a watchdog thread) finds that Thread #1 is hung, and issues an interrupt(0)
   *  
   *  This has parallels to the Soft interrupt example.
   *  
   *  The {@link #multiThreadedHardInterrupt2b()} is the same, except for the different interrupt() called
   *  
   *  @see #multiThreadedSoftInterrupt2()
   */
  @SuppressWarnings("unchecked")
  @Test
  public void multiThreadedHardInterrupt2a() throws Exception {
    recorders = new LinkedHashMap<Integer, Recorder>();
    rls = defRLS = new TestDefaultRunLevelService(h, false, String.class, recorders); 

    this.defRLlistener = (TestRunLevelListener) listener;
    defRLlistener.calls.clear();
    
    InterruptRunLevelManagedService1a.rls = rls;
    InterruptRunLevelManagedService1a.swallowExceptionsInProceedTo = false;
    InterruptRunLevelManagedService2b.i = 0;
    
    final List problems = new ArrayList();
    Thread watchDog = new Thread() {
      @Override
      public void run() {
        try {
          sleep(400);
          Logger.getAnonymousLogger().log(Level.INFO, "issuing interrupt(0) from thread: " + this);
          rls.interrupt(0);
        } catch (Exception e) {
          problems.add(e);
        }
      }
    };
    watchDog.start();
    
    Logger.getAnonymousLogger().log(Level.INFO, "issuing proceedTo(1) from main thread: " + this);
    // this main thread will be interrupted to go to 0 but only after proceedTo(2) is sent (see InterruptRunLevelManagedService1a)
    rls.proceedTo(1);
    
    watchDog.join();
    
    assertTrue("problems: " + problems, problems.isEmpty());
    assertEquals(0, defRLS.getCurrentRunLevel());
    assertEquals(null, defRLS.getPlannedRunLevel());
    assertTrue("hanging service not reached", InterruptRunLevelManagedService2b.i > 0);
    
    Collection<Inhabitant<?>> coll = h.getInhabitantsByContract(RunLevelContract.class.getName());
    assertTrue(coll.size() >= 3);
    boolean gotOne = false;
    boolean gotTwo = false;
    for (Inhabitant<?> i : coll) {
      String typeName = i.typeName();
      if (typeName.contains("InterruptRunLevelManagedService1")) {
        gotOne = true;
        assertFalse("expected to be in released state: " + i, i.isActive());
      }
      if (typeName.contains("InterruptRunLevelManagedService2")) {
        gotTwo = true;
        assertFalse("expected to be in released state: " + i, i.isActive());
      }
    }
    assertTrue(gotOne);
    assertTrue(gotTwo);

    assertNeverGotToRunLevel(2);
    
    assertListenerState(true, false, 0, 1);
  }

  /**
   * This is testing the non-async version of RLS in the following scenario:
   * 
   *  - Thread #1 issues a proceedTo(whatever)
   *  - One of the services activated {@link InterruptRunLevelManagedService1a} calls
   *      proceedTo(2)
   *  - A service {@link InterruptRunLevelManagedService2b} hangs
   *  - Thread #2 (a watchdog thread) finds that Thread #1 is hung, and issues an interrupt() --- not interrupt(0)
   *  
   *  @see #multiThreadedHardInterrupt2a()
   *  @see #multiThreadedSoftInterrupt2()
   */
  @SuppressWarnings("unchecked")
  @Test
  public void multiThreadedHardInterrupt2b() throws Exception {
    recorders = new LinkedHashMap<Integer, Recorder>();
    rls = defRLS = new TestDefaultRunLevelService(h, false, String.class, recorders); 

    this.defRLlistener = (TestRunLevelListener) listener;
    defRLlistener.calls.clear();
    
    InterruptRunLevelManagedService1a.rls = rls;
    InterruptRunLevelManagedService1a.swallowExceptionsInProceedTo = false;
    InterruptRunLevelManagedService2b.i = 0;
    
    final List problems = new ArrayList();
    Thread watchDog = new Thread() {
      @Override
      public void run() {
        try {
          sleep(400);
          Logger.getAnonymousLogger().log(Level.INFO, "issuing interrupt() from thread: " + this);
          rls.interrupt();
        } catch (Exception e) {
          problems.add(e);
        }
      }
    };
    watchDog.start();
    
    Logger.getAnonymousLogger().log(Level.INFO, "issuing proceedTo(1) from main thread: " + this);
    // this main thread will be interrupted to go to 0 but only after proceedTo(2) is sent (see InterruptRunLevelManagedService1a)
    rls.proceedTo(1);
    
    watchDog.join();
    
    assertTrue("problems: " + problems, problems.isEmpty());
    assertEquals(2, defRLS.getCurrentRunLevel());
    assertEquals(null, defRLS.getPlannedRunLevel());
    assertTrue("hanging service not reached", InterruptRunLevelManagedService2b.i > 0);
    
    assertListenerState(true, false, 0, 1);
  }

  /**
   * This is testing the async version of RLS in the following scenario:
   * 
   *  - Thread #1 issues a proceedTo(whatever).  RLS will spawn a new thread
   *      that performs the "work" (see {@link DefaultRunLevelService#proceedToWorker} in impl).
   *      Let's call this new thread thread 1b.
   *  - One of the services activated in 1b, {@link InterruptRunLevelManagedService1a}, calls
   *      proceedTo(2). It also swallows all exceptions (e.g., any interrupt)!
   *  - A service {@link InterruptRunLevelManagedService2b} in RunLevel 2 hangs, but in
   *      a way that can't be interrupted!
   *  - Thread #2 (a watchdog thread) finds that Thread #1/1b is hung, and then
   *      issues a proceedTo(0)
   *  
   * The expected behavior for async is that the thread #1b is "orphaned" and thread #2
   * picks up the work.
   * 
   * We need to make sure that when thread 1b returns (if it returns) it does not continue
   * with the 1/1b proceedTo() --- so we break out of that loop, by being a RunLevelListener.
   * 
   * Important Note: this scenario could not readily be supported today in the sync case of the default RLS.
   * It may work, but the test is written is such a way (i.e., RunLevelListener on 2b) that this
   * becomes an invalid test for the sync case.
   */
  @SuppressWarnings({ "unchecked", "static-access" })
  @Test
  public void multiThreadedSoftInterrupt3Async() throws Exception {
    recorders = new LinkedHashMap<Integer, Recorder>();
    rls = defRLS = new TestDefaultRunLevelService(h, true, String.class, recorders); 

    this.defRLlistener = (TestRunLevelListener) listener;
    defRLlistener.calls.clear();
    
    InterruptRunLevelManagedService1a.rls = rls;
    InterruptRunLevelManagedService1a.swallowExceptionsInProceedTo = true;

    InterruptRunLevelManagedService2b.i = 0;
    InterruptRunLevelManagedService2b.doSleep = false;
    try {
      final List problems = new ArrayList();
      Thread watchDog = new Thread() {
        @Override
        public void run() {
          try {
            sleep(400);
            Logger.getAnonymousLogger().log(Level.INFO, "issuing proceedTo(0) interrupt from thread: " + this);
            // this will hang {@link InterruptRunLevelManagedService2b}
            rls.proceedTo(0);
            Logger.getAnonymousLogger().log(Level.INFO, "out of proceedTo(0) interrupt from thread: " + this);
          } catch (Exception e) {
            problems.add(e);
          }
        }
      };
      watchDog.setDaemon(true);
      watchDog.start();
      
      Logger.getAnonymousLogger().log(Level.INFO, "issuing proceedTo(1) from main thread: " + this);
      // this main thread will be interrupted to go to 0 but only after proceedTo(2) is sent (see above)
      rls.proceedTo(1);

      watchDog.join();

      if (null != rls.getState().getPlannedRunLevel()) {
        try {
          synchronized (rls) {
            rls.wait(100);
          }
        } catch (Exception e) {
          // eat it
        }
      }
      
      assertTrue("problems: " + problems, problems.isEmpty());
      assertEquals(0, defRLS.getCurrentRunLevel());
      assertEquals(null, defRLS.getPlannedRunLevel());
      assertTrue("hanging service not reached", InterruptRunLevelManagedService2b.i > 0);
      
      Collection<Inhabitant<?>> coll = h.getInhabitantsByContract(RunLevelContract.class.getName());
      assertTrue(coll.size() >= 3);
      boolean gotOne = false;
      boolean gotTwo = false;
      for (Inhabitant<?> i : coll) {
        String typeName = i.typeName();
        if (typeName.contains("InterruptRunLevelManagedService1")) {
          gotOne = true;
          assertFalse("expected to be in released state: " + i, i.isActive());
        }
        if (typeName.contains("InterruptRunLevelManagedService2")) {
          gotTwo = true;
          assertFalse("expected to be in released state: " + i, i.isActive());
        }
      }
      assertTrue(gotOne);
      assertTrue(gotTwo);
  
      assertNeverGotToRunLevel(2);
      
      assertListenerState(true, false, 2, 0);
    } finally {
      InterruptRunLevelManagedService2b.doSleep = true;
      InterruptRunLevelManagedService1a.swallowExceptionsInProceedTo = false;

      // get thread #1 unstuck and wait for completion (not really needed since its daemon)
      while (null == InterruptRunLevelManagedService2b.self) {
        Thread.currentThread().sleep(500);
        InterruptRunLevelManagedService2b.breakOut = true;
      }      
    }
  }
  
  @SuppressWarnings("unchecked")
  @Test
  public void inhabitantSorterAndActivatorPresent() {
    installTestRunLevelService(false);

    rls.proceedTo(0);

    TestInhabitantSorter is = new TestInhabitantSorter();
    ExistingSingletonInhabitant isi = new ExistingSingletonInhabitant(is);
    h.addIndex(isi, InhabitantSorter.class.getName(), null);
    
    TestInhabitantActivator ia = new TestInhabitantActivator();
    ExistingSingletonInhabitant iai = new ExistingSingletonInhabitant(ia);
    h.addIndex(iai, InhabitantActivator.class.getName(), null);
    
    rls.proceedTo(5);
    
    assertEquals("sort operations", 1, is.callCount);
    assertEquals("activation operations", 3, ia.activateCount);
    assertEquals("deactivation operations", 0, ia.releaseCount);

    rls.proceedTo(0);
    
    assertEquals("sort operations", 1, is.callCount);
    assertEquals("activation operations", 3, ia.activateCount);
    assertEquals("deactivation operations", 1, ia.releaseCount);
  }

  /**
   * This goes beyond the RunLevelService, but there is certainly
   * a component of RunLevelService to consider.
   * <p/>
   * The basic assertions are these:<br/>
   * (a) it's ok to use optional when there is no implementation
   * available for a given contract available in the system.<br/>
   * (b) you should get cascading UnsatisfiedDependencyExceptions
   * in all other cases should there be a failure during service
   * initialization (e.g., PostConstruct throwing RuntimeException).
   * <p/>
   * In addition to the habitat being in the right context, the 
   * RunLevelService listeners furthermore need to see the correct
   * number of errors during the injection process.
   * <p/>
   * In this test we use the following:<br/>
   * Contracts:<br/>
   *  ContractWithNoImplementers - no implementations present on platform<br/>
   *  ContractWithExceptionThrowingImplementers - implementations that throw exceptions during init<br/>
   *  ShouldBeActivateable1 - marker contract for checking the habitat after the fact to ensure that services can be activated<br/>
   *  ShouldNotBeActivable1 - same, but opposite, marker contract<br/>
   * <p/>
   * Implementations:<br/>
   *  ServiceImplOf_ContractWithException - singleton responsible for throwing the exception<br/>
   *  ServiceClientOf_ContractWithNoImplementers - client with an optional depency to a service that doesn't exist - should be fine<br/>
   *  ServiceClientOf_ContractWithExceptionThrowingImplementers - client with an optional dependency to a service that throws - should not be wired even though its optional!<br/>
   *  ServiceClientFirstRemovedOf_ContractWithNoImplementers - a transitive set of dependencies, each involving optional, which should also be fine<br/>
   *  ServiceClientFirstRemovedOf_ContractWithExceptionThrowingImplementers - should not be wirable for similar reasons, present on system but not available<br/>
   *  OptionalRunLevelTstEnv - used for scoping to the correct RunLevelService impl<b/>
   * <p/>
   * 
   * Test:
   * The Test will be to move to runLevel=1 for env=OptionalInjectionEnvTest having a testing RunLevelListener installed.<br/>
   * (0) if the run level did not proceedTo 1 completely then fail.<br/>
   * (1) if any of ClientServiceOf_ContractWithNoImplementers, FirstRemovedClientServiceOf_ContractWithNoImplementers do not become active then fail.<br/>
   * (2) if any of ServiceImplOf_ContractWithExceptionThrowingImplementers, ClientServiceOf_ContractWithExceptionThrowingImplementers, FirstRemovedClientServiceOf_ContractWithExceptionThrowingImplementers becomes active then fail.<br/>
   * (3) If any of the two bad RunLevel services become active then fail.<br/>
   * (4) if the onError is not called two times then fail.<br/>
   * (5) if the exception in the onError is not rooted with an ComponentException cause then fail.<br/>
   * (6) If any of the two ok RunLevel services don't become active then fail.<br/>
   *
   * <p/>
   * TODO: We really need to redefine "optional" as "required if present", and perhaps add an @Inject attribute for "allowError" or something.
   */
  @Test
  public void testOptionalDependencies() throws Exception {
    // setup the test
    installTestRunLevelService(false);

    this.defRLlistener = (TestRunLevelListener) listener;
    defRLlistener.calls.clear();

    // get things rolling
    rls = new TestDefaultRunLevelService(h, false, OptionalRunLevelTstEnv.class, recorders); 
    rls.proceedTo(1);
    
    // test for failure
    
    // (0) if the run level did not proceedTo 1 completely then fail<br/>
    assertEquals(1, rls.getState().getCurrentRunLevel());
    assertEquals(null, rls.getState().getPlannedRunLevel());
    
    // (1) if any of ClientServiceOf_ContractWithNoImplementers, FirstRemovedClientServiceOf_ContractWithNoImplementers do not become active then fail.<br/>
    // (6) If any of the two ok RunLevel services don't become active then fail.<br/>
    Collection<Inhabitant<?>> coll = h.getInhabitantsByContract(ShouldBeActivateable1.class.getName());
    assertEquals("should be active count", 4, coll.size());
    for (Inhabitant<?> i : coll) {
      assertTrue("expected active: " + i, i.isActive());
      ShouldBeActivateable1 service = (ShouldBeActivateable1) i.get();
      service.validateSelf();
    }
    
    // (2) if any of ServiceImplOf_ContractWithExceptionThrowingImplementers, ClientServiceOf_ContractWithExceptionThrowingImplementers, FirstRemovedClientServiceOf_ContractWithExceptionThrowingImplementers becomes active then fail.<br/>
    // (3) If any of the two bad RunLevel services become active then fail.<br/>
    coll = h.getInhabitantsByContract(ShouldNotBeActivateable1.class.getName());
    assertEquals("should not be active count", 5, coll.size());
    for (Inhabitant<?> i : coll) {
      assertFalse("expected not active: " + i, i.isActive());
    }

    // (4) if the onError is not called two times then fail.<br/>
    // (5) if the exception in the onError is not rooted with an ComponentException cause then fail.<br/>
    assertListenerState(false, true, false);
    int errCount = 0;
    for (TestRunLevelListener.Call call : defRLlistener.calls) {
      if (call.type.equals("error")) {
        errCount++;
        assertTrue("exception: " + call.error, ComponentException.class.isInstance(call.error));
        Throwable t = call.error.getCause();
        assertNotNull(t);
        while (null != t) {
          assertTrue("exception: " + t, ComponentException.class.isInstance(t));
          t = t.getCause();
        }
      }
    }
    assertEquals("error count", 2, errCount);
  }

  /**
   * Obtaining a non default run level service from the habitat
   */
  @Test
  public void obtainingRunLevelServiceForAnotherScope() {
      DescriptorImpl descriptor = new DescriptorImpl(null, null);
      descriptor.addContract(RunLevelService.class.getName());
      descriptor.addMetadata(RunLevel.META_SCOPE_TAG, "java.lang.Long");
      Collection<Binding<?>> bindings = h.getBindings(descriptor);
      assertEquals(bindings.toString(), 1, bindings.size());

      Binding theOne = bindings.iterator().next();
      assertTrue("should have been initialized now", ((ManagedComponentProvider)theOne.getProvider()).isActive());
      assertEquals(AnotherNonDefaultRunLevelService.class, theOne.getProvider().get().getClass());
      assertSame(theOne.getProvider().get().getClass(), theOne.getProvider().get().getClass());
  }
  
  /**
   * Attempting to activate the non default run level service when there exists
   * a bad dependency (i.e., a dependency to another run level service
   * scope type.)
   */
  @Test
  public void activatingAnotherScopeWithBadDependency() {
      DescriptorImpl descriptor = new DescriptorImpl(null, AnotherNonDefaultEnvServerService.class.getName());
      Collection<Binding<?>> bindings = h.getBindings(descriptor);
      assertEquals(bindings.toString(), 1, bindings.size());
      
      Binding theInvalidOne = bindings.iterator().next();
      assertFalse("should not have been initialized now",
              ((ManagedComponentProvider)theInvalidOne.getProvider()).isActive());

      descriptor = new DescriptorImpl(null, null);
      descriptor.addContract(RunLevelService.class.getName());
      descriptor.addMetadata(RunLevel.META_SCOPE_TAG, "java.lang.Long");
      bindings = h.getBindings(descriptor);
      assertEquals(1, bindings.size());
      Binding theRls = bindings.iterator().next();
      AbstractRunLevelService rls = (AbstractRunLevelService) theRls.getProvider().get();

      final Reference<Boolean> cancelled = new Reference<Boolean>();
      final Reference<Integer> progress = new Reference<Integer>(0);
      final Reference<Throwable> error = new Reference<Throwable>();
      
      installTestRunLevelService(false);
      
      defRLlistener = (TestRunLevelListener) listener;
      defRLlistener.calls.clear();

      rls.setListener(new RunLevelListener() {
        @Override
        public void onCancelled(RunLevelState<?> state, ServiceContext ctx,
                int previousProceedTo, boolean isInterrupt) {
            cancelled.set(true);
        }

        @Override
        public void onError(RunLevelState<?> state, ServiceContext context,
                Throwable t, boolean willContinue) {
            error.set(t);
            assertTrue(willContinue);
        }

        @Override
        public void onProgress(RunLevelState<?> state) {
            progress.set(progress.get() + 1);
        }
      });
      
      rls.proceedTo(8);
      assertFalse("should not have been initialized now - it has an invalid dependency",
              ((ManagedComponentProvider)theInvalidOne.getProvider()).isActive());

      assertNull(cancelled.get());
      assertTrue(progress.get() > 8);
      assertNotNull("we expected the proceedTo to generate an error because of invalid injection", error.get());
      assertEquals(0, defRLlistener.calls.size());
      assertEquals(8, rls.getState().getCurrentRunLevel());
      assertEquals(Long.class.getName(), rls.getState().getScopeName());
  }
  
  /**
   * Attempting to activate the non default run level service when there exists
   * a bad dependency (i.e., a dependency to another run level service
   * scope type as well as upward dependencies) but through a Holder making
   * it all legitimate.
   */
  @SuppressWarnings("unchecked")
  @Test
  public void activatingAnotherScopeWithBadDependencyWithHolder() {
      DescriptorImpl descriptor = new DescriptorImpl();
      descriptor.addContract(RunLevelService.class);
      descriptor.addMetadata(RunLevel.META_SCOPE_TAG, Long.class.getName());
      Collection<Binding<?>> bindings = h.getBindings(descriptor);
      assertEquals(1, bindings.size());
      Binding theRls = bindings.iterator().next();
      AbstractRunLevelService rls = (AbstractRunLevelService) theRls.getProvider().get();

      rls.proceedTo(8);

      descriptor = new DescriptorImpl(AHolderBasedRegularService1.class);
      bindings = h.getBindings(descriptor);
      assertEquals(1, bindings.size());
      ManagedComponentProvider<AHolderBasedRegularService1> regService1Provider = 
          (ManagedComponentProvider<AHolderBasedRegularService1>) bindings.iterator().next().getProvider();
      assertNotNull(regService1Provider);
      assertFalse(regService1Provider.isActive());
      
      AHolderBasedRegularService1 regService1 = regService1Provider.get();
      assertNotNull(regService1);
      assertTrue(regService1Provider.isActive());
      
      AHolderBasedServerService1 holder1 = regService1.service1.get();
      assertNotNull("we've reached level 8", holder1);
      assertSame(holder1, regService1.service1.get());

      AHolderBasedServerService2 holder2 = regService1.service2.get();
      assertNull("we've not reached level 9", holder2);
      
      ANonDefaultEnvServerService another1 = holder1.different1.get();
      assertNull("the other run level service has not yet been started", another1);

      // now, start the "different1" run level service to an adequate start level
      descriptor = new DescriptorImpl();
      descriptor.addContract(RunLevelService.class);
      descriptor.addMetadata(RunLevel.META_SCOPE_TAG, Object.class.getName());
      bindings = h.getBindings(descriptor);
      assertEquals(1, bindings.size());
      Binding anotherRlsBinding = bindings.iterator().next();
      RunLevelService<?> anotherRls = (RunLevelService<?>) anotherRlsBinding.getProvider().get();
      anotherRls.proceedTo(7);
      
      // now re-check it
      ManagedComponentProvider acp = (ManagedComponentProvider) holder1.different1;
      assertTrue("proceeding to level 7 should have created demand for this service", acp.isActive());
      
      another1 = holder1.different1.get();
      assertNotNull("the other run level service has now been started", another1);
      assertSame(another1, holder1.different1.get());

      // now, bring the "different1" runLevelService down again
      anotherRls.proceedTo(6);
      
      // now re-check it again
      assertNull("the other run level service has now been brought down", holder1.different1.get());

      // bring it back up and make sure it's different
      anotherRls.proceedTo(7);
      
      ANonDefaultEnvServerService newOne = holder1.different1.get();
      assertNotNull("the other run level service has now been brought back up", newOne);
      assertFalse("instance didn't get cleared when coming down", another1 == newOne);

      // now take the original run level service, and bring it down
      rls.proceedTo(0);
      newOne = holder1.different1.get();
      assertNotNull("should have no affect on a different run level service", newOne);
      
      assertTrue("the regular service should not have been affected", regService1Provider.isActive());
      
      // it's holder services, however, should be gone
      holder1 = regService1.service1.get();
      assertNull("we've gone down", holder1);

      holder2 = regService1.service2.get();
      assertNull("we've gone down", holder2);
  }
  
  /**
   * Attempting to tamper will the default run level service should be prevented.
   */
  @Test
  public void tamperingWithDefaultRunLevelService() {
      installTestRunLevelService(false);
      defRLlistener = (TestRunLevelListener) listener;

      try {
          defRLS.setListener(defRLlistener);
          fail("expected this to be illegal");
      } catch (IllegalStateException e) {
          // expected
      }

      try {
          defRLS.setInhabitantActivator(null);
          fail("expected this to be illegal");
      } catch (IllegalStateException e) {
          // expected
      }

      try {
          defRLS.setInhabitantSorter(null);
          fail("expected this to be illegal");
      } catch (IllegalStateException e) {
          // expected
      }
  }
  
  private void installTestRunLevelService(boolean async) {
    Inhabitant<RunLevelService> r = 
      (Inhabitant<RunLevelService>) h.getInhabitant(RunLevelService.class, "default");
    assertNotNull(r);
    assertTrue(h.removeIndex(RunLevelService.class.getName(), "default"));
    h.remove(r);
    
    DefaultRunLevelService oldRLS = ((DefaultRunLevelService)rls);
    
    recorders = new LinkedHashMap<Integer, Recorder>();
    rls = new TestDefaultRunLevelService(h, async, DefaultRunLevelService.DEFAULT_SCOPE, recorders); 
    r = new ExistingSingletonInhabitant<RunLevelService>(RunLevelService.class, rls);
    h.add(r);
    h.addIndex(r, RunLevelService.class.getName(), "default");

    this.defRLS = (DefaultRunLevelService) rls;
    this.defRLlistener = (TestRunLevelListener) listener;
    defRLlistener.calls.clear();
    
    try {
      Method m = DefaultRunLevelService.class.getDeclaredMethod("setDelegate", RunLevelState.class);
      assert(null != m);
      m.setAccessible(true);
      m.invoke(oldRLS, (RunLevelState)rls);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
  
  
  /**
   * Verifies the instantiation / release of inhabitants are correct
   * 
   * @param runLevel
   */
  private void assertInhabitantsState(int runLevel) {
    Collection<Inhabitant<?>> runLevelInhabitants = h.getInhabitantsByContract(RunLevel.class.getName());
    assertTrue(runLevelInhabitants.size() > 0);
    for (Inhabitant<?> i : runLevelInhabitants) {
      AbstractInhabitantImpl<?> ai = AbstractInhabitantImpl.class.cast(i);
      RunLevel rl = ai.getAnnotation(RunLevel.class);
      if (rl.value() <= runLevel) {
        if (ai.toString().contains("Invalid")) {
          assertFalse("expect not instantiated: " + ai, ai.isActive());
        } else {
          if (DefaultRunLevelService.DEFAULT_SCOPE == rl.runLevelScope()) {
            assertTrue("expect instantiated: " + ai, ai.isActive());
          } else {
            assertFalse("expect instantiated: " + ai, ai.isActive());
          }
        }
      } else {
        assertFalse("expect not instantiated: " + ai, ai.isActive());
      }
    }
  }
  
  
  /**
   * Verifies the listener was indeed called, and the ordering is always consistent.
   */
  private void assertListenerState(boolean expectDownSide, boolean expectErrors, boolean expectCancelled) {
    assertListenerState(expectDownSide, expectErrors, expectCancelled ? 1 : 0, 0);
  }
  
  private void assertListenerState(boolean expectDownSide,
      boolean expectErrors,
      int expectCancelled,
      int expectInterruptCancelled) {
    assertTrue(defRLlistener.calls.size() > 0);
    int last = DefaultRunLevelService.INITIAL_RUNLEVEL;
    boolean upSide = true;
    int sawCancel = 0;
    int sawInterruptCancel = 0;
    boolean sawError = false;

    for (TestRunLevelListener.Call call : defRLlistener.calls) {
      if (expectDownSide) {
        if (!upSide) {
          // already on the down side
          assertTrue(call.toString(), call.current <= last);
        } else {
          // haven't seen the down side yet
          if (call.current < last) {
            upSide = false;
          }
        }
      } else {
        assertTrue(call.toString() + " and last was " + last, call.current >= last);
      }

      if (call.type.equals("cancelled")) {
        if (call.isHardInterrupt) {
          sawInterruptCancel++;
          
          // this is true for now, but may change in the future
          assertNotNull("expect context for interrupt", call.context);
        } else {
          sawCancel++;
        }
      } else if (call.type.equals("error")) {
        sawError = true;
      } else {
        assertEquals(call.toString(), "progress", call.type);
      }
      
      last = call.current;
    }
    
    if (expectDownSide) {
//      assertFalse("should have ended on down side: " + defRLlistener.calls, upSide);
      // race conditions prevents us from doing the assert
      if (upSide) {
        Logger.getAnonymousLogger().log(Level.WARNING, "Expected to have ended on down side: " + defRLlistener.calls);
      }
    }
  
    if (expectErrors) {
      assertTrue(defRLlistener.calls.toString(), sawError);
    }
    
    if (expectCancelled > 0) {
      assertEquals("expected to see cancel in: " + defRLlistener.calls,
          expectCancelled, sawCancel);
    }

    assertEquals("expected to see interrupt-style cancel in: " + defRLlistener.calls,
        expectInterruptCancelled, sawInterruptCancel);
  }

  private void assertNeverGotToRunLevel(int runLevel) {
    for (TestRunLevelListener.Call call : defRLlistener.calls) {
      assertTrue("Should never have reached runLevel: " + runLevel + " but we did in " + defRLlistener.calls,
          call.current < runLevel);
    }
  }

  /**
   * Verifies that the recorder is always consistent.
   */
  private void assertRecorderState() {
    assertFalse(recorders.toString(), recorders.isEmpty());
    assertEquals("Belongs to a different scope", 0, ExceptionRunLevelManagedService.constructCount);
    assertEquals("Belongs to a different scope", 0, ExceptionRunLevelManagedService.destroyCount);
    // we could really do more here...
  }
  
  
  private static class TestDefaultRunLevelService extends DefaultRunLevelService {
    TestDefaultRunLevelService(Habitat habitat, boolean async, Class<?> targetScope,
        HashMap<Integer, Recorder> recorders) {
      super(habitat, async, null, targetScope, recorders);
    }
  }
  
  
  private static class TestInhabitantSorter implements InhabitantSorter {
    public int callCount;

    @Override
    public List<Inhabitant<?>> sort(List<Inhabitant<?>> inhabitants) {
      callCount++;
      return inhabitants;
    }
  }
  

  private static class TestInhabitantActivator implements InhabitantActivator {
    public int activateCount;
    public int releaseCount;
    
    @Override
    public void activate(Inhabitant<?> inhabitant) {
      activateCount++;
//      System.out.println(inhabitant);
      inhabitant.get();
    }
    
    @Override
    public void deactivate(Inhabitant<?> inhabitant) {
      releaseCount++;
//      System.out.println(inhabitant);
//      inhabitant.release();
    }

    @Override
    public void awaitCompletion() {
    }

    @Override
    public void awaitCompletion(long timeount, TimeUnit unit) {
    }
  }

}
