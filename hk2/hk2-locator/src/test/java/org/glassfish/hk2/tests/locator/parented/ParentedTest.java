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
package org.glassfish.hk2.tests.locator.parented;

import java.util.List;

import junit.framework.Assert;

import org.glassfish.hk2.api.DynamicConfiguration;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.api.ServiceLocatorFactory;
import org.glassfish.hk2.tests.locator.utilities.LocatorHelper;
import org.glassfish.hk2.utilities.Binder;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author jwells
 *
 */
public class ParentedTest {
    private final ServiceLocatorFactory factory = ServiceLocatorFactory.getInstance();
    
    private final static String TEST_NAME = "ParentedTest";
    private final static ServiceLocator locator = LocatorHelper.create(TEST_NAME, new ParentedModule());
    
    private final static String GRANDPA = "Grandpa";
    private final static String DAD = "Dad";
    private final static String ME = "Me";
    
    private final static String CHILD1 = "Child1";
    private final static String CHILD2 = "Child2";
    
    /**
     * Tests three generations of locators
     */
    @Test
    public void testBasicParenting() {
        ServiceLocator grandpa = factory.create(GRANDPA);
        ServiceLocator dad = factory.create(DAD, grandpa);
        ServiceLocator me = factory.create(ME, dad);
        
        List<ServiceLocator> grandpaLocators = grandpa.getAllServices(ServiceLocator.class);
        List<ServiceLocator> dadLocators = dad.getAllServices(ServiceLocator.class);
        List<ServiceLocator> meLocators = me.getAllServices(ServiceLocator.class);
        
        Assert.assertNotNull(grandpaLocators);
        Assert.assertEquals(1, grandpaLocators.size());
        Assert.assertEquals(grandpa, grandpaLocators.get(0));
        
        Assert.assertNotNull(dadLocators);
        Assert.assertEquals(2, dadLocators.size());
        Assert.assertEquals(dad, dadLocators.get(0));
        Assert.assertEquals(grandpa, dadLocators.get(1));
        
        Assert.assertNotNull(meLocators);
        Assert.assertEquals(3, meLocators.size());
        Assert.assertEquals(me, meLocators.get(0));
        Assert.assertEquals(dad, meLocators.get(1));
        Assert.assertEquals(grandpa, meLocators.get(2));
        
        // Make sure the most normal case returns the right guy
        Assert.assertEquals(me, me.getService(ServiceLocator.class));
    }
    
    /**
     * Tests that a child getting shutdown will not adversly affect the services
     * in the parent
     */
    @Test
    public void testShutdownOfChild() {
        ServiceLocator child1 = factory.create(CHILD1, locator);
        
        ServiceLocatorUtilities.bind(child1, new Binder() {

            @Override
            public void bind(DynamicConfiguration config) {
                config.addActiveDescriptor(SingletonServiceInChild.class);
                
            }
            
        });
        
        SingletonServiceInParent ssip = child1.getService(SingletonServiceInParent.class);
        Assert.assertNotNull(ssip);
        Assert.assertFalse(ssip.isShutdown());
        
        SingletonServiceInChild ssic = child1.getService(SingletonServiceInChild.class);
        Assert.assertNotNull(ssic);
        Assert.assertFalse(ssic.isShutdown());
        
        // Shutting down the child should NOT shutdown the service in the parent
        factory.destroy(child1);
        
        Assert.assertFalse(ssip.isShutdown());
        Assert.assertTrue(ssic.isShutdown());
    }
    
    /**
     * First looks up a service in a parent-defined context
     * from the child, the closes the child and looks it up
     * again from the parent.
     */
    @Test @Ignore
    public void testContextInParent() {
        ServiceLocator child2 = factory.create(CHILD2, locator);
        
        ServiceWithParentContextInjectionPoint swpcip = child2.getService(ServiceWithParentContextInjectionPoint.class);
        Assert.assertNotNull(swpcip);
        
        factory.destroy(child2);
        
        swpcip = locator.getService(ServiceWithParentContextInjectionPoint.class);
        Assert.assertNotNull(swpcip);
    }
}
