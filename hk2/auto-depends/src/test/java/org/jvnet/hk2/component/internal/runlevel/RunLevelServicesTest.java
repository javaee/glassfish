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
    RunLevel rl = ARunLevel.class.getAnnotation(RunLevel.class);
    RunLevelService<?> rls = 
      rlss.get(h, rl.value(), rl.runLevelScope());
    assertNotNull(rls);
    assertEquals(DefaultRunLevelService.class, rls.getClass());
  }
  
  @Test
  public void undelegatingBehavior() {
    RunLevelServiceStub stub = new RunLevelServiceStub(h, String.class.getName());
    assertSame(stub, stub.getState());
    assertNull(stub.getCurrentRunLevel());
    assertNull(stub.getPlannedRunLevel());
    assertEquals(String.class.getName(), stub.getScopeName());
    
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
    RunLevel rl = ANonExistantEnvRunLevel.class.getAnnotation(RunLevel.class);
    RunLevelService<?> rls = 
      rlss.get(h, rl.value(), rl.runLevelScope());
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
    assertEquals("should not delegate", Integer.class.getName(), stub.getScopeName());
    
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
    RunLevel rl = ANonExistantEnvRunLevel.class.getAnnotation(RunLevel.class);
    RunLevelService<?> rls = 
      rlss.get(h, rl.value(), rl.runLevelScope());
    assertNotNull(rls);
    assertEquals(RunLevelServiceStub.class, rls.getClass());
  }
  
  @Test
  public void runLevelServiceForInitializedHabitat() {
    RunLevelServices rlss = new RunLevelServices();
    RunLevel rl = ANonExistantEnvRunLevel.class.getAnnotation(RunLevel.class);
    try {
      RunLevelService<?> rls = 
        rlss.get(h, rl.value(), rl.runLevelScope());
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
    RunLevelService<?> rls = new SomeOtherRunLevelService();
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
    RunLevel rl = ANonExistantEnvRunLevel.class.getAnnotation(RunLevel.class);
    try {
      rls = rlss.get(h, rl.value(), rl.runLevelScope());
      fail("expected exception but got: " + rls);
    } catch (ComponentException e) {
      // expected
    }
  }
  
}
