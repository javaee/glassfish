/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2007-2011 Oracle and/or its affiliates. All rights reserved.
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

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.junit.Hk2Test;
import org.jvnet.hk2.test.impl.PerLookupService;
import org.jvnet.hk2.test.impl.PerLookupServiceNested3;

import com.sun.hk2.component.AbstractInhabitantImpl;
import com.sun.hk2.component.InjectInjectionResolver;

/**
 * Verifies the correctness of PerLookup managed scoped instances
 */
public class PerLookupManagedTest extends Hk2Test {

  public static final int ITERATIONS = 10000;
  
  @Inject
  Habitat h;

  /**
   * Construct {@link #ITERATIONS} {@link PerLookupService} instances, letting
   * garbage collection clean up the previous unused ones.  This is to ferret
   * out any memory consumption / leak issues.
   */
  @Test
  public void managedPerLookups() throws Exception {
    if (InjectInjectionResolver.MANAGED_ENABLED) {
      PerLookupService plsPrev = null;
      Inhabitant<PerLookupService> plsi;
      PerLookupService pls;
      ArrayList<Inhabitant<?>> managed = new ArrayList<Inhabitant<?>>();
      for (int i = 0; i < ITERATIONS; i++) {
        plsi = h.getInhabitantByType(PerLookupService.class).scopedClone();
        managed.add(plsi);
        pls = plsi.get();
        assertFalse(plsPrev == pls);
        plsPrev = pls;
        assertSame(pls, plsi.get());
        
        assertEquals(i+1, PerLookupService.constructs);
        assertEquals(i+1, PerLookupServiceNested3.constructs);
        
        assertEquals(1, AbstractInhabitantImpl.class.cast(plsi).getManagedCount());
        
        plsi.release();
      }

      managed.clear();
      managed = null;
      
      gc();

      assertTrue("expected some cleanup to have occurred: " + PerLookupService.destroys, PerLookupService.destroys > 0);
      assertTrue("expected some cleanup to have occurred: " + PerLookupServiceNested3.destroys, PerLookupServiceNested3.destroys > 0);
    }
  }

  @Test
  public void unmanagedPerLookups() throws Exception {
    if (!InjectInjectionResolver.MANAGED_ENABLED) {
      PerLookupService plsPrev = null;
      Inhabitant<PerLookupService> plsi;
      PerLookupService pls;
      for (int i = 0; i < ITERATIONS; i++) {
        plsi = h.getInhabitantByType(PerLookupService.class).scopedClone();
        pls = plsi.get();
        assertFalse(plsPrev == pls);
        plsPrev = pls;
        assertFalse(pls == plsi.get());
        
        assertEquals(i+1, PerLookupService.constructs);
        assertEquals(i+1, PerLookupServiceNested3.constructs);
        
        assertEquals("nothing should be managed", 0, AbstractInhabitantImpl.class.cast(plsi).getManagedCount());

        plsi.release();
      }

      gc();

      assertFalse("no cleanup should have occurred: " + PerLookupService.destroys, PerLookupService.destroys > 0);
      assertFalse("no cleanup should have occurred: : " + PerLookupServiceNested3.destroys, PerLookupServiceNested3.destroys > 0);
    }
  }

  // I'm not aware of any better way to do this
  private void gc() throws InterruptedException {
    System.gc();
    System.gc();
    Thread.sleep(100);
    System.gc();
    System.gc();
    Thread.sleep(100);
    System.gc();
    System.gc();
    System.runFinalization();
    Thread.sleep(100);
  }

  @Before
  public void reset() {
    PerLookupService.constructs = 0;
    PerLookupService.destroys = 0;
    PerLookupServiceNested3.constructs = 0;
    PerLookupServiceNested3.destroys = 0;
  }

}
