/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012 Oracle and/or its affiliates. All rights reserved.
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
package org.glassfish.hk2.tests.basic.servicelocator.extensions;

import junit.framework.Assert;

import org.glassfish.hk2.api.Configurator;
import org.glassfish.hk2.api.Module;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.api.ServiceLocatorFactory;
import org.glassfish.hk2.utilities.BuilderHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author jwells
 *
 */
public class ScopeTest {
  private final static String SERVICE_LOCATOR_NAME = "testScopeModule";
  private ServiceLocator locator;
  
  @Before
  public void setup() {
    if (locator != null) return;
    
    locator = ServiceLocatorFactory.getInstance().create(SERVICE_LOCATOR_NAME, new Module() {

      @Override
      public void configure(Configurator configurator) {
        configurator.bindScope(new CustomScopeA());
        
        configurator.bind(BuilderHelper.link(ServiceA.class).in(CustomScopeA.class).build());
      }
      
    });
    
  }
  
  @After
  public void after() {
    ServiceLocatorFactory.getInstance().destroy(SERVICE_LOCATOR_NAME);
    locator = null;
  }
  
  @Test
  public void testGetSameObjectFromSameScope() {
    CustomScopeA scopeA = locator.getService(CustomScopeA.class);
    Assert.assertNotNull(scopeA);
    
    scopeA.startMe();
    
    ServiceA a1 = locator.getService(ServiceA.class);
    Assert.assertNotNull(a1);
    
    ServiceA a2 = locator.getService(ServiceA.class);
    Assert.assertNotNull(a2);
    
    Assert.assertEquals(a1, a2);
    
    scopeA.stopMe();
  }
  
  @Test
  public void testNotInScopeFails() {
    CustomScopeA scopeA = locator.getService(CustomScopeA.class);
    Assert.assertNotNull(scopeA);
    
    try {
      locator.getService(ServiceA.class);
    }
    catch (IllegalStateException ise) {
        String message = ise.getMessage();
        Assert.assertNotNull("IllegalStateException has no error message", message);
        Assert.assertEquals("Got invalid error string " + message, message, CustomScopeA.ERROR_MESSAGE);
        return;
    }
    
    Assert.fail("getService should have failed, not in scope");
  }

}
