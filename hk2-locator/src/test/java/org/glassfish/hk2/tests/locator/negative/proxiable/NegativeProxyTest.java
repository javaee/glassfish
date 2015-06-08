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

package org.glassfish.hk2.tests.locator.negative.proxiable;

import org.glassfish.hk2.api.DynamicConfiguration;
import org.glassfish.hk2.api.DynamicConfigurationService;
import org.glassfish.hk2.api.MultiException;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.hk2.api.ProxyCtl;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.tests.locator.utilities.LocatorHelper;
import org.glassfish.hk2.utilities.AbstractActiveDescriptor;
import org.glassfish.hk2.utilities.ActiveDescriptorBuilder;
import org.glassfish.hk2.utilities.BuilderHelper;
import org.glassfish.hk2.utilities.DescriptorImpl;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author jwells
 *
 */
public class NegativeProxyTest {
    private final static String TEST_NAME = "NegativeProxyTest";
    private final static ServiceLocator locator = LocatorHelper.create(TEST_NAME, new NegativeProxyModule());
    
    /**
     * Tests that a scope marked {@link Unproxiable} cannot have isProxiable set to true
     */
    @Test
    public void testBadProxiable() {
        DescriptorImpl di = BuilderHelper.link(SimpleService.class.getName()).
            in(PerLookup.class.getName()).
            proxy(true).
            build();
        
        try {
            locator.reifyDescriptor(di);
            Assert.fail("A descriptor from an Unproxiable service must not have isProxiable return true");
        }
        catch (MultiException me) {
            Assert.assertTrue(me.getMessage().contains("The descriptor is in an Unproxiable scope but has " +
                " isProxiable set to true"));
            
        }
        
    }
    
    @Test
    public void testBadProxiableScope() {
        DescriptorImpl di = BuilderHelper.link(ServiceInBadScope.class.getName()).
            in(BadScope.class.getName()).
            build();
        
        try {
            locator.reifyDescriptor(di);
            Assert.fail("BadScope is both proxiable and unproxiable, reify should fail");
        }
        catch (MultiException me) {
            Assert.assertTrue(me.getMessage().contains(" is marked both @Proxiable and @Unproxiable"));
        }
        
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testInvalidAutomaticActiveDescriptor() {
        DynamicConfigurationService dcs = locator.getService(DynamicConfigurationService.class);
        DynamicConfiguration dc = dcs.createDynamicConfiguration();
        
        dc.addActiveDescriptor(InvalidlyAnnotatedServices.class);
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testInvalidActiveDescriptor() {
        DynamicConfigurationService dcs = locator.getService(DynamicConfigurationService.class);
        DynamicConfiguration dc = dcs.createDynamicConfiguration();
        
        AbstractActiveDescriptor<?> ad = BuilderHelper.activeLink(InvalidlyAnnotatedServices.class).
                in(PerLookup.class).
                proxy().
                build();
               
        dc.addActiveDescriptor(ad);
    }
    
    /**
     * The UnavailableScopeService is proxied but there is no
     * context for it.  Ensure that if a method is called the
     * proper exception is thrown (IllegalStateException)
     */
    @Test(expected=IllegalStateException.class)
    public void testProxiedServiceWithUnavailableContext() {
        UnavailableScopeService uss = locator.getService(UnavailableScopeService.class);
        Assert.assertNotNull(uss);
        Assert.assertTrue(uss instanceof ProxyCtl);

        // Must fail with an IllegalStateException
        uss.callMe();
    }
}
