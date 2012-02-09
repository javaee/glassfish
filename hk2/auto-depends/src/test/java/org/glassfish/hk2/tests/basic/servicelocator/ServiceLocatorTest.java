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
package org.glassfish.hk2.tests.basic.servicelocator;

import java.util.List;

import org.glassfish.hk2.TypeLiteral;
import org.glassfish.hk2.api.Configurator;
import org.glassfish.hk2.api.Module;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.api.ServiceLocatorFactory;
import org.glassfish.hk2.utilities.BuilderHelper;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author jwells
 *
 */
public class ServiceLocatorTest {
  private final static String SERVICE_LOCATOR_NAME = "testModule";
  private ServiceLocator locator;
  
  @Before
  public void setup() {
    locator = ServiceLocatorFactory.getInstance().create(SERVICE_LOCATOR_NAME, new Module() {

      @Override
      public void configure(Configurator configurator) {
        configurator.bind(BuilderHelper.link(ServiceA.class).build());
        configurator.bind(BuilderHelper.link(ServiceB.class).withContract(ContractB.class).build());
        configurator.bind(BuilderHelper.link(ServiceC1.class).withContract(ContractC.class).build());
        configurator.bind(BuilderHelper.link(ServiceC2.class).withContract(ContractC.class).build());
        configurator.bind(BuilderHelper.link(ServiceD.class).
            withContract(ContractD1.class).
            withContract(ContractD2.class).build());
        configurator.bind(BuilderHelper.link(ServiceE.class).build());
      }
      
    });
    
  }
  
  @After
  public void after() {
    ServiceLocatorFactory.getInstance().destroy(SERVICE_LOCATOR_NAME);
    locator = null;
  }
  
  @Test
  public void testServiceWithImplOnly() {
    ServiceA a = locator.getService(ServiceA.class);
    Assert.assertNotNull(a);
  }
  
  @Test
  public void testServiceWithImplAndContract() {
    ServiceB b = locator.getService(ServiceB.class);
    Assert.assertNotNull(b);
    
    ContractB cb = locator.getService(ContractB.class);
    Assert.assertNotNull(cb);
  }
  
  @Test
  public void testMultipleServicesWithSameContract() {
    List<ContractC> allC = locator.getAllServices(ContractC.class);
    Assert.assertNotNull(allC);
    Assert.assertTrue(allC.size() == 2);
    
    boolean foundC1 = false;
    boolean foundC2 = false;
    for (ContractC aC : allC) {
      if (ServiceC1.class.equals(aC.getClass())) {
        Assert.assertFalse(foundC1);  // Should only be here once
        foundC1 = true;
      }
      
      if (ServiceC2.class.equals(aC.getClass())) {
        Assert.assertFalse(foundC2);  // Should only be here once
        foundC2 = true;
      }
      
    }
    
    Assert.assertTrue(foundC1);
    Assert.assertTrue(foundC2);
  }
  
  @Test
  public void testServiceWithMultipleContracts() {
    ContractD1 d1 = locator.getService(ContractD1.class);
    Assert.assertNotNull(d1);
    
    ContractD2 d2 = locator.getService(ContractD2.class);
    Assert.assertNotNull(d2);
  }
  
  @Test
  public void testServiceInjectedWithContractAndService() {
    ServiceE e = locator.getService(ServiceE.class);
    Assert.assertNotNull(e);
    
    Assert.assertNotNull(e.getB());
    Assert.assertNotNull(e.getD());
  }
}
