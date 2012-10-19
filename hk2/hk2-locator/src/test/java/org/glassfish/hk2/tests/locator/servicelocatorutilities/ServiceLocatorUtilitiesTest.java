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
package org.glassfish.hk2.tests.locator.servicelocatorutilities;

import javax.inject.Singleton;

import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.Descriptor;
import org.glassfish.hk2.api.ServiceHandle;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.tests.locator.utilities.LocatorHelper;
import org.glassfish.hk2.utilities.AbstractActiveDescriptor;
import org.glassfish.hk2.utilities.BuilderHelper;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import org.junit.Assert;
import org.junit.Test;

/**
 * This tests {@link ServiceLocatorUtility} methods that are not tested
 * in other suites
 * 
 * @author jwells
 *
 */
public class ServiceLocatorUtilitiesTest {
    private final static String TEST_NAME = "ServiceLocatorUtilitiesTest";
    private final static ServiceLocator locator = LocatorHelper.create(TEST_NAME, null);

    @Test
    public void testAddActiveDescriptor() {
        SimpleService ss = new SimpleService();
        
        ActiveDescriptor<SimpleService> active = BuilderHelper.createConstantDescriptor(ss);
        
        ServiceLocatorUtilities.addOneDescriptor(locator, active);
        
        SimpleService ss1 = locator.getService(SimpleService.class);
        Assert.assertNotNull(ss1);
        
        Assert.assertSame(ss, ss1);
    }
    
    @Test
    public void testAddDescriptor() {
        Descriptor descriptor = BuilderHelper.createDescriptorFromClass(SimpleService1.class);
        
        ServiceLocatorUtilities.addOneDescriptor(locator, descriptor);
        
        SimpleService1 ss1 = locator.getService(SimpleService1.class);
        Assert.assertNotNull(ss1);
    }
    
    @Test
    public void testAddNonReifiedActiveDescriptor() {
        SimpleService2 ss = new SimpleService2();
        
        AbstractActiveDescriptor<SimpleService2> active = BuilderHelper.createConstantDescriptor(ss);
        
        NonReifiedActiveDescriptor<SimpleService2> nonReified = new NonReifiedActiveDescriptor<SimpleService2>(active);
        
        ServiceLocatorUtilities.addOneDescriptor(locator, nonReified);
        
        SimpleService2 ss1 = locator.getService(SimpleService2.class);
        Assert.assertNotNull(ss1);
        
        // This should NOT be the same because the non-reified goes in as
        // a normal descriptor, which means the system will create the SimpleService2
        // rather than using the thing from the active descriptor
        Assert.assertNotSame(ss, ss1);
    }
    
    /**
     * Tests the createAndInitialize method
     */
    @Test
    public void testCreateAndInitialize() {
        ServiceWithPostConstruct swpc = locator.createAndInitialize(
                ServiceWithPostConstruct.class);
        Assert.assertNotNull(swpc);
        
        swpc.check();
    }
    
    /**
     * Tests adding a constant with {@link ServiceLocatorUtilities#addOneConstant(ServiceLocator, Object)}
     */
    @Test
    public void testAddAndRemoveConstant() {
        SimpleService3 ss3 = new SimpleService3();
        
        ActiveDescriptor<?> ss3Descriptor = ServiceLocatorUtilities.addOneConstant(locator, ss3);
        
        Assert.assertEquals(ss3.getClass().getName(), ss3Descriptor.getImplementation());
        Assert.assertEquals(ss3.getClass(), ss3Descriptor.getImplementationClass());
        
        // Should be findable with both contracts
        SimpleService3 ss3_via_impl = locator.getService(SimpleService3.class);
        Assert.assertEquals(ss3_via_impl, ss3);
        
        SimpleContract ss3_via_contract = locator.getService(SimpleContract.class);
        Assert.assertEquals(ss3_via_contract, ss3);
        
        ServiceLocatorUtilities.removeOneDescriptor(locator, ss3Descriptor);
        
        // Should now be gone
        ss3_via_impl = locator.getService(SimpleService3.class);
        Assert.assertNull(ss3_via_impl);
        
        ss3_via_contract = locator.getService(SimpleContract.class);
        Assert.assertNull(ss3_via_contract);
    }
    
    /**
     * Tests removing a descriptor with a non-specific descriptor
     */
    @Test
    public void testRemoveNonSpecificDescriptor() {
        Descriptor d = BuilderHelper.link(SimpleService4.class.getName()).
                to(SimpleContract.class).
                in(Singleton.class.getName()).build();
        
        ServiceLocatorUtilities.addOneDescriptor(locator, d);
        
        SimpleService4 ss4 = locator.getService(SimpleService4.class);
        Assert.assertNotNull(ss4);
        
        // This one does NOT have service-id and locator-id set
        ServiceLocatorUtilities.removeOneDescriptor(locator, d);
        
        ss4 = locator.getService(SimpleService4.class);
        Assert.assertNull(ss4);
    }
    
    /**
     * Tests adding constants with constrained contracts and multiple names
     */
    @Test
    public void testAddContractWithSpecificContracts() {
        SimpleService3 ss3 = new SimpleService3();
        
        ActiveDescriptor<?> ad = ServiceLocatorUtilities.addOneConstant(locator, ss3, null, SimpleContract.class);
        
        // Should NOT be able to look it up with SimpleService3
        Assert.assertNull(locator.getService(SimpleService3.class));
        
        SimpleContract ss3_lookup = locator.getService(SimpleContract.class);
        Assert.assertSame(ss3, ss3_lookup);
        
        ServiceLocatorUtilities.removeOneDescriptor(locator, ad);
    }
    
    private final static String ALICE = "alice";
    private final static String BOB = "bob";
    
    /**
     * Tests adding constants with constrained contracts and multiple names
     */
    @Test
    public void testAddContractWithDifferentNames() {
        SimpleService3 ss3_alice = new SimpleService3();
        SimpleService3 ss3_bob = new SimpleService3();
        
        ServiceLocatorUtilities.addOneConstant(
                locator, ss3_alice, ALICE, SimpleContract.class);
        ServiceLocatorUtilities.addOneConstant(
                locator, ss3_bob, BOB, SimpleContract.class);
        
        // Should NOT be able to look it up with SimpleService3
        Assert.assertNull(locator.getService(SimpleService3.class));
        
        SimpleContract ss3_lookup_bob = locator.getService(SimpleContract.class, BOB);
        Assert.assertSame(ss3_bob, ss3_lookup_bob);
        
        SimpleContract ss3_lookup_alice = locator.getService(SimpleContract.class, ALICE);
        Assert.assertSame(ss3_alice, ss3_lookup_alice);
        
        ServiceLocatorUtilities.removeFilter(locator,
                BuilderHelper.createNameAndContractFilter(SimpleContract.class.getName(), BOB));
        
        Assert.assertNull(locator.getService(SimpleContract.class, BOB));
        Assert.assertNotNull(locator.getService(SimpleContract.class, ALICE));
        
        ServiceLocatorUtilities.removeFilter(locator,
                BuilderHelper.createNameAndContractFilter(SimpleContract.class.getName(), ALICE));
        
        Assert.assertNull(locator.getService(SimpleContract.class, ALICE));
    }
    
    @Test
    public void testFindOneDescriptor() {
        Descriptor addMe = BuilderHelper.link(SimpleService3.class).
                to(SimpleContract.class).build();
        
        ActiveDescriptor<?> ss3Desc1 = ServiceLocatorUtilities.addOneDescriptor(locator, addMe);
        ActiveDescriptor<?> ss3Desc2 = ServiceLocatorUtilities.addOneDescriptor(locator, addMe);
        
        // Look up two ways, with the specific and the non-specific descriptor
        ActiveDescriptor<?> found1 = ServiceLocatorUtilities.findOneDescriptor(locator, addMe);
        Assert.assertSame(ss3Desc1, found1);
        
        ActiveDescriptor<?> found2 = ServiceLocatorUtilities.findOneDescriptor(locator, ss3Desc1);
        Assert.assertSame(ss3Desc1, found2);
        
        ActiveDescriptor<?> found3 = ServiceLocatorUtilities.findOneDescriptor(locator, ss3Desc2);
        Assert.assertSame(ss3Desc2, found3);
        
        // Now remove first one, ensure I can still get with the not-found specific descriptor
        ServiceLocatorUtilities.removeOneDescriptor(locator, ss3Desc1);
        
        ActiveDescriptor<?> found4 = ServiceLocatorUtilities.findOneDescriptor(locator, ss3Desc1);
        Assert.assertSame(ss3Desc2, found4);
        
        ServiceLocatorUtilities.removeOneDescriptor(locator, ss3Desc2);
        
        Assert.assertNull(ServiceLocatorUtilities.findOneDescriptor(locator, addMe));
        Assert.assertNull(ServiceLocatorUtilities.findOneDescriptor(locator, ss3Desc1));
        Assert.assertNull(ServiceLocatorUtilities.findOneDescriptor(locator, ss3Desc2));
        
    }
    
    @Test
    public void testGetServiceWithString() {
        ServiceLocator sl = ServiceLocatorUtilities.getService(locator, ServiceLocator.class.getName());
        Assert.assertNotNull(sl);
        Assert.assertSame(sl, locator);
        
        Assert.assertNull(ServiceLocatorUtilities.getService(locator, "not.really.There"));
    }
    
    @Test
    public void testGetServiceWithDescriptor() {
        Descriptor desc5 = BuilderHelper.link(SimpleService5.class.getName()).build();
        Descriptor desc6 = BuilderHelper.link(SimpleService6.class.getName()).build();
        
        ActiveDescriptor<SimpleService5> activeDesc = ServiceLocatorUtilities.addOneDescriptor(locator, desc5);
        
        {
            // This way of getting it does NOT have the locator fields filled in, but should still work
            SimpleService5 s5 = ServiceLocatorUtilities.getService(locator, desc5);
            Assert.assertNotNull(s5);
        }
        
        {
            // This way of getting it DOES have the locator fields filled in
            SimpleService5 s5 = ServiceLocatorUtilities.getService(locator, activeDesc);
            Assert.assertNotNull(s5);
        }
        
        {
            // This service is NOT there
            SimpleService6 s6 = ServiceLocatorUtilities.getService(locator, desc6);
            Assert.assertNull(s6);
        }
    }
    
    public static class NonReifiedActiveDescriptor<T> extends AbstractActiveDescriptor<T> implements ActiveDescriptor<T> {
        /**
         * 
         */
        private static final long serialVersionUID = 8750311164952618038L;
        
        private final AbstractActiveDescriptor<T> delegate;
        
        private NonReifiedActiveDescriptor(AbstractActiveDescriptor<T> delegate) {
            super(delegate.getContractTypes(),
                    delegate.getScopeAnnotation(),
                    delegate.getName(),
                    delegate.getQualifierAnnotations(),
                    delegate.getDescriptorType(),
                    delegate.getRanking(),
                    delegate.isProxiable(),
                    delegate.getMetadata());
            
            this.delegate = delegate;
        }
        
        /**
         * This method is the point of this class.  Since this ActiveDescriptor is not reified the
         * method should treat it as a descriptor, not an active descriptor.  And hence the constant
         * should NOT be honored.
         */
        @Override
        public boolean isReified() {
            return false;
        }
        
        public String getImplementation() {
            return delegate.getImplementation();
        }

        /* (non-Javadoc)
         * @see org.glassfish.hk2.api.ActiveDescriptor#getImplementationClass()
         */
        @Override
        public Class<?> getImplementationClass() {
            return delegate.getImplementationClass();
        }

        /* (non-Javadoc)
         * @see org.glassfish.hk2.api.ActiveDescriptor#create(org.glassfish.hk2.api.ServiceHandle)
         */
        @Override
        public T create(ServiceHandle<?> root) {
            return delegate.create(root);
        }
        
    }
}
