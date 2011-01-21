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

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static org.junit.Assert.*;
import org.junit.Ignore;
import org.junit.Test;

/**
 * MultiMap Tests.
 * 
 * @author Jeff Trent
 */
public class MultiMapTest {
  MultiMap<String, String> mm = new MultiMap<String, String>(false);
  MultiMap<String, String> mmc = new MultiMap<String, String>(true);

  @Ignore
  public void testGet_returnsReadOnlyMap() throws Exception {
    runTestGet_returnsReadOnlyMap(mm);
    runTestGet_returnsReadOnlyMap(mmc);
  }

  protected void runTestGet_returnsReadOnlyMap(MultiMap<String, String> mm) {
    List<String> list = mm.get("key");
    try {
      fail("add expected to fail: " + list.add("x"));
    } catch (Exception e) {
      // expected
    }

    mm.add("key", "val");
    list = mm.get("key");
    assertNotNull(list);
    assertEquals(1, list.size());
    try {
      fail("remove expected to fail: " + list.remove(0));
    } catch (Exception e) {
      // expected
    }
    try {
      fail("add expected to fail: " + list.add("x"));
    } catch (Exception e) {
      // expected
    }
  }

  @Test
  public void testRemove_KV() throws Exception {
    runTestRemove_KV(mm);
    runTestRemove_KV(mmc);
  }

  protected void runTestRemove_KV(MultiMap<String, String> mm) {
    String val = new String("val");
    assertFalse(mm.remove("key", val));
    mm.add("key", "val");
    mm.add("key", "val2");
    assertTrue(mm.remove("key", val));
    assertFalse(mm.remove("key", val));
    assertTrue(mm.remove("key", "val2"));
  }
  
  @Test
  public void testContainsKV() {
    runContainsKV(mm);
    runContainsKV(mmc);
  }

  protected void runContainsKV(MultiMap<String, String> mm) {
    assertFalse(mm.contains("a", "b"));
    mm.add("a", "b");
    assertFalse(mm.contains("a", null));
    assertFalse(mm.contains("a", "a"));
    assertTrue(mm.contains("a", "b"));
    assertFalse(mm.contains("b", "a"));
  }

  @Test
  public void testKeySet() {
    MultiMap<String, String> mm = new MultiMap<String, String>();
    mm.add("key", "val");
    mm.add("key", "val2");
    mm.add("key2", "val");
    mm.add("key3", "val2");
    
    assertEquals("keySet", new HashSet(Arrays.asList(new String[] {"key", "key2", "key3"})), mm.keySet());
  }
}
