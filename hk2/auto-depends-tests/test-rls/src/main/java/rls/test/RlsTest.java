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
package rls.test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertSame;
import static junit.framework.Assert.fail;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.HashSet;

import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.RunLevel;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.component.Inhabitant;

import rls.test.infra.MultiThreadedInhabitantActivator;
import rls.test.infra.RandomInhabitantSorter;
import rls.test.model.ContractX;
import rls.test.model.ContractY;
import rls.test.model.ServiceBaseX;
import rls.test.model.ServiceDerivedX;
import rls.test.model.ServiceOtherToY;
import rls.test.model.ServiceZ;

import com.sun.enterprise.module.bootstrap.ModuleStartup;
import com.sun.enterprise.module.bootstrap.StartupContext;
import com.sun.hk2.component.Holder;

@Service
public class RlsTest implements ModuleStartup {

  @Inject static Habitat h;
//  @Inject(optional=true) static ContractY y;
//  @Inject(optional=true) static ServiceOtherToY other;
  @Inject static Holder<ServiceZ> zHolder;
  
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

  public static void runTests() {
    assert h.isInitialized() : "Sanity check";
    
    assertTrue("Sorter should be called", RandomInhabitantSorter.wasCalled());
    assertTrue("Activator should be called", MultiThreadedInhabitantActivator.wasCalled());
    
//    assertNull("can't support dependencies to a non RLS", y);
//    assertNull("can't support dependencies to a non RLS", other);

    try {
      Thread.sleep(250);
    } catch (InterruptedException e) {
      fail(e.getMessage());
    }
    
    Collection<Inhabitant<?>> coll = h.getInhabitantsByContract(ContractX.class.getName());
    assertEquals("ContractX service count: " + coll, 2, coll.size());
    mustBeActive(coll, true);

    coll = new HashSet<Inhabitant<?>>(h.getInhabitantsByContract(RunLevel.class.getName()));
    assertEquals("ContractX service count: " + coll, 5, coll.size());
    
    mustBeActive(coll, true);

    verifyServiceBaseX();
    verifyServiceDerivedX();
    verifyServiceY();
    verifyServiceZ();
    
    verifyInhabitantMetaData();
  }

  @SuppressWarnings("static-access")
  private static void verifyServiceBaseX() {
    assertEquals("ctor count (one for base one for derived thru base)", 
        2, ServiceBaseX.ctorCount);
    assertEquals("postConstruct count (one for base one for derived thru base)", 
        2, ServiceBaseX.postConstructCount);
    assertNotNull(ServiceBaseX.y);
    assertNotNull(ServiceBaseX.other);
    assertSame(ServiceBaseX.y, ServiceBaseX.other.y);
  }

  private static void verifyServiceDerivedX() {
    assertEquals("ctor count", 
        1, ServiceDerivedX.ctorCount);
    assertEquals("postConstruct count", 
        1, ServiceDerivedX.postConstructCount);
    assertNotNull(ServiceDerivedX.y);
  }

  private static void verifyServiceY() {
    assertEquals("ctor count", 
        1, ServiceOtherToY.ctorCount);
    assertEquals("postConstruct count", 
        1, ServiceOtherToY.postConstructCount);
    assertNotNull("other.y", ServiceOtherToY.y);
    assertNotNull("other.allY", ServiceOtherToY.allY);
    assertEquals("other.allY count", 3, ServiceOtherToY.allY.length);
    Collection<Inhabitant<?>> coll = h.getInhabitantsByContract(ContractY.class.getName());
    assertEquals("ContractY service count: " + coll, 3, coll.size());
    mustBeActive(coll, true);
    assertNotNull("other.zHolder", ServiceOtherToY.zHolder);
  }

  private static void verifyServiceZ() {
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

    assertSame("other.zHolder", zHolder.get(), ServiceOtherToY.zHolder.get());
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

  /**
   * verifies that RunLevel metadata exists on the inhabitants (using habitat file approach)
   */
  private static void verifyInhabitantMetaData() {
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

}
