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
package org.glassfish.hk2.tests;

import junit.framework.Assert;

import org.glassfish.hk2.api.Configurator;
import org.glassfish.hk2.api.Module;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.api.ServiceLocatorFactory;
import org.glassfish.hk2.tests.extension.ServiceLocatorImpl;
import org.junit.Test;

/**
 * @author jwells
 *
 */
public class ServiceLocatorFactoryTest {
  private final static String LOCATOR_NAME = "Locator1";
  
  @Test
  public void testAddToServiceLocatorFactory() {
    ServiceLocatorFactory slf = ServiceLocatorFactory.getInstance();
    Assert.assertNotNull(slf);
    
    ServiceLocator sl = slf.create(LOCATOR_NAME, new TestModule());
    Assert.assertNotNull(sl);
  }
  
  @Test
  public void testFindFromServiceLocatorFactory() {
    ServiceLocatorFactory slf = ServiceLocatorFactory.getInstance();
    slf.create(LOCATOR_NAME, new TestModule());
    
    ServiceLocator sl = slf.find(LOCATOR_NAME);
    Assert.assertNotNull(sl);
    
    // Do it a second time to make sure it didn't get removed
    sl = slf.find(LOCATOR_NAME);
    Assert.assertNotNull(sl);
  }
  
  @Test
  public void testDeleteFromServiceLocatorFactory() {
    ServiceLocatorFactory slf = ServiceLocatorFactory.getInstance();
    ServiceLocatorImpl sl = (ServiceLocatorImpl) slf.create(LOCATOR_NAME, new TestModule());
    if (sl == null) {
      sl = (ServiceLocatorImpl) slf.find(LOCATOR_NAME);
    }
    
    Assert.assertNotNull(sl);
    Assert.assertFalse(sl.isShutdown());
    
    sl = (ServiceLocatorImpl) slf.destroy(LOCATOR_NAME);
    Assert.assertTrue(sl.isShutdown());
    
    Assert.assertNull(slf.destroy(LOCATOR_NAME));
    Assert.assertNull(slf.find(LOCATOR_NAME));
  }
  
  private static class TestModule implements Module {

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.Module#configure(org.glassfish.hk2.api.Configurator)
     */
    @Override
    public void configure(Configurator configurator) {
      // Do nothing, not testing the bindings or anything
      
    }
    
  }

}
