/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2013-2015 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.hk2.tests.locator.extras.contextclassloader;

import org.glassfish.hk2.api.ServiceHandle;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.api.ServiceLocatorFactory;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author jwells
 *
 */
public class ContextClassLoaderTest {
    private ServiceLocator locator;
    
    /**
     * Called prior to the tests
     */
    @Before
    public void before() {
        locator = ServiceLocatorFactory.getInstance().create(null);
        
        ServiceLocatorUtilities.addClasses(locator,
                CCLChangingService.class,
                ServiceA.class);
        
    }
    
    /**
     * Tests that the locator is CCL neutral by default
     */
    @Test
    public void testCCLNeutral() {
        ServiceHandle<?> handle = locator.getServiceHandle(CCLChangingService.class);
        
        ClassLoader cclClassLoader = new MyClassLoader();
        Thread.currentThread().setContextClassLoader(cclClassLoader);
        
        try {
            /**
             * Will be CCL neutral
             */
            handle.getService();
        
            Assert.assertEquals(cclClassLoader, Thread.currentThread().getContextClassLoader());
            
            handle.destroy();
            
            Assert.assertEquals(cclClassLoader, Thread.currentThread().getContextClassLoader());
        }
        finally {
            Thread.currentThread().setContextClassLoader(null);   
        }
    }
    
    /**
     * Tests that you can make the locator non CCL neutral
     */
    @Test
    public void testCCLNotNeutral() {
        locator.setNeutralContextClassLoader(false);
        
        ServiceHandle<?> handle = locator.getServiceHandle(CCLChangingService.class);
        
        ClassLoader cclClassLoader = new MyClassLoader();
        Thread.currentThread().setContextClassLoader(cclClassLoader);
        
        try {
            /**
             * Will be CCL neutral
             */
            handle.getService();
        
            ClassLoader currentCCL = Thread.currentThread().getContextClassLoader();
            Assert.assertNotSame(cclClassLoader, currentCCL);
            
            handle.destroy();
            
            Assert.assertNotSame(currentCCL, Thread.currentThread().getContextClassLoader());
        }
        finally {
            Thread.currentThread().setContextClassLoader(null);
            locator.setNeutralContextClassLoader(true);
        }
    }
    
    /**
     * Tests the raw operations are naturally neutral
     */
    @Test
    public void testRawOperationsNeutral() {
        ClassLoader cclClassLoader = new MyClassLoader();
        Thread.currentThread().setContextClassLoader(cclClassLoader);
        
        try {
            Object o = locator.create(CCLChangingService.class);
        
            Assert.assertEquals(cclClassLoader, Thread.currentThread().getContextClassLoader());
            
            locator.inject(o);
            
            Assert.assertEquals(cclClassLoader, Thread.currentThread().getContextClassLoader());
            
            locator.postConstruct(o);
            
            Assert.assertEquals(cclClassLoader, Thread.currentThread().getContextClassLoader());
            
            locator.preDestroy(o);
            
            Assert.assertEquals(cclClassLoader, Thread.currentThread().getContextClassLoader());
        }
        finally {
            Thread.currentThread().setContextClassLoader(null);
        }
    }
    
    /**
     * Tests the raw operations are naturally neutral
     */
    @Test
    public void testRawOperationsCanNotBeNeutral() {
        locator.setNeutralContextClassLoader(false);
        
        ClassLoader cclClassLoader = new MyClassLoader();
        Thread.currentThread().setContextClassLoader(cclClassLoader);
        
        try {
            Object o = locator.create(CCLChangingService.class);
        
            ClassLoader currentCCL = Thread.currentThread().getContextClassLoader();
            Assert.assertNotSame(cclClassLoader, currentCCL);
            
            locator.inject(o);
            
            Assert.assertNotSame(currentCCL, Thread.currentThread().getContextClassLoader());
            currentCCL = Thread.currentThread().getContextClassLoader();
            
            locator.postConstruct(o);
            
            Assert.assertNotSame(currentCCL, Thread.currentThread().getContextClassLoader());
            currentCCL = Thread.currentThread().getContextClassLoader();
            
            locator.preDestroy(o);
            
            Assert.assertNotSame(currentCCL, Thread.currentThread().getContextClassLoader());
        }
        finally {
            Thread.currentThread().setContextClassLoader(null);
            locator.setNeutralContextClassLoader(true);
        }
    }
    
    private static class MyClassLoader extends ClassLoader {
        
    }

}
