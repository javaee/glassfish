/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2015 Oracle and/or its affiliates. All rights reserved.
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
package org.glassfish.hk2.tests.hk2bridge;

import org.glassfish.hk2.api.DynamicConfiguration;
import org.glassfish.hk2.api.DynamicConfigurationService;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.extras.ExtrasUtilities;
import org.glassfish.hk2.tests.extras.internal.Utilities;
import org.glassfish.hk2.utilities.BuilderHelper;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author jwells
 *
 */
public class BridgeTest {
    /**
     * Tests the hk2 to hk2 bridging feature
     */
    @Test // @org.junit.Ignore
    public void testBasicOneWayBridge() {
        ServiceLocator into = Utilities.getUniqueLocator();
        ServiceLocator from = Utilities.getUniqueLocator(SimpleService.class);
        
        Assert.assertNull(into.getService(SimpleService.class));
        ExtrasUtilities.bridgeServiceLocator(into, from);
        
        Assert.assertNotNull(into.getService(SimpleService.class));
    }
    
    /**
     * Tests that dynamic changes are captured in the from locator
     */
    @Test // @org.junit.Ignore
    public void testDynamicallyAddAndRemove() {
        ServiceLocator into = Utilities.getUniqueLocator();
        ServiceLocator from = Utilities.getUniqueLocator(SimpleService.class);
        
        ExtrasUtilities.bridgeServiceLocator(into, from);
        
        Assert.assertNotNull(into.getService(SimpleService.class));
        Assert.assertNull(into.getService(SimpleService2.class));
        Assert.assertNotNull(from.getService(SimpleService.class));
        Assert.assertNull(from.getService(SimpleService2.class));
        
        DynamicConfigurationService dcs = from.getService(DynamicConfigurationService.class);
        DynamicConfiguration config = dcs.createDynamicConfiguration();
        
        config.addActiveDescriptor(SimpleService2.class);
        config.addUnbindFilter(BuilderHelper.createContractFilter(SimpleService.class.getName()));
        
        config.commit();
        
        Assert.assertNull(into.getService(SimpleService.class));
        Assert.assertNotNull(into.getService(SimpleService2.class));
        Assert.assertNull(from.getService(SimpleService.class));
        Assert.assertNotNull(from.getService(SimpleService2.class));
    }
    
    /**
     * Tests that bridged services are represented in lists
     */
    @Test // @org.junit.Ignore
    public void testAllServicesGetsBoth() {
        ServiceLocator into = Utilities.getUniqueLocator(SimpleService.class);
        ServiceLocator from = Utilities.getUniqueLocator(SimpleService.class);
        
        Assert.assertEquals(1, into.getAllServices(SimpleService.class).size());
        Assert.assertEquals(1, from.getAllServices(SimpleService.class).size());
        
        ExtrasUtilities.bridgeServiceLocator(into, from);
        
        Assert.assertEquals(2, into.getAllServices(SimpleService.class).size());
        Assert.assertEquals(1, from.getAllServices(SimpleService.class).size());
    }
    
    /**
     * Tests that cycles can be done
     */
    @Test // @org.junit.Ignore
    public void testCycle() {
        ServiceLocator into = Utilities.getUniqueLocator(SimpleService.class, SimpleService3.class);
        ServiceLocator from = Utilities.getUniqueLocator(SimpleService2.class, SimpleService3.class);
        
        Assert.assertNotNull(into.getService(SimpleService.class));
        Assert.assertNull(into.getService(SimpleService2.class));
        Assert.assertNull(from.getService(SimpleService.class));
        Assert.assertNotNull(from.getService(SimpleService2.class));
        Assert.assertNotNull(into.getService(SimpleService3.class));
        Assert.assertNotNull(from.getService(SimpleService3.class));
        
        ExtrasUtilities.bridgeServiceLocator(into, from);
        ExtrasUtilities.bridgeServiceLocator(from, into);
        
        Assert.assertNotNull(into.getService(SimpleService.class));
        Assert.assertNotNull(into.getService(SimpleService2.class));
        Assert.assertNotNull(from.getService(SimpleService.class));
        Assert.assertNotNull(from.getService(SimpleService2.class));
        Assert.assertNotNull(into.getService(SimpleService3.class));
        Assert.assertNotNull(from.getService(SimpleService3.class));
        
        Assert.assertEquals(1, into.getAllServices(SimpleService.class).size());
        Assert.assertEquals(1, from.getAllServices(SimpleService.class).size());
        Assert.assertEquals(1, into.getAllServices(SimpleService2.class).size());
        Assert.assertEquals(1, from.getAllServices(SimpleService2.class).size());
        Assert.assertEquals(2, into.getAllServices(SimpleService3.class).size());
        Assert.assertEquals(2, from.getAllServices(SimpleService3.class).size());
        
    }
    
    /**
     * Tests that two locators can both bridge into a single other locator
     */
    @Test // @org.junit.Ignore
    public void testTwoBridges() {
        ServiceLocator into = Utilities.getUniqueLocator();
        ServiceLocator from1 = Utilities.getUniqueLocator(SimpleService.class);
        ServiceLocator from2 = Utilities.getUniqueLocator(SimpleService2.class);
        
        
        ExtrasUtilities.bridgeServiceLocator(into, from1);
        ExtrasUtilities.bridgeServiceLocator(into, from2);
        
        Assert.assertNotNull(into.getService(SimpleService.class));
        Assert.assertNotNull(into.getService(SimpleService2.class));
    }
    
    /**
     * Tests that locators can chain
     */
    @Test // @org.junit.Ignore
    public void testBridgeChain() {
        ServiceLocator into = Utilities.getUniqueLocator(SimpleService.class);
        ServiceLocator from1 = Utilities.getUniqueLocator(SimpleService2.class);
        ServiceLocator from2 = Utilities.getUniqueLocator(SimpleService3.class);
        
        ExtrasUtilities.bridgeServiceLocator(into, from1);
        ExtrasUtilities.bridgeServiceLocator(from1, from2);
        
        // The into locator should now have all services
        Assert.assertNotNull(into.getService(SimpleService.class));
        Assert.assertNotNull(into.getService(SimpleService2.class));
        Assert.assertNotNull(into.getService(SimpleService3.class));
        
        // The from1 locator should have Services 2 and 3
        Assert.assertNull(from1.getService(SimpleService.class));
        Assert.assertNotNull(from1.getService(SimpleService2.class));
        Assert.assertNotNull(from1.getService(SimpleService3.class));
        
        // The from2 locator should have Service3 only
        Assert.assertNull(from2.getService(SimpleService.class));
        Assert.assertNull(from2.getService(SimpleService2.class));
        Assert.assertNotNull(from2.getService(SimpleService3.class));
    }
    
    /**
     * Tests that locators can chain with a cycle
     */
    @Test // @org.junit.Ignore
    public void testBridgeChainWithCycle() {
        ServiceLocator into = Utilities.getUniqueLocator(SimpleService.class);
        ServiceLocator from1 = Utilities.getUniqueLocator(SimpleService2.class);
        ServiceLocator from2 = Utilities.getUniqueLocator(SimpleService3.class);
        
        ExtrasUtilities.bridgeServiceLocator(into, from1);
        ExtrasUtilities.bridgeServiceLocator(from1, from2);
        ExtrasUtilities.bridgeServiceLocator(from2, into);
        
        // The into locator should now have all services
        Assert.assertNotNull(into.getService(SimpleService.class));
        Assert.assertNotNull(into.getService(SimpleService2.class));
        Assert.assertNotNull(into.getService(SimpleService3.class));
        
        // The from1 locator should have all services
        Assert.assertNotNull(from1.getService(SimpleService.class));
        Assert.assertNotNull(from1.getService(SimpleService2.class));
        Assert.assertNotNull(from1.getService(SimpleService3.class));
        
        // The from2 locator should have all services
        Assert.assertNotNull(from2.getService(SimpleService.class));
        Assert.assertNotNull(from2.getService(SimpleService2.class));
        Assert.assertNotNull(from2.getService(SimpleService3.class));
        
        // Everybody should have one of each
        Assert.assertEquals(1, into.getAllServices(SimpleService.class).size());
        Assert.assertEquals(1, into.getAllServices(SimpleService2.class).size());
        Assert.assertEquals(1, into.getAllServices(SimpleService3.class).size());
        
        Assert.assertEquals(1, from1.getAllServices(SimpleService.class).size());
        Assert.assertEquals(1, from1.getAllServices(SimpleService2.class).size());
        Assert.assertEquals(1, from1.getAllServices(SimpleService3.class).size());
        
        Assert.assertEquals(1, from2.getAllServices(SimpleService.class).size());
        Assert.assertEquals(1, from2.getAllServices(SimpleService2.class).size());
        Assert.assertEquals(1, from2.getAllServices(SimpleService3.class).size());
        
    }
    
    /**
     * This type of cycle could cause problems:
     * a -> b -> c -> b
     */
    @Test // @org.junit.Ignore
    public void testBridgeChainWithInnerCycle() {
        ServiceLocator into = Utilities.getUniqueLocator(SimpleService.class);
        ServiceLocator from1 = Utilities.getUniqueLocator(SimpleService2.class);
        ServiceLocator from2 = Utilities.getUniqueLocator(SimpleService3.class);
        
        ExtrasUtilities.bridgeServiceLocator(into, from1);
        ExtrasUtilities.bridgeServiceLocator(from1, from2);
        ExtrasUtilities.bridgeServiceLocator(from1, into);
        
        // The into locator should now have all services
        Assert.assertNotNull(into.getService(SimpleService.class));
        Assert.assertNotNull(into.getService(SimpleService2.class));
        Assert.assertNotNull(into.getService(SimpleService3.class));
        
        // The from1 locator should have all services
        Assert.assertNotNull(from1.getService(SimpleService.class));
        Assert.assertNotNull(from1.getService(SimpleService2.class));
        Assert.assertNotNull(from1.getService(SimpleService3.class));
        
        // The from2 locator should have Service3 only
        Assert.assertNull(from2.getService(SimpleService.class));
        Assert.assertNull(from2.getService(SimpleService2.class));
        Assert.assertNotNull(from2.getService(SimpleService3.class));
        
        // Should be one of each in the into locator
        Assert.assertEquals(1, into.getAllServices(SimpleService.class).size());
        Assert.assertEquals(1, into.getAllServices(SimpleService2.class).size());
        Assert.assertEquals(1, into.getAllServices(SimpleService3.class).size());
        
        // Should be one of each in the from1 locator (which is the test, basically)
        Assert.assertEquals(1, from1.getAllServices(SimpleService.class).size());
        Assert.assertEquals(1, from1.getAllServices(SimpleService2.class).size());
        Assert.assertEquals(1, from1.getAllServices(SimpleService3.class).size());
        
        // Should only be one Service2 in from2
        Assert.assertEquals(0, from2.getAllServices(SimpleService.class).size());
        Assert.assertEquals(0, from2.getAllServices(SimpleService2.class).size());
        Assert.assertEquals(1, from2.getAllServices(SimpleService3.class).size());
        
    }

}
