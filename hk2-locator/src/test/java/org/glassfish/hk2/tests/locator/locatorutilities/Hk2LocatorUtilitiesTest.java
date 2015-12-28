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
package org.glassfish.hk2.tests.locator.locatorutilities;

import java.util.List;

import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.tests.locator.utilities.LocatorHelper;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import org.junit.Assert;
import org.junit.Test;
import org.jvnet.hk2.external.runtime.Hk2LocatorUtilities;

/**
 * @author jwells
 *
 */
public class Hk2LocatorUtilitiesTest {
    /**
     * Ensures filter is empty if no user services were added
     */
    @Test // @org.junit.Ignore
    public void testSingleLocatorNoUserServices() {
        ServiceLocator locator = LocatorHelper.create();
        
        List<ActiveDescriptor<?>> descriptors = locator.getDescriptors(
                Hk2LocatorUtilities.getNoInitialServicesFilter());
        Assert.assertTrue(descriptors.isEmpty());
        
    }
    
    /**
     * Ensures filter is empty if a user services is added
     */
    @Test // @org.junit.Ignore
    public void testSingleLocatorWithUserServices() {
        ServiceLocator locator = LocatorHelper.getServiceLocator(SimpleService.class);
        
        List<ActiveDescriptor<?>> descriptors = locator.getDescriptors(
                Hk2LocatorUtilities.getNoInitialServicesFilter());
        Assert.assertFalse(descriptors.isEmpty());
        Assert.assertEquals(1, descriptors.size());
        
        ActiveDescriptor<?> descriptor = descriptors.get(0);
        
        Assert.assertEquals(descriptor.getImplementationClass(), SimpleService.class);
        
    }
    
    /**
     * Ensures filter is empty in a child if no services in parent or child
     */
    @Test // @org.junit.Ignore
    public void testChildLocatorNoUserServices() {
        ServiceLocator locator = LocatorHelper.getServiceLocator();
        ServiceLocator child = LocatorHelper.create(locator);
        
        List<ActiveDescriptor<?>> descriptors = child.getDescriptors(
                Hk2LocatorUtilities.getNoInitialServicesFilter());
        Assert.assertTrue(descriptors.isEmpty());
    }
    
    /**
     * Ensures filter is empty in a child if there is a service in the parent
     */
    @Test // @org.junit.Ignore
    public void testChildLocatorUserServiceInParent() {
        ServiceLocator locator = LocatorHelper.getServiceLocator(SimpleService.class);
        ServiceLocator child = LocatorHelper.create(locator);
        
        List<ActiveDescriptor<?>> descriptors = child.getDescriptors(
                Hk2LocatorUtilities.getNoInitialServicesFilter());
        Assert.assertFalse(descriptors.isEmpty());
        Assert.assertEquals(1, descriptors.size());
        
        ActiveDescriptor<?> descriptor = descriptors.get(0);
        
        Assert.assertEquals(descriptor.getImplementationClass(), SimpleService.class);
    }
    
    /**
     * Ensures filter is not empty if there is a service in the child
     */
    @Test // @org.junit.Ignore
    public void testChildLocatorUserServiceInChild() {
        ServiceLocator locator = LocatorHelper.getServiceLocator();
        ServiceLocator child = LocatorHelper.create(locator);
        ServiceLocatorUtilities.addClasses(child, SimpleService.class);
        
        List<ActiveDescriptor<?>> parentDescriptors = locator.getDescriptors(
                Hk2LocatorUtilities.getNoInitialServicesFilter());
        
        Assert.assertTrue(parentDescriptors.isEmpty());
        
        List<ActiveDescriptor<?>> descriptors = child.getDescriptors(
                Hk2LocatorUtilities.getNoInitialServicesFilter());
        Assert.assertFalse(descriptors.isEmpty());
        Assert.assertEquals(1, descriptors.size());
        
        ActiveDescriptor<?> descriptor = descriptors.get(0);
        
        Assert.assertEquals(descriptor.getImplementationClass(), SimpleService.class);
    }
    
    /**
     * Ensures filter is not empty if there is a service in the child
     */
    @Test // @org.junit.Ignore
    public void testChildLocatorUserServicesInBoth() {
        ServiceLocator locator = LocatorHelper.getServiceLocator(SimpleService.class);
        ServiceLocator child = LocatorHelper.create(locator);
        ServiceLocatorUtilities.addClasses(child, SimpleService.class);
        
        List<ActiveDescriptor<?>> parentDescriptors = locator.getDescriptors(
                Hk2LocatorUtilities.getNoInitialServicesFilter());
        Assert.assertFalse(parentDescriptors.isEmpty());
        Assert.assertEquals(1, parentDescriptors.size());
        
        ActiveDescriptor<?> descriptorP0 = parentDescriptors.get(0);
        
        Assert.assertEquals(descriptorP0.getImplementationClass(), SimpleService.class);
        
        List<ActiveDescriptor<?>> descriptors = child.getDescriptors(
                Hk2LocatorUtilities.getNoInitialServicesFilter());
        Assert.assertFalse(descriptors.isEmpty());
        Assert.assertEquals(2, descriptors.size());
        
        ActiveDescriptor<?> descriptorC0 = descriptors.get(0);
        ActiveDescriptor<?> descriptorC1 = descriptors.get(1);
        
        Assert.assertEquals(descriptorC0.getImplementationClass(), SimpleService.class);
        Assert.assertEquals(descriptorC1.getImplementationClass(), SimpleService.class);
    }

}
