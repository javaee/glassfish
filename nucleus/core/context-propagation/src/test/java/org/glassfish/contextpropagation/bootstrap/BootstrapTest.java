/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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

package org.glassfish.contextpropagation.bootstrap;

import static org.junit.Assert.assertNotNull;
//import mockit.Deencapsulation;

import org.glassfish.contextpropagation.spi.ContextMapHelper;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.tests.utils.ConfigApiTest;
import org.junit.Test;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.jvnet.hk2.annotations.Contract;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.DomDocument;
import org.jvnet.hk2.junit.Hk2Runner;

@Ignore
@RunWith(Hk2Runner.class)
public class BootstrapTest extends ConfigApiTest {

  /*@Inject
  static RandomContract rci;
  @Inject 
  static DependencyProvider dp;*/
  
//  @Test
//  public void test() {
//    assertNotNull(ContextMapHelper.getScopeAwareContextMap());
//    Object o = Deencapsulation.getField(ContextBootstrap.class, "dependencyProvider"); 
//    System.out.println("DependencyProvider: " + o);
//    assertNotNull(o);
//    assertNotNull(ContextBootstrap.getGuid());
//  }
  
  @Service
  public static class RandomService implements RandomContract {
      public int add(int i, int j) {
          return i+j;
      }
  }
  
  @Contract
  public interface RandomContract {

      /**
       * Adds the two paramters and return the result
       * @param i the first element to add
       * @param j the second
       * @return the addition of i and j
       */
      int add(int i, int j);
  }
  
  @Override
  public DomDocument<?> getDocument(ServiceLocator habitat) {
    return null;
  }
  
}
