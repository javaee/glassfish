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

package org.glassfish.hk2.tests.locator.justintime;

import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.MultiException;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.api.ServiceLocatorFactory;
import org.glassfish.hk2.tests.locator.utilities.LocatorHelper;
import org.glassfish.hk2.utilities.BuilderHelper;
import org.glassfish.hk2.utilities.InjecteeImpl;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author jwells
 *
 */
public class JustInTimeTest {
    private final static String TEST_NAME = "JustInTimeTest";
    private final static ServiceLocator locator = LocatorHelper.create(TEST_NAME, new JustInTimeModule());

    /**
     * Tests that if I forgot to add a service, I can add it just in time
     */
    @Test
    public void testJustInTimeResolution() {
        InjectedThriceService threeTimes = locator.getService(InjectedThriceService.class);
        Assert.assertNotNull(threeTimes);
        Assert.assertTrue(threeTimes.isValid());

        // Make sure the resolver was only called once
        SimpleServiceJITResolver jitResolver = locator.getService(SimpleServiceJITResolver.class);
        Assert.assertNotNull(jitResolver);

        Assert.assertEquals("Expected 1 JIT resolution, but got " + jitResolver.getNumTimesCalled(), 1, jitResolver.getNumTimesCalled());

    }

    /**
     * In this test the resolver itself has resolution problems.  We make sure this does not
     * mess up the other resolver, and that once the resolution problem of the resolver has
     * been fixed that it can do its job properly.
     */
    @Test
    public void testDoubleTroubleResolution() {
        try {
            locator.getService(DoubleTroubleService.class);
            Assert.fail("DoubleTrouble depends on Service2 which should not be available yet");
        } catch (MultiException me) {
            // Good
        }

        // SimpleService3 will fix the DoubleTrouble JIT resolver
        ServiceLocatorUtilities.addOneDescriptor(locator, BuilderHelper.link(SimpleService3.class).build());

        Assert.assertNotNull(locator.getService(DoubleTroubleService.class));
    }

    /**
     * This test ensures that a direct lookup (with {@link ServiceLocator#getInjecteeDescriptor(org.glassfish.hk2.api.Injectee)})
     * works properly
     */
    @Test
    public void testJITInLookup() {
        InjecteeImpl injectee = new InjecteeImpl(SimpleService.class);

        ActiveDescriptor<?> ad = locator.getInjecteeDescriptor(injectee);
        Assert.assertNotNull(ad);
    }
    
    /**
     * Tests the get method of Provider
     */
    @Test
    public void testProviderGet() {
        ServiceLocator locator = getProviderLocator();
        
        IterableProviderService ips = locator.getService(IterableProviderService.class);
        Assert.assertNotNull(ips);
        
        Assert.assertNotNull(locator.getService(SimpleService4.class));
        
        ips.checkGet();
    }
    
    /**
     * Tests the getHandle method of Provider
     */
    @Test
    public void testProviderGetHandle() {
        ServiceLocator locator = getProviderLocator();
        
        IterableProviderService ips = locator.getService(IterableProviderService.class);
        Assert.assertNotNull(ips);
        
        Assert.assertNotNull(locator.getService(SimpleService4.class));
        
        ips.checkGetHandle();
    }
    
    /**
     * Tests the getHandle method of Provider
     */
    @Test
    public void testProviderIterator() {
        ServiceLocator locator = getProviderLocator();
        
        IterableProviderService ips = locator.getService(IterableProviderService.class);
        Assert.assertNotNull(ips);
        
        Assert.assertNotNull(locator.getService(SimpleService4.class));
        
        ips.checkIterator();
    }
    
    /**
     * Tests the getSize method of Provider
     */
    @Test
    public void testProviderSize() {
        ServiceLocator locator = getProviderLocator();
        
        IterableProviderService ips = locator.getService(IterableProviderService.class);
        Assert.assertNotNull(ips);
        
        Assert.assertNotNull(locator.getService(SimpleService4.class));
        
        ips.checkSize();
    }

    @Test
    public void testMaliciousResolver() {
        ServiceLocator locator = getProviderLocator();

        Assert.assertNull(locator.getService(UnimplementedContract.class));
    }
    
    /**
     * Tests the getSize method of Provider
     */
    @Test
    public void testProviderHandleIterator() {
        ServiceLocator locator = getProviderLocator();
        
        IterableProviderService ips = locator.getService(IterableProviderService.class);
        Assert.assertNotNull(ips);
        
        Assert.assertNotNull(locator.getService(SimpleService4.class));
        
        ips.checkHandleIterator();
    }
    
    private static ServiceLocator getProviderLocator() {
        ServiceLocator locator = ServiceLocatorFactory.getInstance().create(null);
        
        ServiceLocatorUtilities.addClasses(locator,
                IterableProviderService.class,
                EvilJITResolver.class,
                SimpleService4JITResolver.class);
        
        return locator;
    }

}
