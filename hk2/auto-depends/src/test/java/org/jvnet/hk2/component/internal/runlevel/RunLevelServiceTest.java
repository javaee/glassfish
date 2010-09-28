package org.jvnet.hk2.component.internal.runlevel;

import static org.junit.Assert.*;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.RunLevel;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.component.Inhabitant;
import org.jvnet.hk2.component.InhabitantActivator;
import org.jvnet.hk2.component.InhabitantSorter;
import org.jvnet.hk2.component.RunLevelListener;
import org.jvnet.hk2.component.RunLevelService;
import org.jvnet.hk2.component.RunLevelState;
import org.jvnet.hk2.component.internal.runlevel.DefaultRunLevelService;
import org.jvnet.hk2.component.internal.runlevel.Recorder;
import org.jvnet.hk2.junit.Hk2Runner;
import org.jvnet.hk2.junit.Hk2RunnerOptions;
import org.jvnet.hk2.test.runlevel.ExceptionRunLevelManagedService;
import org.jvnet.hk2.test.runlevel.ExceptionRunLevelManagedService2b;
import org.jvnet.hk2.test.runlevel.InterruptRunLevelManagedService1a;
import org.jvnet.hk2.test.runlevel.InterruptRunLevelManagedService2b;
import org.jvnet.hk2.test.runlevel.NonRunLevelWithRunLevelDepService;
import org.jvnet.hk2.test.runlevel.RunLevelContract;
import org.jvnet.hk2.test.runlevel.RunLevelServiceBase;
import org.jvnet.hk2.test.runlevel.RunLevelServiceNegOne;
import org.jvnet.hk2.test.runlevel.ServiceA;
import org.jvnet.hk2.test.runlevel.ServiceB;
import org.jvnet.hk2.test.runlevel.ServiceC;
import org.jvnet.hk2.test.runlevel.TestRunLevelListener;

import com.sun.hk2.component.AbstractInhabitantImpl;
import com.sun.hk2.component.ExistingSingletonInhabitant;

/**
 * Testing around the default RunLevelService impl.
 * 
 * @author Jeff Trent
 */
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
  @SuppressWarnings("unchecked")
  @Test
  public void validInitialHabitatState() {
    Collection<RunLevelListener> coll1 = h.getAllByContract(RunLevelListener.class);
    assertNotNull(coll1);
    assertEquals(1, coll1.size());
    assertSame(listener, coll1.iterator().next());
    assertTrue(coll1.iterator().next() instanceof TestRunLevelListener);
    
    Collection<RunLevelService> coll2 = h.getAllByContract(RunLevelService.class);
    assertNotNull(coll2);
    assertEquals(coll2.toString(), 2, coll2.size());  // a test one, and the real one
    
    RunLevelService rls = h.getComponent(RunLevelService.class);
    assertNotNull(rls);
    assertNotNull(rls.getState());
    assertEquals(-1, rls.getState().getCurrentRunLevel());
    assertEquals(null, rls.getState().getPlannedRunLevel());
    assertEquals(Void.class, rls.getState().getEnvironment());
    
    RunLevelService rls2 = h.getComponent(RunLevelService.class, "default");
    assertSame(rls, rls2);
    assertSame(this.rls, rls);
    assertTrue(rls instanceof DefaultRunLevelService);
  }
  
  /**
   * Verifies that RunLevel -1 inhabitants are created immediately
   */
  @Test
  public void validateRunLevelNegOneInhabitants() {
    assertTrue(h.isInitialized());
    Inhabitant<RunLevelServiceNegOne> i = h.getInhabitantByType(RunLevelServiceNegOne.class);
    assertNotNull(i);
    assertTrue(i.toString() + "expected to have been instantiated", i.isInstantiated());
  }
  
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
   * There should be no runlevel services at this level.
   */
  @Test
  public void proceedTo0() {
    installTestRunLevelService(false);
    rls.proceedTo(0);
    assertEquals(recorders.toString(), 0, recorders.size());
    assertEquals(0, defRLS.getCurrentRunLevel());
    assertEquals(null, defRLS.getPlannedRunLevel());
  }
  
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

    assertTrue(iB.isInstantiated());
    assertTrue(iA.isInstantiated());
    assertTrue(iC.isInstantiated());
    
    iter = activations.iterator();
    assertSame("order is important", iC, iter.next());
    assertSame("order is important", iB, iter.next());
    assertSame("order is important", iA, iter.next());

    RunLevelServiceBase a = (RunLevelServiceBase) iA.get();
    RunLevelServiceBase b = (RunLevelServiceBase) iB.get();
    RunLevelServiceBase c = (RunLevelServiceBase) iC.get();

    RunLevelServiceBase.count = 0;
    defRLS.proceedTo(0);
    assertFalse(iB.isInstantiated());
    assertFalse(iA.isInstantiated());
    assertFalse(iC.isInstantiated());

    assertEquals(recorders.toString(), 1, recorders.size());
    assertNotNull(recorders.toString(), recorders.get(10));
    assertTrue(recorders.toString(), recorders.get(10).getActivations().isEmpty());

    assertEquals("count", 3, RunLevelServiceBase.count);
    assertEquals("order is important on shutdown too: A", 0, a.countStamp);
    assertEquals("order is important on shutdown too: B", 1, b.countStamp);
    assertEquals("order is important on shutdown too: C", 2, c.countStamp);

    assertListenerState(true, false, false);
  }
  
  @Test
  public void dependenciesFromNonRunLevelToRunLevelService() {
    rls.proceedTo(10);
  
    Inhabitant<NonRunLevelWithRunLevelDepService> i = 
      h.getInhabitantByType(NonRunLevelWithRunLevelDepService.class);
    assertNotNull(i);
    assertFalse(i.isInstantiated());
    
    try {
      fail("Expected get() to fail, bad dependency to a RunLevel service: " + i.get());
    } catch (Exception e) {
      // expected
    }

    assertFalse(i.isInstantiated());
  }
  
  @Test
  public void dependenciesFromNonRunLevelToRunLevelServiceAsync() {
    installTestRunLevelService(true);
    
    defRLS.proceedTo(10);
  
    Inhabitant<NonRunLevelWithRunLevelDepService> i = 
      h.getInhabitantByType(NonRunLevelWithRunLevelDepService.class);
    assertNotNull(i);
    assertFalse(i.isInstantiated());
    
    try {
      fail("Expected get() to fail, bad dependency to a RunLevel service: " + i.get());
    } catch (Exception e) {
      // expected
    }

    assertFalse(i.isInstantiated());
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

  @Test
  public void exceptionTypeEnvRunLevelService() throws Exception {
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
    
    Collection<Inhabitant<?>> coll = h.getInhabitantsByContract(RunLevelContract.class.getCanonicalName());
    assertTrue(coll.size() >= 3);
    boolean gotOne = false;
    for (Inhabitant<?> i : coll) {
      String typeName = i.typeName();
      if (typeName.contains("ExceptionRunLevelManagedService2")) {
        gotOne = true;
        assertFalse("expected to be in released state: " + i, i.isInstantiated());
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
  public void multiThreadedInterrupt() throws Exception {
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
    
    Collection<Inhabitant<?>> coll = h.getInhabitantsByContract(RunLevelContract.class.getCanonicalName());
    assertTrue(coll.size() >= 3);
    boolean gotOne = false;
    boolean gotTwo = false;
    for (Inhabitant<?> i : coll) {
      String typeName = i.typeName();
      if (typeName.contains("InterruptRunLevelManagedService1")) {
        gotOne = true;
        assertTrue("expected to be in active state: " + i, i.isInstantiated());
      }
      if (typeName.contains("InterruptRunLevelManagedService2")) {
        gotTwo = true;
        assertFalse("expected to be in released state: " + i, i.isInstantiated());
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
   */
  @SuppressWarnings("unchecked")
  @Test
  public void multiThreadedInterrupt2() throws Exception {
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
          // this will hang {@link InterruptRunLevelManagedService2b}
          rls.proceedTo(0);
        } catch (Exception e) {
          problems.add(e);
        }
      }
    };
    watchDog.start();
    
    Logger.getAnonymousLogger().log(Level.INFO, "issuing proceedTo(1) from main thread: " + this);
    // this main thread will be interrupted to go to 0 but only after proceedTo(2) is sent (see above)
    rls.proceedTo(1);
    
    watchDog.join();
    
    assertTrue("problems: " + problems, problems.isEmpty());
    assertEquals(0, defRLS.getCurrentRunLevel());
    assertEquals(null, defRLS.getPlannedRunLevel());
    assertTrue("hanging service not reached", InterruptRunLevelManagedService2b.i > 0);
    
    Collection<Inhabitant<?>> coll = h.getInhabitantsByContract(RunLevelContract.class.getCanonicalName());
    assertTrue(coll.size() >= 3);
    boolean gotOne = false;
    boolean gotTwo = false;
    for (Inhabitant<?> i : coll) {
      String typeName = i.typeName();
      if (typeName.contains("InterruptRunLevelManagedService1")) {
        gotOne = true;
        assertFalse("expected to be in released state: " + i, i.isInstantiated());
      }
      if (typeName.contains("InterruptRunLevelManagedService2")) {
        gotTwo = true;
        assertFalse("expected to be in released state: " + i, i.isInstantiated());
      }
    }
    assertTrue(gotOne);
    assertTrue(gotTwo);

    assertNeverGotToRunLevel(2);
    
    assertListenerState(true, false, 1);
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
  public void multiThreadedInterrupt3Async() throws Exception {
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
      
      Collection<Inhabitant<?>> coll = h.getInhabitantsByContract(RunLevelContract.class.getCanonicalName());
      assertTrue(coll.size() >= 3);
      boolean gotOne = false;
      boolean gotTwo = false;
      for (Inhabitant<?> i : coll) {
        String typeName = i.typeName();
        if (typeName.contains("InterruptRunLevelManagedService1")) {
          gotOne = true;
          assertFalse("expected to be in released state: " + i, i.isInstantiated());
        }
        if (typeName.contains("InterruptRunLevelManagedService2")) {
          gotTwo = true;
          assertFalse("expected to be in released state: " + i, i.isInstantiated());
        }
      }
      assertTrue(gotOne);
      assertTrue(gotTwo);
  
      assertNeverGotToRunLevel(2);
      
      assertListenerState(true, false, 2);
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
  
  
  @SuppressWarnings("unchecked")
  private void installTestRunLevelService(boolean async) {
    Inhabitant<RunLevelService> r = 
      (Inhabitant<RunLevelService>) h.getInhabitant(RunLevelService.class, "default");
    assertNotNull(r);
    assertTrue(h.removeIndex(RunLevelService.class.getName(), "default"));
    h.remove(r);
    
    DefaultRunLevelService oldRLS = ((DefaultRunLevelService)rls);
    
    recorders = new LinkedHashMap<Integer, Recorder>();
    rls = new TestDefaultRunLevelService(h, async, Void.class, recorders); 
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
    Collection<Inhabitant<?>> runLevelInhabitants = h.getAllInhabitantsByContract(RunLevel.class.getName());
    assertTrue(runLevelInhabitants.size() > 0);
    for (Inhabitant<?> i : runLevelInhabitants) {
      AbstractInhabitantImpl<?> ai = AbstractInhabitantImpl.class.cast(i);
      RunLevel rl = ai.getAnnotation(RunLevel.class);
      if (rl.value() <= runLevel) {
        if (ai.toString().contains("Invalid")) {
          assertFalse("expect not instantiated: " + ai, ai.isInstantiated());
        } else {
          if (Void.class == rl.environment()) {
            assertTrue("expect instantiated: " + ai, ai.isInstantiated());
          } else {
            assertFalse("expect instantiated: " + ai, ai.isInstantiated());
          }
        }
      } else {
        assertFalse("expect not instantiated: " + ai, ai.isInstantiated());
      }
    }
  }
  
  
  /**
   * Verifies the listener was indeed called, and the ordering is always consistent.
   */
  private void assertListenerState(boolean expectDownSide, boolean expectErrors, boolean expectCancelled) {
    assertListenerState(expectDownSide, expectErrors, expectCancelled ? 1 : 0);
  }
  
  private void assertListenerState(boolean expectDownSide, boolean expectErrors, int expectCancelled) {
    assertTrue(defRLlistener.calls.size() > 0);
    int last = -2;
    boolean upSide = true;
    int sawCancel = 0;
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

      if (upSide) {
        // we should only see cancel and error on up side (the way we designed our tests)
        if (call.type.equals("cancelled")) {
          sawCancel++;
        } else if (call.type.equals("error")) {
          sawError = true;
        }
      } else {
        if (call.type.equals("error")) {
          sawError = true;
        } else {
          assertEquals(call.toString(), "progress", call.type);
        }
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
      assertEquals("expected to see cancel in: " + defRLlistener.calls, expectCancelled, sawCancel);
    }
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
    assertEquals("Belongs to a different environment", 0, ExceptionRunLevelManagedService.constructCount);
    assertEquals("Belongs to a different environment", 0, ExceptionRunLevelManagedService.destroyCount);
    // we could really do more here...
  }
  
  
  private static class TestDefaultRunLevelService extends DefaultRunLevelService {
    TestDefaultRunLevelService(Habitat habitat, boolean async, Class<?> targetEnv,
        HashMap<Integer, Recorder> recorders) {
      super(habitat, async, null, targetEnv, recorders);
    }
  }
  
  
  @SuppressWarnings("unchecked")
  private static class TestInhabitantSorter implements InhabitantSorter {
    public int callCount;

    @Override
    public List<Inhabitant> sort(List<Inhabitant> inhabitants) {
      callCount++;
      return inhabitants;
    }
  }
  

  @SuppressWarnings("unchecked")
  private static class TestInhabitantActivator implements InhabitantActivator {
    public int activateCount;
    public int releaseCount;
    
    @Override
    public void activate(Inhabitant inhabitant) {
      activateCount++;
      System.out.println(inhabitant);
      inhabitant.get();
    }
    
    @Override
    public void deactivate(Inhabitant inhabitant) {
      releaseCount++;
      System.out.println(inhabitant);
//      inhabitant.release();
    }
  }

}
