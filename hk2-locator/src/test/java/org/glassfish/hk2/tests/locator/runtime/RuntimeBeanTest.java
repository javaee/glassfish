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
package org.glassfish.hk2.tests.locator.runtime;

import java.util.List;

import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.tests.locator.utilities.LocatorHelper;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import org.junit.Assert;
import org.junit.Test;
import org.jvnet.hk2.external.runtime.ServiceLocatorRuntimeBean;

/**
 * @author jwells
 *
 */
public class RuntimeBeanTest {
    /**
     * Tests that the number of descriptors
     * is correct
     */
    @Test // @org.junit.Ignore
    public void testNumberOfDescriptors() {
        ServiceLocator locator = LocatorHelper.create();
        ServiceLocatorRuntimeBean bean = locator.getService(ServiceLocatorRuntimeBean.class);
        
        int numDescriptors = bean.getNumberOfDescriptors();
        
        List<ActiveDescriptor<?>> descriptors = ServiceLocatorUtilities.addClasses(locator, SimpleService.class, SimpleService.class);
        
        int postAddNumDescriptors = bean.getNumberOfDescriptors();
        
        Assert.assertEquals(numDescriptors + 2, postAddNumDescriptors);
        
        ServiceLocatorUtilities.removeOneDescriptor(locator, descriptors.get(1));
        
        int postRemoveNumDescriptors = bean.getNumberOfDescriptors();
        
        Assert.assertEquals(numDescriptors + 1, postRemoveNumDescriptors);
    }
    
    /**
     * Tests the number of children statistic
     */
    @Test // @org.junit.Ignore
    public void testNumberOfChildren() {
        ServiceLocator locator = LocatorHelper.create();
        ServiceLocatorRuntimeBean bean = locator.getService(ServiceLocatorRuntimeBean.class);
        
        int numChildren = bean.getNumberOfChildren();
        
        Assert.assertEquals(0, numChildren);
        
        ServiceLocator child = LocatorHelper.create(locator);
        
        numChildren = bean.getNumberOfChildren();
        
        Assert.assertEquals(1, numChildren);
        
        ServiceLocator grandChild = LocatorHelper.create(child);
        
        numChildren = bean.getNumberOfChildren();
        
        Assert.assertEquals(1, numChildren);
        
        grandChild.shutdown();
        
        numChildren = bean.getNumberOfChildren();
        
        Assert.assertEquals(1, numChildren);
        
        child.shutdown();
        
        numChildren = bean.getNumberOfChildren();
        
        Assert.assertEquals(0, numChildren);
        
    }
    
    /**
     * Tests that the service cache can be zeroed
     */
    @Test // @org.junit.Ignore
    public void testServiceCacheClear() {
        ServiceLocator locator = LocatorHelper.create();
        ServiceLocatorRuntimeBean bean = locator.getService(ServiceLocatorRuntimeBean.class);
        
        int maxCacheSize = bean.getServiceCacheMaximumSize();
        int cacheSize = bean.getServiceCacheSize();
        
        Assert.assertTrue(maxCacheSize >= cacheSize);
        
        ServiceLocatorUtilities.addClasses(locator, SimpleService.class);
        
        Assert.assertNotNull(locator.getService(SimpleService.class));
        
        int postLookupCacheSize = bean.getServiceCacheSize();
        
        Assert.assertTrue(postLookupCacheSize > cacheSize);
        
        // Lets see if the cache is working
        Assert.assertNotNull(locator.getService(SimpleService.class));
        
        int postDuexLookupCacheSize = bean.getServiceCacheSize();
        
        Assert.assertEquals(postLookupCacheSize, postDuexLookupCacheSize);
        
        bean.clearServiceCache();
        
        Assert.assertEquals(0, bean.getServiceCacheSize());
        
        Assert.assertNotNull(locator.getService(SimpleService.class));
        
        Assert.assertEquals(1, bean.getServiceCacheSize());
    }
    
    /**
     * Tests that the service cache can be zeroed
     */
    @Test // @org.junit.Ignore
    public void testReflectionCacheClear() {
        ServiceLocator locator = LocatorHelper.create();
        ServiceLocatorRuntimeBean bean = locator.getService(ServiceLocatorRuntimeBean.class);
        
        int cacheSize = bean.getReflectionCacheSize();
        
        List<ActiveDescriptor<?>> descriptors = ServiceLocatorUtilities.addClasses(locator, SimpleService.class);
        
        Assert.assertNotNull(locator.getService(SimpleService.class));
        
        int postLookupCacheSize = bean.getReflectionCacheSize();
        
        Assert.assertTrue(postLookupCacheSize > cacheSize);
        
        bean.clearReflectionCache();
        
        Assert.assertEquals(0, bean.getReflectionCacheSize());
        
        ServiceLocatorUtilities.removeOneDescriptor(locator, descriptors.get(0));
        ServiceLocatorUtilities.addClasses(locator, SimpleService.class);
        
        Assert.assertNotNull(locator.getService(SimpleService.class));
        
        Assert.assertTrue(bean.getReflectionCacheSize() > 0);
    }

}
