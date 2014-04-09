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
package org.glassfish.hk2.tests.locator.proxiable2;

import junit.framework.Assert;

import org.glassfish.hk2.api.ProxyCtl;
import org.glassfish.hk2.api.ServiceHandle;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.tests.locator.utilities.LocatorHelper;
import org.junit.Test;

/**
 * @author jwells
 *
 */
public class ProxiableTest {
    private final static String TEST_NAME = "Proxiable2Test";
    private final static ServiceLocator locator = LocatorHelper.create(TEST_NAME, new ProxiableModule());
    
    /**
     * Tests that I can have something in the singleton
     * scope that gets proxied
     */
    @Test
    public void testProxiedSingleton() {
        ProxiableService.resetConstructorCalled();
        
        ServiceHandle<ProxiableService> psHandle = locator.getServiceHandle(ProxiableService.class);
        Assert.assertNotNull(psHandle);
        
        try {
            ProxiableService ps = psHandle.getService();
        
            Assert.assertEquals(0, ProxiableService.getConstructorCalled());
        
            ps.doService();  // Forces true creation
        
            Assert.assertEquals(1, ProxiableService.getConstructorCalled());
        }
        finally {
            // Removes it from Singleton scope
            psHandle.destroy();
        }
    }
    
    /**
     * Tests that the proxied singleton service implements ProxyCtl
     */
    @Test
    public void testProxiedSingletonUsingProxyCtl() {
        ProxiableService2.resetConstructorCalled();
        
        ServiceHandle<ProxiableService2> psHandle = locator.getServiceHandle(ProxiableService2.class);
        Assert.assertNotNull(psHandle);
        
        try {
            ProxiableService2 ps = psHandle.getService();
        
            Assert.assertEquals(0, ProxiableService2.getConstructorCalled());
            
            Assert.assertTrue(ps instanceof ProxyCtl);
        
            ((ProxyCtl) ps).__make();  // Forces true creation
        
            Assert.assertEquals(1, ProxiableService2.getConstructorCalled());
        }
        finally {
            // Removes it from Singleton scope
            psHandle.destroy();
        }
    }
    
    /**
     * Test that the singleton context works
     */
    @Test
    public void testProxiedSingletonFromContext() {
        ProxiableServiceInContext.resetConstructorCalled();
        
        ServiceHandle<ProxiableServiceInContext> psHandle = locator.getServiceHandle(ProxiableServiceInContext.class);
        Assert.assertNotNull(psHandle);
        
        try {
            ProxiableServiceInContext ps = psHandle.getService();
        
            Assert.assertEquals(0, ProxiableServiceInContext.getConstructorCalled());
        
            ps.doService();  // Forces true creation
        
            Assert.assertEquals(1, ProxiableServiceInContext.getConstructorCalled());
        }
        finally {
            // Removes it from Singleton scope
            psHandle.destroy();
        }
    }
    
    /**
     * Tests that the ProxiableSingleton works, using ProxyCtl
     */
    @Test
    public void testProxiedSingletonInContextUsingProxyCtl() {
        ProxiableServiceInContext2.resetConstructorCalled();
        
        ServiceHandle<ProxiableServiceInContext2> ps2Handle = locator.getServiceHandle(ProxiableServiceInContext2.class);
        Assert.assertNotNull(ps2Handle);
        
        try {
            ProxiableServiceInContext2 ps2 = ps2Handle.getService();
        
            Assert.assertEquals(0, ProxiableServiceInContext2.getConstructorCalled());
            
            Assert.assertTrue(ps2 instanceof ProxyCtl);

            ((ProxyCtl) ps2).__make();  // Forces true creation
        
            Assert.assertEquals(1, ProxiableServiceInContext2.getConstructorCalled());
        }
        finally {
            // Removes it from Singleton scope
            ps2Handle.destroy();
        }
    }
    
    /**
     * Test that you can explicitly NOT be proxied from a proxiable context
     */
    @Test
    public void testNotProxiedSingletonFromContext() {
        NotProxiableService.resetConstructorCalled();
        
        ServiceHandle<NotProxiableService> psHandle = locator.getServiceHandle(NotProxiableService.class);
        Assert.assertNotNull(psHandle);
        
        Assert.assertEquals(0, NotProxiableService.getConstructorCalled());
        
        try {
            NotProxiableService ps = psHandle.getService();
        
            Assert.assertEquals(1, NotProxiableService.getConstructorCalled());
        
            Assert.assertFalse(ps instanceof ProxyCtl);
        }
        finally {
            // Removes it from Singleton scope
            psHandle.destroy();
        }
    }
    
    /**
     * Test that the singleton context works
     */
    @Test
    public void testProxiedServiceFromFactory() {
        ProxiableServiceFromFactory.resetConstructorCalled();
        
        ServiceHandle<ProxiableServiceFromFactory> psHandle = locator.getServiceHandle(ProxiableServiceFromFactory.class);
        Assert.assertNotNull(psHandle);
        
        try {
            ProxiableServiceFromFactory ps = psHandle.getService();
        
            Assert.assertEquals(0, ProxiableServiceFromFactory.getConstructorCalled());
        
            ps.doService();  // Forces true creation
        
            Assert.assertEquals(1, ProxiableServiceFromFactory.getConstructorCalled());
        }
        finally {
            // Removes it from Singleton scope
            psHandle.destroy();
        }
    }

}
