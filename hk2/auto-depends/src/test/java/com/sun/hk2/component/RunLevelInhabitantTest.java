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
package com.sun.hk2.component;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.component.ComponentException;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.component.MultiMap;
import org.jvnet.hk2.component.RunLevelState;
import org.jvnet.hk2.junit.Hk2Runner;

import com.sun.hk2.component.Holder;
import com.sun.hk2.component.LazyInhabitant;
import com.sun.hk2.component.RunLevelInhabitant;

@SuppressWarnings("unchecked")
@RunWith(Hk2Runner.class)
public class RunLevelInhabitantTest {

  @Inject
  Habitat h;
  
  final Holder<ClassLoader> clh = new Holder.Impl<ClassLoader>(RunLevelInhabitantTest.class.getClassLoader());

  final MultiMap<String,String> md = new MultiMap<String,String>();
  
  @Test
  public void testSufficientLevel() {
    RunLevelState state = new TestRunLevelState(5, 10);

    LazyInhabitant<?> i = new LazyInhabitant(h, clh, getClass().getName(), md);
    assertFalse(i.isInstantiated());
    RunLevelInhabitant rli = new RunLevelInhabitant(i, 5, state);
    assertEquals(getClass().getName(), rli.typeName());
    assertFalse(rli.isInstantiated());
    assertNotNull(rli.get());
    assertSame(rli.get(), rli.get());
    assertTrue(rli.isInstantiated());
    assertTrue(i.isInstantiated());
    assertEquals(getClass(), rli.type());
    rli.release();
    assertFalse(rli.isInstantiated());
    assertFalse(i.isInstantiated());
    assertSame(i.metadata(), rli.metadata());
  }

  @Test
  public void testInsufficientLevel() {
    RunLevelState state = new TestRunLevelState(null, 10);
    
    LazyInhabitant<?> i = new LazyInhabitant(h, clh, getClass().getName(), md);
    assertFalse(i.isInstantiated());
    RunLevelInhabitant rli = new RunLevelInhabitant(i, 15, state);
    assertEquals(getClass().getName(), rli.typeName());
    assertFalse(rli.isInstantiated());
    try {
      fail("expected exception but got: " + rli.get());
    } catch (ComponentException e) {
    }
    assertFalse(rli.isInstantiated());
    assertFalse(i.isInstantiated());
    assertSame(i.type(), rli.type());
    assertFalse(i.isInstantiated());
    // should have no affect
    rli.release();
    assertFalse(rli.isInstantiated());
    assertFalse(i.isInstantiated());
    assertSame(i.metadata(), rli.metadata());
  }
  
}
