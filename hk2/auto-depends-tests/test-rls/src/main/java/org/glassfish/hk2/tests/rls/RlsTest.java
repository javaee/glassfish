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
package org.glassfish.hk2.tests.rls;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertSame;
import static junit.framework.Assert.fail;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.tests.rls.model.ServiceOtherToYImpl;
import javax.inject.Inject;
import javax.inject.Provider;

import org.jvnet.hk2.annotations.RunLevel;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.Inhabitant;

import org.glassfish.hk2.tests.rls.infra.RLSTestMultiThreadedInhabitantActivator;
import org.glassfish.hk2.tests.rls.infra.RandomInhabitantSorter;
import org.glassfish.hk2.tests.rls.model.ContractX;
import org.glassfish.hk2.tests.rls.model.ContractY;
import org.glassfish.hk2.tests.rls.model.ServiceBaseX;
import org.glassfish.hk2.tests.rls.model.ServiceDerivedX;
import org.glassfish.hk2.tests.rls.model.ServiceOtherToY;
import org.glassfish.hk2.tests.rls.model.ServiceZ;
import org.glassfish.hk2.utilities.BuilderHelper;

import com.sun.enterprise.module.bootstrap.ModuleStartup;
import com.sun.enterprise.module.bootstrap.StartupContext;
import com.sun.hk2.component.Holder;

@Service
public class RlsTest implements ModuleStartup {

  @Inject ServiceLocator h;
//  @Inject(optional=true) static ContractY y;
//  @Inject(optional=true) static ServiceOtherToY other;
  @Inject Provider<ServiceZ> zHolder;
  
  @Override
  public void setStartupContext(StartupContext context) {
  }

  @Override
  public void start() {
    runTests();
  }

  @Override
  public void stop() {
  }

  public void runTests() {
     
//    assertNull("can't support dependencies to a non RLS", y);
//    assertNull("can't support dependencies to a non RLS", other);

    try {
      Thread.sleep(250);
    } catch (InterruptedException e) {
      fail(e.getMessage());
    }
    
    List<?> coll = h.getAllServices(BuilderHelper.createContractFilter(ContractX.class.getName()));
    assertEquals("ContractX service count: " + coll, 2, coll.size());
    
    coll =  h.getAllServices(BuilderHelper.createContractFilter(RunLevel.class.getName()));
    assertEquals("ContractX service count: " + coll, 5, coll.size());
    
    verifyServiceBaseX();
    verifyServiceDerivedX();
    verifyServiceY();
    verifyServiceZ();
    
  }

  @SuppressWarnings("static-access")
  private static void verifyServiceBaseX() {
    assertEquals("ctor count (one for base one for derived thru base)", 
        2, ServiceBaseX.ctorCount);
    assertEquals("postConstruct count (one for base one for derived thru base)", 
        2, ServiceBaseX.postConstructCount);
  }

  private static void verifyServiceDerivedX() {
    assertEquals("ctor count", 
        1, ServiceDerivedX.ctorCount);
    assertEquals("postConstruct count", 
        1, ServiceDerivedX.postConstructCount);
  }

  private void verifyServiceY() {
    assertEquals("ctor count", 
        1, ServiceOtherToYImpl.ctorCount);
    assertEquals("postConstruct count", 
        1, ServiceOtherToYImpl.postConstructCount);
    List<?> coll = h.getAllServices(BuilderHelper.createContractFilter(ContractY.class.getName()));
    assertEquals("ContractY service count: " + coll, 3, coll.size());
  }

  private  void verifyServiceZ() {
    assertNotNull("holder to z", zHolder);
    
    assertEquals("ctor count", 
        0, ServiceZ.ctorCount);
    assertEquals("postConstruct count", 
        0, ServiceZ.postConstructCount);
    assertNotNull(zHolder.get());
    assertSame(zHolder.get(), zHolder.get());
    assertEquals("ctor count", 
        1, ServiceZ.ctorCount);
    assertEquals("postConstruct count", 
        1, ServiceZ.postConstructCount);
  }
  
  private static void mustBeActive(Collection<Inhabitant<?>> coll, boolean expectActive) {
    for (Inhabitant<?> i : coll) {
      if (expectActive) {
        assertTrue("DefaultRunLevelService should have activated: " + i, i.isActive());
      } else {
        assertFalse("should not be active: " + i, i.isActive());
      }
    }
  }


}
