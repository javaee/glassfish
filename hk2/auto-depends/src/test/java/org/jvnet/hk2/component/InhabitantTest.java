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

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.jvnet.hk2.test.impl.OneSimple;

import com.sun.hk2.component.ExistingSingletonInhabitant;
import com.sun.hk2.component.LazyInhabitant;

import com.sun.hk2.component.Holder;

/**
 * General Inhabitant-type testing.
 * 
 * @author Jeff Trent
 */
@SuppressWarnings("unchecked")
public class InhabitantTest {

  @Test
  public void verifyInstancingAndInhabitatRequested() {
    int inhabitantRequested = 0;
    
    for (Inhabitant i : createTestInhabitants()) {
      assertSame(i.toString(), i.get(), i.get());
      
      Object obj = i.get();
      if (InhabitantRequested.class.isInstance(obj)) {
        assertSame(OneSimple.class, obj.getClass());
        OneSimple os = OneSimple.class.cast(obj);
        assertNotNull(os.self);
        inhabitantRequested++;
        
        os.self = null;
        assertSame(i.toString(), obj, i.get());
        assertNull(os.self);
      }
    }
    
    assertTrue(inhabitantRequested > 0);
  }

  /**
   * Verifies the behavior in post construct ComponentException
   */
  @Test
  public void postConstructFailure() {
    Habitat h = new Habitat();
    Holder<ClassLoader> cl = new Holder.Impl(getClass().getClassLoader());
    LazyInhabitant li = new LazyInhabitant(h, cl,
        AFailingPostConstructService.class.getName(),
        new MultiMap<String, String>());
    h.add(li);
    h.addIndex(li, PostConstruct.class.getName(), "test");
    
    try {
      Object obj = h.getComponent(PostConstruct.class.getName(), "test");
      fail("expected ComponentException but got: " + obj);
    } catch (ComponentException e) {
      // expected
    }
    
    Inhabitant i = h.getInhabitantByContract(PostConstruct.class.getName(), "test");
    assertSame(li, i);
    assertFalse(i.isInstantiated());
  }
  
  List<Inhabitant<?>> createTestInhabitants() {
    Habitat h = new Habitat();
    
    ArrayList<Inhabitant<?>> list = new ArrayList<Inhabitant<?>>();
    list.add(new ExistingSingletonInhabitant(this));
    
    Holder<ClassLoader> cl = new Holder.Impl(getClass().getClassLoader());
    LazyInhabitant li = new LazyInhabitant(h, cl,
        OneSimple.class.getName(),
        new MultiMap<String, String>());
    list.add(li);

    return list;
  }


  public static class AFailingPostConstructService implements PostConstruct {

    @Override
    public void postConstruct() {
      throw new ComponentException("forced exception in test");
    }
    
  }
}
