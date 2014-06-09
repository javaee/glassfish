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

import java.util.List;

import javax.inject.Singleton;

import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.AnnotationLiteral;
import org.glassfish.hk2.api.Descriptor;
import org.glassfish.hk2.api.DynamicConfiguration;
import org.glassfish.hk2.api.DynamicConfigurationService;
import org.glassfish.hk2.api.ErrorService;
import org.glassfish.hk2.api.MultiException;
import org.glassfish.hk2.api.ServiceHandle;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.api.ServiceLocatorFactory;
import org.glassfish.hk2.tests.locator.utilities.LocatorHelper;
import org.glassfish.hk2.utilities.AbstractActiveDescriptor;
import org.glassfish.hk2.utilities.BuilderHelper;
import org.glassfish.hk2.utilities.DescriptorImpl;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import org.junit.Assert;
import org.junit.Test;

/**
 * This tests {@link ServiceLocatorUtilities} methods that are not tested
 * in other suites
 *
 * @author jwells
 *
 */
public class ServiceLocatorUtilitiesTest {
    private final static String TEST_NAME = "ServiceLocatorUtilitiesTest";
    /* package */ final static String ALICE_NAME = "Alice";
    /* package */ final static String MALLORY_NAME = "Mallory";
    /* package */ final static String BLUE = "blue";
    /* package */ final static String RED = "red";

    private SimpleService addSimpleService(ServiceLocator locator) {
        SimpleService ss1 = locator.getService(SimpleService.class);
        if (ss1 == null) {
            SimpleService ss = new SimpleService();

            ActiveDescriptor<SimpleService> active = BuilderHelper.createConstantDescriptor(ss);

            ServiceLocatorUtilities.addOneDescriptor(locator, active);

            ss1 = locator.getService(SimpleService.class);
        }

        return ss1;
    }

    private static ServiceLocator uniqueCreate() {
        return ServiceLocatorFactory.getInstance().create(null);
    }

    /**
     * Tests using addSimpleService
     */
    @Test
    public void testAddActiveDescriptor() {
        ServiceLocator locator = uniqueCreate();

        SimpleService ss = addSimpleService(locator);

        SimpleService ss1 = locator.getService(SimpleService.class);
        Assert.assertNotNull(ss1);

        Assert.assertSame(ss, ss1);
    }

    /**
     * Tests addOneDescriptor
     */
    @Test
    public void testAddDescriptor() {
        ServiceLocator locator = uniqueCreate();

        Descriptor descriptor = BuilderHelper.createDescriptorFromClass(SimpleService1.class);

        ServiceLocatorUtilities.addOneDescriptor(locator, descriptor);

        SimpleService1 ss1 = locator.getService(SimpleService1.class);
        Assert.assertNotNull(ss1);
    }

    /**
     * Tests adding non-reified active descriptor
     */
    @Test
    public void testAddNonReifiedActiveDescriptor() {
        ServiceLocator locator = uniqueCreate();

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
        ServiceLocator locator = uniqueCreate();

        addSimpleService(locator);

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
        ServiceLocator locator = uniqueCreate();

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
        ServiceLocator locator = uniqueCreate();

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
        ServiceLocator locator = uniqueCreate();

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
        ServiceLocator locator = uniqueCreate();

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

    /**
     * Tests the findOneDescriptor method
     */
    @Test
    public void testFindOneDescriptor() {
        ServiceLocator locator = uniqueCreate();

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

    /**
     * Tests the ServiceLocatorUtilities getService method with a string
     */
    @Test
    public void testGetServiceWithString() {
        ServiceLocator locator = uniqueCreate();

        ServiceLocator sl = ServiceLocatorUtilities.getService(locator, ServiceLocator.class.getName());
        Assert.assertNotNull(sl);
        Assert.assertSame(sl, locator);

        Assert.assertNull(ServiceLocatorUtilities.getService(locator, "not.really.There"));
    }

    /**
     * Tests the ServiceLocatorUtilities getService method with a descriptor
     */
    @Test
    public void testGetServiceWithDescriptor() {
        ServiceLocator locator = uniqueCreate();

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

    /**
     * Tests that a DynamicConfiguration is properly created (and useable)
     */
    @Test
    public void testCreateDynamicConfiguration() {
        ServiceLocator locator = uniqueCreate();

        DynamicConfiguration dc = ServiceLocatorUtilities.createDynamicConfiguration(locator);
        Assert.assertNotNull(dc);

        Assert.assertNull(locator.getService(SimpleService7.class));

        ActiveDescriptor<?> added = dc.addActiveDescriptor(SimpleService7.class);
        Assert.assertNotNull(added);

        dc.commit();

        Assert.assertNotNull(locator.getService(SimpleService7.class));
    }

    /**
     * Tests that a DynamicConfiguration will throw IllegalArgumentException for null locator
     */
    @Test(expected=IllegalArgumentException.class)
    public void testCreateDynamicConfigurationWithNullLocator() {
        ServiceLocatorUtilities.createDynamicConfiguration(null);
    }

    /**
     * Tests that a locator without a DynamicConfigurationService will throw
     * IllegalStateException from createDynamicConfiguration
     */
    @Test(expected=IllegalStateException.class)
    public void testCreateDynamicConfigurationOnUnwriteableLocator() {
        ServiceLocator unwriteableLocator = LocatorHelper.create(TEST_NAME + "_unwriteable", null);

        // This MAKES this locator unwriteable
        ServiceLocatorUtilities.removeFilter(unwriteableLocator,
                BuilderHelper.createContractFilter(DynamicConfigurationService.class.getName()));

        ServiceLocatorUtilities.createDynamicConfiguration(unwriteableLocator);
    }

    /**
     * Since SimpleService8 is in Singleton context, we can tell if it is coming from
     * the context if it is the same on two lookups
     */
    @Test
    public void testFindOrCreateWithServiceInLocator() {
        ServiceLocator locator = uniqueCreate();

        ServiceLocatorUtilities.addOneDescriptor(locator, BuilderHelper.link(SimpleService8.class).
                in(Singleton.class.getName()).
                build());

        SimpleService8 one = ServiceLocatorUtilities.findOrCreateService(locator, SimpleService8.class);
        Assert.assertNotNull(one);

        SimpleService8 two = ServiceLocatorUtilities.findOrCreateService(locator, SimpleService8.class);
        Assert.assertNotNull(two);

        Assert.assertEquals(one, two);

    }

    /**
     * Even though SimpleService9 is marked with Singleton, it is NOT in the
     * locator, and hence it will be recreated every time in the "create" phase
     */
    @Test
    public void testFindOrCreateWithServiceNotInLocator() {
        ServiceLocator locator = uniqueCreate();

        SimpleService9 one = ServiceLocatorUtilities.findOrCreateService(locator, SimpleService9.class);
        Assert.assertNotNull(one);

        SimpleService9 two = ServiceLocatorUtilities.findOrCreateService(locator, SimpleService9.class);
        Assert.assertNotNull(two);

        Assert.assertNotSame(one, two);
    }

    /**
     * Bad locator test
     */
    @Test(expected=IllegalArgumentException.class)
    public void testBadLocatorToFindOrCreate() {
        ServiceLocatorUtilities.findOrCreateService(null, SimpleService9.class);
    }

    /**
     * Bad service test
     */
    @Test(expected=IllegalArgumentException.class)
    public void testBadServiceToFindOrCreate() {
        ServiceLocator locator = uniqueCreate();

        ServiceLocatorUtilities.findOrCreateService(locator, null);
    }

    /**
     * A non-standard non-reified ActiveDescriptor for use in testing
     * 
     * @author jwells
     *
     * @param <T> The type of this class
     */
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
                    delegate.getDescriptorVisibility(),
                    delegate.getRanking(),
                    delegate.isProxiable(),
                    delegate.isProxyForSameScope(),
                    delegate.getClassAnalysisName(),
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

    private final static String FIELD1 = "Field1";
    private final static String FIELD2 = "Field2";
    private final static String VALUE1 = "Value1";
    private final static String VALUE2 = "Value2";
    private final static String FIELD3 = "Field3";

    /**
     * Tests getting one metadata field from a service handle
     */
    @Test
    public void testGetOneMetadataServiceHandle() {
        ServiceLocator locator = uniqueCreate();

        Descriptor d = BuilderHelper.link(SimpleService10.class.getName()).
            has(FIELD1, VALUE1).
            has(FIELD2, VALUE2).
            build();

        ServiceLocatorUtilities.addOneDescriptor(locator, d);

        ServiceHandle<SimpleService10> handle = locator.getServiceHandle(SimpleService10.class);

        Assert.assertEquals(VALUE1, ServiceLocatorUtilities.getOneMetadataField(handle, FIELD1));
        Assert.assertEquals(VALUE2, ServiceLocatorUtilities.getOneMetadataField(handle, FIELD2));
        Assert.assertNull(ServiceLocatorUtilities.getOneMetadataField(handle, FIELD3));
    }

    /**
     * Tests getting one metadata field from a descriptor
     */
    @Test
    public void testGetOneMetadataDescriptor() {
        DescriptorImpl d = new DescriptorImpl();

        d.addMetadata(FIELD1, VALUE1);
        d.addMetadata(FIELD2, VALUE2);
        d.addMetadata(FIELD2, VALUE1);

        Assert.assertEquals(VALUE1, ServiceLocatorUtilities.getOneMetadataField(d, FIELD1));
        Assert.assertEquals(VALUE2, ServiceLocatorUtilities.getOneMetadataField(d, FIELD2));
        Assert.assertNull(ServiceLocatorUtilities.getOneMetadataField(d, FIELD3));

    }

    /**
     * Tests enableLookupExceptions
     */
    @Test
    public void testErrorRethrower() {
        ServiceLocator locator = uniqueCreate();

        DescriptorImpl di = BuilderHelper.link(FailService.class.getName()).andLoadWith(new HK2LoaderFail()).build();

        ServiceLocatorUtilities.addOneDescriptor(locator, di);

        // The lookup prior to enabling the rethrow should return null
        Assert.assertNull(locator.getService(FailService.class));

        Assert.assertEquals(0, locator.getAllServices(ErrorService.class).size());

        ServiceLocatorUtilities.enableLookupExceptions(locator);

        Assert.assertEquals(1, locator.getAllServices(ErrorService.class).size());

        try {
            locator.getService(FailService.class);
            Assert.fail("Should have rethrown reification failure");
        }
        catch (MultiException me) {
            // expected
        }

        // Make sure second one does not add another impl
        ServiceLocatorUtilities.enableLookupExceptions(locator);

        Assert.assertEquals(1, locator.getAllServices(ErrorService.class).size());

        try {
            locator.getService(FailService.class);
            Assert.fail("Should have rethrown reification failure (2)");
        }
        catch (MultiException me) {
            // expected
        }

    }
    
    /**
     * AlphabetService.class has a complex set of interfaces.
     * This test ensures addClasses gets all of them, and
     * misses the ones it should not see.
     * In parcticular D, F and B are contracts, A, E and C are not
     */
    @Test
    public void testComplexContractHeirarchyAdds() {
        ServiceLocator locator = uniqueCreate();
        
        ServiceLocatorUtilities.addClasses(locator, AlphabetService.class);
        
        AlphabetService alphabet = locator.getService(AlphabetService.class);
        
        InterfaceA a = locator.getService(InterfaceA.class);
        InterfaceB b = locator.getService(InterfaceB.class);
        InterfaceC c = locator.getService(InterfaceC.class);
        InterfaceD d = locator.getService(InterfaceD.class);
        InterfaceE e = locator.getService(InterfaceE.class);
        InterfaceF f = locator.getService(InterfaceF.class);
        
        Assert.assertNull(a);
        Assert.assertNull(c);
        Assert.assertNull(e);
        
        Assert.assertEquals(alphabet, b);
        Assert.assertEquals(alphabet, d);
        Assert.assertEquals(alphabet, f);
        
    }
    
    /**
     * Tests that a service can be named with the @Service annotation
     */
    @Test
    public void testServiceNamedWithService() {
        ServiceLocator locator = uniqueCreate();
        
        ServiceLocatorUtilities.addClasses(locator, ServiceNamedService.class);
        
        Assert.assertNotNull(locator.getService(ServiceNamedService.class, ALICE_NAME));
    }
    
    /**
     * Tests that a service with conflicting @Service and @Named names is rejected
     */
    @Test(expected=java.lang.IllegalArgumentException.class)
    public void testServiceWithConflictingNames() {
        ServiceLocator locator = uniqueCreate();
        
        ServiceLocatorUtilities.addClasses(locator, ServiceWithConflictingNames.class);
    }
    
    /**
     * Tests that a service with matching @Service and @Named names is ok
     */
    @Test
    public void testServiceWithMatchingNames() {
        ServiceLocator locator = uniqueCreate();
        
        ServiceLocatorUtilities.addClasses(locator, ServiceWithMatchingNames.class);
        
        Assert.assertNotNull(locator.getService(ServiceWithMatchingNames.class, ALICE_NAME));
    }
    
    /**
     * Tests that a service with matching @Service and default @Named is ok
     */
    @Test
    public void testServiceWithMatchingDefaultNames() {
        ServiceLocator locator = uniqueCreate();
        
        ServiceLocatorUtilities.addClasses(locator, ServiceWithMatchingDefaultName.class);
        
        Assert.assertNotNull(locator.getService(ServiceWithMatchingDefaultName.class, "ServiceWithMatchingDefaultName"));
    }
    
    /**
     * Tests that a service with non matching default @Named and @Service name
     */
    @Test(expected=java.lang.IllegalArgumentException.class)
    public void testServiceWithConflictingDefaultName() {
        ServiceLocator locator = uniqueCreate();
        
        ServiceLocatorUtilities.addClasses(locator, ServiceWithConflictingDefaultName.class);
    }
    
    /**
     * Makes sure we can dump all services
     */
    @Test
    public void testDumpAllServices() {
        ServiceLocator locator = uniqueCreate();
        
        ServiceLocatorUtilities.dumpAllDescriptors(locator);
        ServiceLocatorUtilities.dumpAllDescriptors(locator, System.out);
    }
    
    /**
     * Uses addClasses with factories
     */
    @Test
    public void testAddClassesWithFactories() {
        ServiceLocator locator = uniqueCreate();
        
        List<ActiveDescriptor<?>> added = ServiceLocatorUtilities.addClasses(locator, RedFactory.class, BlueFactory.class);
        Assert.assertEquals(4, added.size());
        
        String blue = locator.getService(String.class, new BlueImpl());
        Assert.assertEquals(BLUE, blue);
        
        String red = locator.getService(String.class, new RedImpl());
        Assert.assertEquals(RED, red);
    }
    
    @Test
    public void addClassWithMetadata() {
        ServiceLocator locator = uniqueCreate();
        
        List<ActiveDescriptor<?>> added = ServiceLocatorUtilities.addClasses(locator, ServiceWithMetadata.class);
        Assert.assertEquals(1, added.size());
        
        ActiveDescriptor<?> descriptor = added.get(0);
        
        Assert.assertEquals("value", ServiceLocatorUtilities.getOneMetadataField(descriptor, "key"));
        
        List<String> multiValues = descriptor.getMetadata().get("multiKey");
        Assert.assertEquals(2, multiValues.size());
        
        Assert.assertEquals("value1", multiValues.get(0));
        Assert.assertEquals("value2", multiValues.get(1));
    }
    
    private static class BlueImpl extends AnnotationLiteral<Blue> implements Blue {

        /**
         * 
         */
        private static final long serialVersionUID = -7725354246185428959L;

        
    }
    
    private static class RedImpl extends AnnotationLiteral<Red> implements Red {

        /**
         * 
         */
        private static final long serialVersionUID = 3107298539617537764L;


        
    }
}




