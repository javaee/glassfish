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
package org.jvnet.hk2.test;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.component.ComponentException;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.component.Inhabitant;
import org.jvnet.hk2.junit.Hk2Runner;
import org.jvnet.hk2.test.contracts.DummyContract;
import org.jvnet.hk2.test.contracts.ErrorThrowingContract;
import org.jvnet.hk2.test.contracts.Simple;
import org.jvnet.hk2.test.contracts.SimpleGetter;
import org.jvnet.hk2.test.impl.PerLookupService;
import org.jvnet.hk2.test.impl.RandomService;
import org.jvnet.hk2.test.impl.RandomSimpleService;

import com.sun.hk2.component.Holder;
import com.sun.hk2.component.LazyInhabitant;

/**
 * Tests Extended Injection point capabilities.
 * 
 * @author Jeff Trent
 */
@RunWith(Hk2Runner.class)
public class ExtendedInjectTest {

  @Inject
  Habitat h;
  
  @Inject
  PerLookupService perLookupService;

  @Inject(optional = true)
  PerLookupService perLookupServiceOptional;

  @Inject
  RandomService singletonService;

  @Inject(optional = true)
  RandomService singletonServiceOptional;

  @Inject
  Holder<RandomService> singletonServiceHolder;

  @Inject(optional = true)
  DummyContract neverService;

  @Inject(name = "one")
  Simple oneSingletonService;

  @Inject(name = "one")
  Simple oneSingletonServiceCopy;

  @Inject(name = "two")
  Simple twoPerLookupService;

  @Inject(name = "two")
  Simple twoPerLookupServiceCopy;

  @Inject(optional = true)
  SimpleGetter simpleOptionalServiceWithADependency;

  // These are NOT annotated at field level, but is at method level
  Simple setterOneSimple;
  Simple setterOtherSimple;
  
  @Inject(name = "one")
  void setOneSimple(Simple simple) {
    assertNull(setterOneSimple);
    setterOneSimple = simple;
  }

  @SuppressWarnings("unused")
  @Inject(name = "other")
  private void setOtherSimple(Simple simple) {
    assertNull(setterOtherSimple);
    setterOtherSimple = simple;
  }

  @Test
  public void byType_perLookupScoped() {
    assertNotNull(perLookupService);
    assertNotSame(perLookupService, perLookupServiceOptional);
  }

  @Test
  public void byType_perSingletonScoped() {
    assertNotNull(singletonService);
    assertSame(singletonService, singletonServiceOptional);
  }

  @Test
  public void byContract_perLookupScoped() {
    assertNotNull(twoPerLookupService);
    assertNotSame(twoPerLookupService, twoPerLookupServiceCopy);
  }

  @Test
  public void byContract_perSingletonScoped() {
    assertNotNull(oneSingletonService);
    assertSame(oneSingletonService, oneSingletonServiceCopy);
  }

  @Test
  public void simpleOptionalServiceHavingADependent() {
    assertNotNull(simpleOptionalServiceWithADependency);
    assertEquals(RandomSimpleService.class, simpleOptionalServiceWithADependency.getClass());
    assertNotNull(simpleOptionalServiceWithADependency.getSimple());
    assertEquals("one", simpleOptionalServiceWithADependency.getSimple().get());
  }
  
  @Test
  public void byContract_neverSatisfied() {
    assertNull(neverService);
  }

  // TODO: see simpleGetterServiceHolderOptional above
  @Test
  public void holder() {
    assertNotNull(singletonServiceHolder);
    assertNotNull(singletonServiceHolder.get());
    assertSame(singletonService, singletonServiceHolder.get());
  }
  
  @Test
  public void setterMethod() {
    assertNotNull(setterOneSimple);
    assertEquals(setterOneSimple.get(), "one");

    assertNotNull(setterOtherSimple);
    assertEquals(setterOtherSimple.get(), "other");
  }

  /**
   * Verifies the affect of exceptions during the injection phase.  The
   * expectation is that inhabitant should NOT be activated in such
   * circumstances.
   */
  @Test
  public void errorThrowingSetterMethod() {
    Collection<Inhabitant<?>> iColl = 
        h.getAllInhabitantsByContract(ErrorThrowingContract.class.getName());
    assertNotNull(iColl);
    assertEquals(1, iColl.size());
    Inhabitant<?> i = iColl.iterator().next();
    
    LogHandler handler = new LogHandler();
    Logger logger = Logger.getLogger(LazyInhabitant.class.getName());
    logger.addHandler(handler);
    try {
      Object obj = i.get();
      fail("Exception expected but instead got: " + obj);
    } catch (ComponentException e) {
      // expected
//      e.printStackTrace();
    } finally {
      logger.removeHandler(handler);
    }
    assertFalse("shouldn't be instantiated - it's in a bad state", i.isInstantiated());

    // TODO: do logging (in the dynamic work)
//    assertEquals("log records: " + handler.publishedRecords, 1, handler.publishedRecords.size());
//    LogRecord lr = handler.publishedRecords.get(0);
//    assertEquals("log record: " + lr, Level.WARNING, lr.getLevel());
//    assertTrue("log record: " + lr, lr.getMessage().contains("Failed to activate inhabitant"));
//    assertNotNull("log record: " + lr, lr.getThrown());
  }

  /**
   * Verifies the affect of exceptions during the injection phase.  The
   * expectation is that the dependent inhabitant should NOT be 
   * wired / activated in such circumstances.
   */
  @Test
  public void errorThrowingInDI() {
    Inhabitant<?> iets = 
      h.getInhabitant(ErrorThrowingContract.class, null);
    assertNotNull(iets);
    assertFalse(iets.isInstantiated());
    
    Inhabitant<?> ietds = 
      h.getInhabitant(Simple.class, "ErrorThrowingDependentService");
    assertNotNull(ietds);
    assertNotSame(iets, ietds);
    assertFalse(ietds.isInstantiated());
  
    LogHandler handler = new LogHandler();
    Logger logger = Logger.getLogger(LazyInhabitant.class.getName());
    logger.addHandler(handler);
    
    try {
      Simple simple = h.getComponent(Simple.class, "ErrorThrowingDependentService");
      fail("Expected unsatisfied dependencies exception but got: " + simple);
    } catch (Exception e) {
      // expected
      assertEquals("exception type", ComponentException.class, e.getClass());
      assertEquals("message", "injection failed on org.jvnet.hk2.test.impl.ErrorThrowingDependentService.errorThrowing with interface org.jvnet.hk2.test.contracts.ErrorThrowingContract", e.getLocalizedMessage());
      Throwable e2 = e.getCause();
      assertEquals("exception 2 type", ComponentException.class, e2.getClass());
      assertEquals("message 2", "injection failed on void org.jvnet.hk2.test.impl.ErrorThrowingService.fakeRandomContractThrowingUp(org.jvnet.hk2.test.runlevel.RandomContract)", e2.getLocalizedMessage());
    }
    
    assertFalse(iets.isInstantiated());
    assertFalse(ietds.isInstantiated());

    // TODO: do logging (in the dynamic work)
//    assertEquals("log records: " + handler.publishedRecords, 2, handler.publishedRecords.size());
//    for (LogRecord lr : handler.publishedRecords) {
//      assertEquals("log record: " + lr, Level.WARNING, lr.getLevel());
//      assertTrue("log record: " + lr, lr.getMessage().contains("Failed to activate inhabitant"));
//      assertNotNull("log record: " + lr, lr.getThrown());
//    }
  }
  
  
  static class LogHandler extends Handler {
    final ArrayList<LogRecord> publishedRecords = new ArrayList<LogRecord>();
    
    @Override
    public void close() throws SecurityException {
    }

    @Override
    public void flush() {
    }

    @Override
    public void publish(LogRecord lr) {
      publishedRecords.add(lr);
    }
  }

}
