package org.jvnet.hk2.component.internal.runlevel;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.RunLevel;
import org.jvnet.hk2.component.ComponentException;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.component.Inhabitant;
import org.jvnet.hk2.component.InhabitantListener;
import org.jvnet.hk2.component.RunLevelService;
import org.jvnet.hk2.component.RunLevelState;
import org.jvnet.hk2.component.InhabitantListener.EventType;
import org.jvnet.hk2.junit.Hk2Runner;
import org.jvnet.hk2.test.runlevel.ANonExistantEnvRunLevel;
import org.jvnet.hk2.test.runlevel.ARunLevel;
import org.jvnet.hk2.test.runlevel.SomeOtherRunLevelService;

import com.sun.hk2.component.ExistingSingletonInhabitant;
import com.sun.hk2.component.TestRunLevelInhabitant;

/**
 * Tests for RunLevelServices
 * 
 * @author Jeff Trent
 */
@RunWith(Hk2Runner.class)
public class RunLevelServicesTest {

  @Inject
  Habitat h;
  
  @Test
  public void runLevelServiceForDefaultEnvShouldBeReal() {
    Habitat h = new Habitat();
    assertFalse(h.isInitialized());
    RunLevelServices rlss = new RunLevelServices();
    RunLevelService<?> rls = 
      rlss.get(h, ARunLevel.class.getAnnotation(RunLevel.class));
    assertNotNull(rls);
    assertEquals(DefaultRunLevelService.class, rls.getClass());
  }
  
  @Test
  public void undelegatingBehavior() {
    RunLevelServiceStub stub = new RunLevelServiceStub(h, String.class);
    assertSame(stub, stub.getState());
    assertNull(stub.getCurrentRunLevel());
    assertNull(stub.getPlannedRunLevel());
    assertSame(String.class, stub.getEnvironment());
    
    try {
      stub.proceedTo(1);
      fail("should have generated exception");
    } catch (Exception e) {
      // expected
    }
  }
  
  @Test
  public void forNonExistantRunLevelService() {
    Habitat h = new Habitat();
    assertFalse(h.isInitialized());
    RunLevelServices rlss = new RunLevelServices();
    RunLevelService<?> rls = 
      rlss.get(h, ANonExistantEnvRunLevel.class.getAnnotation(RunLevel.class));
    assertNotNull(rls);
    assertEquals(RunLevelServiceStub.class, rls.getClass());
    RunLevelState<?> state = rls.getState();
    assertNotNull("needs state to be given to RunLevelInhabitant", state);
    
    assertTrue(rls instanceof InhabitantListener);
    InhabitantListener listener = (InhabitantListener)rls;
    
    Inhabitant<?> real = new ExistingSingletonInhabitant<Object>(new Object());
    TestRunLevelInhabitant rli = new TestRunLevelInhabitant(real, 2, state, listener);

    // force a call to the listener - should be swallowed without any problems
    rli.notify(EventType.INHABITANT_ACTIVATED);
    
    // at this point, since we are already setup, we are going to also
    // the the behavior of a delegate being set (so its really not "NonExistant"
    // anymore.
    
    SomeOtherRunLevelService realRls = new SomeOtherRunLevelService(42, 24);
    
    RunLevelServiceStub stub = (RunLevelServiceStub)rls;
    stub.activate(realRls);
    
    // now that stub has been activated, we expect some new behavior
    assertSame("State, however, should be the same - that's what the inhabitants have in possession",
        state, stub.getState());
    assertEquals("should delegate", 42, stub.getCurrentRunLevel());
    assertEquals("should delegate", 24, stub.getPlannedRunLevel());
    assertSame("should not delegate", Integer.class, stub.getEnvironment());
    
    // force another call to the listener - should be passed along
    rli.notify(EventType.INHABITANT_ACTIVATED);
    assertEquals(1, realRls.calls.size());
  }
  
  /**
   * Needs to be a listener in order to forward messages to the delegate.
   */
  @Test
  public void runLevelServiceForNonExistantShouldBeAListener() {
    Habitat h = new Habitat();
    assertFalse(h.isInitialized());
    RunLevelServices rlss = new RunLevelServices();
    RunLevelService<?> rls = 
      rlss.get(h, ANonExistantEnvRunLevel.class.getAnnotation(RunLevel.class));
    assertNotNull(rls);
    assertEquals(RunLevelServiceStub.class, rls.getClass());
  }
  
  @Test
  public void runLevelServiceForInitializedHabitat() {
    RunLevelServices rlss = new RunLevelServices();
    try {
      RunLevelService<?> rls = 
        rlss.get(h, ANonExistantEnvRunLevel.class.getAnnotation(RunLevel.class));
      fail("expected exception but got: " + rls);
    } catch (ComponentException e) {
      // expected
    }
  }
  
  @SuppressWarnings("unchecked")
  @Test
  public void multipleRunLevelServicesIsAProblem() {
    Habitat h = new Habitat();
    assertFalse(h.isInitialized());

    // now create the first run level service
    RunLevelService rls = new SomeOtherRunLevelService();
    ExistingSingletonInhabitant<RunLevelService> rlsI = 
      new ExistingSingletonInhabitant<RunLevelService>(RunLevelService.class, rls);
    h.add(rlsI);
    h.addIndex(rlsI, RunLevelService.class.getName(), "x");

    // now create the duplicate run level service
    rls = new SomeOtherRunLevelService();
    rlsI = 
      new ExistingSingletonInhabitant<RunLevelService>(RunLevelService.class, rls);
    h.add(rlsI);
    h.addIndex(rlsI, RunLevelService.class.getName(), "y");

    h.initialized();

    RunLevelServices rlss = new RunLevelServices();
    try {
      rls = rlss.get(h, ANonExistantEnvRunLevel.class.getAnnotation(RunLevel.class));
      fail("expected exception but got: " + rls);
    } catch (ComponentException e) {
      // expected
    }
  }
  
}
