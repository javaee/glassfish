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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.Assert;

import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.api.ServiceLocatorFactory;
import org.glassfish.hk2.api.ServiceLocatorListener;
import org.glassfish.hk2.tests.extension.ServiceLocatorImpl;
import org.junit.Test;

/**
 * @author jwells
 *
 */
public class ServiceLocatorFactoryTest {
  
  /**
   * Tests you can add a locator to the system
   */
  @Test
  public void testAddToServiceLocatorFactory() {
    ServiceLocatorFactory slf = ServiceLocatorFactory.getInstance();
    Assert.assertNotNull(slf);
    
    ServiceLocator sl = slf.create("AddtoServiceLocatorFactory");
    Assert.assertNotNull(sl);
  }
  
  private final static String FIND_FROM_SLF = "FindFromServiceLocatorFactory";
  /**
   * Tests that a locator is not removed after a find... uh, duh...
   */
  @Test
  public void testFindFromServiceLocatorFactory() {
    ServiceLocatorFactory slf = ServiceLocatorFactory.getInstance();
    slf.create(FIND_FROM_SLF);
    
    ServiceLocator sl = slf.find(FIND_FROM_SLF);
    Assert.assertNotNull(sl);
    
    // Do it a second time to make sure it didn't get removed
    sl = slf.find(FIND_FROM_SLF);
    Assert.assertNotNull(sl);
  }
  
  private final static String DELETE_FROM_SLF = "DeleteFromServiceLocatorFactory";
  /**
   * Tests that our dummy service locator is properly removed on shutdown
   */
  @Test
  public void testDeleteFromServiceLocatorFactory() {
    ServiceLocatorFactory slf = ServiceLocatorFactory.getInstance();
    ServiceLocator locator = slf.create(DELETE_FROM_SLF);
    if (!(locator instanceof ServiceLocatorImpl)) return;
    
    ServiceLocatorImpl sl = (ServiceLocatorImpl) locator;
    Assert.assertNotNull(sl);
    Assert.assertFalse(sl.isShutdown());
    
    slf.destroy(DELETE_FROM_SLF);
    Assert.assertTrue(sl.isShutdown());
    
    // Make sure it is really gone
    Assert.assertNull(slf.find(DELETE_FROM_SLF));
    
    // And that destroying it again does no damage
    slf.destroy(DELETE_FROM_SLF); 
  }
  
  private final static String SLF_LISTENER_1 = "ServiceLocatorFactoryListener1";
  private final static String SLF_LISTENER_2 = "ServiceLocatorFactoryListener2";
  /**
   * Tests that listeners can be established on the create/destroy process
   */
  @Test
  public void testListenerMethods() {
      ServiceLocatorFactory slf = ServiceLocatorFactory.getInstance();
      
      ServiceLocatorListenerImpl listener1 = new ServiceLocatorListenerImpl();
      
      slf.addListener(listener1);
      
      slf.create(SLF_LISTENER_1);
      slf.create(SLF_LISTENER_2);
      
      // Ensures a second listener can be added and will be initialized properly
      ServiceLocatorListenerImpl listener2 = new ServiceLocatorListenerImpl();
      slf.addListener(listener2);
      
      Set<String> names1 = listener1.getLocatorNames();
      
      Assert.assertTrue(names1.contains(SLF_LISTENER_1));
      Assert.assertTrue(names1.contains(SLF_LISTENER_2));
      
      Set<String> names2 = listener2.getLocatorNames();
      
      Assert.assertTrue(names2.contains(SLF_LISTENER_1));
      Assert.assertTrue(names2.contains(SLF_LISTENER_2));
      
      slf.destroy(SLF_LISTENER_1);
      
      Assert.assertFalse(names1.contains(SLF_LISTENER_1));
      Assert.assertTrue(names1.contains(SLF_LISTENER_2));
      
      Assert.assertFalse(names2.contains(SLF_LISTENER_1));
      Assert.assertTrue(names2.contains(SLF_LISTENER_2));
      
      slf.removeListener(listener1);
      
      slf.destroy(SLF_LISTENER_2);
      
      Assert.assertFalse(names1.contains(SLF_LISTENER_1));
      Assert.assertTrue(names1.contains(SLF_LISTENER_2));
      
      Assert.assertFalse(names2.contains(SLF_LISTENER_1));
      Assert.assertFalse(names2.contains(SLF_LISTENER_2));
      
      slf.removeListener(listener2);
  }
  
  private static class ServiceLocatorListenerImpl implements ServiceLocatorListener {
      private final HashSet<String> locators = new HashSet<String>();

      @Override
      public void initialize(Set<ServiceLocator> initialLocators) {
        for (ServiceLocator locator : initialLocators) {
            locators.add(locator.getName());
        }
        
      }

      @Override
      public void listenerAdded(ServiceLocator added) {
          locators.add(added.getName());
        
      }

      @Override
      public void listenerDestroyed(ServiceLocator destroyed) {
          locators.remove(destroyed.getName());
        
      }
      
      public Set<String> getLocatorNames() {
          return locators;
      }
      
  }
}
