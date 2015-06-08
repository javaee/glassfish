/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012-2015 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.hk2.tests.locator.proxiable3;

import org.glassfish.hk2.api.ProxyCtl;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.tests.locator.utilities.LocatorHelper;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author jwells
 *
 */
public class Proxiable3Test {
    private final static String TEST_NAME = "Proxiable3Test";
    private final static ServiceLocator locator = LocatorHelper.create(TEST_NAME, new Proxiable3Module());
    
    /**
     * Tests I can lookup the same service with different proxiable interfaces
     */
    @Test
    public void testProxyViaInterfaceLookup() {
        Control control = locator.getService(Control.class);
        Assert.assertNotNull(control);
        
        control.resetInvocations();
        
        Foo foo = locator.getService(Foo.class);
        
        foo.foo();
        
        Bar bar = locator.getService(Bar.class);
        
        bar.bar();
        bar.bar();
        
        Assert.assertSame(1, control.getFooInvocations());
        Assert.assertSame(2, control.getBarInvocations());
        
        control.resetInvocations();
    }
    
    /**
     * Tests that proxies work when being injected into another object
     */
    @Test
    public void testProxyViaInterfaceInjection() {
        InjectedWithProxiesService injected = locator.getService(InjectedWithProxiesService.class);
        
        Assert.assertTrue(injected.didPass());
    }
    
    /**
     * Ensures that the interface proxies implement ProxyCtl
     */
    @Test
    public void testProxyViaInterfaceLookupImplementProxyCtl() {
        Control control = locator.getService(Control.class);
        Foo foo = locator.getService(Foo.class);
        Bar bar = locator.getService(Bar.class);
        
        Assert.assertTrue(control instanceof ProxyCtl);
        Assert.assertTrue(foo instanceof ProxyCtl);
        Assert.assertTrue(bar instanceof ProxyCtl);
    }
    
    /**
     * Ensures that the interface proxies implement ProxyCtl
     */
    @Test
    public void testProxyViaInterfaceInjectionImplementProxyCtl() {
        InjectedWithProxiesService injected = locator.getService(InjectedWithProxiesService.class);
        
        injected.areProxies();
    }
    
    /**
     * Ensures that an interface proxy works with a factory produced type
     */
    @Test
    public void testProxiedViaFactory() {
        FactoryImpl factory = locator.getService(FactoryImpl.class);
        Assert.assertFalse(factory.getProvideCalled());
        
        FactoryProducedFoo foo = locator.getService(FactoryProducedFoo.class);
        Assert.assertNotNull(foo);
        
        Assert.assertFalse(factory.getProvideCalled());
        
        ProxyCtl ctl = (ProxyCtl) foo;
        
        ctl.__make();
        
        Assert.assertTrue(factory.getProvideCalled());
    }

}
