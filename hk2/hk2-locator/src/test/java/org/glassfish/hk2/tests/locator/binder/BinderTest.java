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
package org.glassfish.hk2.tests.locator.binder;

import javax.inject.Singleton;

import org.glassfish.hk2.api.DynamicConfiguration;
import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.api.ProxyCtl;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.api.ServiceLocatorFactory;
import org.glassfish.hk2.tests.locator.utilities.LocatorHelper;
import org.glassfish.hk2.utilities.Binder;
import org.glassfish.hk2.utilities.BuilderHelper;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.hk2.utilities.reflection.ParameterizedTypeImpl;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author jwells
 *
 */
public class BinderTest {
    private final static String TEST_NAME = "BinderTest";
    private final static ServiceLocator locator = LocatorHelper.create(TEST_NAME, null);
    
    /**
     * Tests adding in bindings
     */
    @Test
    public void testAddInAFewBindings() {
        Assert.assertNull(locator.getBestDescriptor(BuilderHelper.createContractFilter(Nazgul.class.getName())));
        Assert.assertNull(locator.getBestDescriptor(BuilderHelper.createContractFilter(Elves.class.getName())));
        
        ServiceLocatorUtilities.bind(locator, new NazgulBinder(), new ElvesBinder());
        
        Assert.assertNotNull(locator.getBestDescriptor(BuilderHelper.createContractFilter(Nazgul.class.getName())));
        Assert.assertNotNull(locator.getBestDescriptor(BuilderHelper.createContractFilter(Elves.class.getName())));
    }
    
    /**
     * Tests creating a new locator with bindings
     */
    @Test
    public void testCreateNewLocatorAndAddBindings() {
        ServiceLocator locator2 = ServiceLocatorUtilities.bind(TEST_NAME + "2", new NazgulBinder(), new ElvesBinder());
        
        Assert.assertNotNull(locator2.getBestDescriptor(BuilderHelper.createContractFilter(Nazgul.class.getName())));
        Assert.assertNotNull(locator2.getBestDescriptor(BuilderHelper.createContractFilter(Elves.class.getName())));
    }
    
    /**
     * Tests a constant factory
     */
    @Test
    public void testFactoryBindingWithConstantFactory() {
        ServiceLocator locator = ServiceLocatorFactory.getInstance().create(null);
        
        final MountDoom myFactory = new MountDoom();
        
        ServiceLocatorUtilities.bind(locator, new AbstractBinder() {
            @Override
            protected void configure() {
                bindFactory(myFactory).to(RingOfPower.class).proxy(true).in(Singleton.class);
            }
            
        });
        
        Factory<RingOfPower> isaConstant = locator.getService(new ParameterizedTypeImpl(Factory.class, RingOfPower.class));
        Assert.assertTrue(isaConstant == myFactory);  // More than just equals
        
        RingOfPower oneRing = locator.getService(RingOfPower.class);
        Assert.assertNotNull(oneRing);

        // Make sure it is proxied
        Assert.assertTrue(oneRing instanceof ProxyCtl);
        ProxyCtl pc = (ProxyCtl) oneRing;
        
        RingOfPower secondRing = (RingOfPower) pc.__make();  // Makes sure factory gets called
        Assert.assertNotNull(secondRing);
    }
    
    /**
     * Tests a class factory
     */
    @Test
    public void testFactoryBindingWithClassFactory() {
        ServiceLocator locator = ServiceLocatorFactory.getInstance().create(null);
        
        ServiceLocatorUtilities.bind(locator, new AbstractBinder() {
            @Override
            protected void configure() {
                bindFactory(MountDoom.class).to(RingOfPower.class).proxy(true).in(Singleton.class);
            }
            
        });
        
        Factory<RingOfPower> myFactory = locator.getService(new ParameterizedTypeImpl(Factory.class, RingOfPower.class));
        Assert.assertNotNull(myFactory);
        
        RingOfPower oneRing = locator.getService(RingOfPower.class);
        Assert.assertNotNull(oneRing);

        // Make sure it is proxied
        Assert.assertTrue(oneRing instanceof ProxyCtl);
        ProxyCtl pc = (ProxyCtl) oneRing;
        
        RingOfPower secondRing = (RingOfPower) pc.__make();  // Makes sure factory gets called
        Assert.assertNotNull(secondRing);
    }
    
    private static class NazgulBinder implements Binder {

        /* (non-Javadoc)
         * @see org.glassfish.hk2.utilities.Binder#bind(org.glassfish.hk2.api.DynamicConfiguration)
         */
        @Override
        public void bind(DynamicConfiguration config) {
            config.bind(BuilderHelper.link(Nazgul.class.getName()).build());
        }
        
    }
    
    private static class ElvesBinder implements Binder {

        /* (non-Javadoc)
         * @see org.glassfish.hk2.utilities.Binder#bind(org.glassfish.hk2.api.DynamicConfiguration)
         */
        @Override
        public void bind(DynamicConfiguration config) {
            config.bind(BuilderHelper.link(Elves.class.getName()).build());
        }
        
    }

}
