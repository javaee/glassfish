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

import java.util.concurrent.Callable;
import java.util.logging.Logger;

import org.junit.Test;

public class SoftCacheTest {

  int count;
  
  @Test
  public void testIt() {
    SoftCache<String, Integer> cache = new SoftCache<String, Integer>();

    Callable<Integer> populator = new Callable<Integer>() {
      @Override
      public Integer call() throws Exception {
        return ++count;
      }
    };

    assertEquals(1, cache.get("x", populator).intValue());
    assertEquals(1, cache.get("x", populator).intValue());
    assertEquals(2, cache.get("y", populator).intValue());
    assertEquals(2, cache.get("y", populator).intValue());
    count++;
    assertEquals(2, cache.get("y", populator).intValue());
    int val = cache.get("z", populator).intValue();
    assertEquals(4, val);
    
    // hope for the best
    System.gc();
    System.gc();
    
    assertTrue(5 == val || 4 == val);
    if (4 == val) {
      Logger.getAnonymousLogger().fine("gc let us down");
    } else {
      Logger.getAnonymousLogger().finest("gc did not let us down");
    }
  }
}
