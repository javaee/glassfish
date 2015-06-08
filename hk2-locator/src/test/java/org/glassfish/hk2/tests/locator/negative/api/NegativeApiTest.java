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

package org.glassfish.hk2.tests.locator.negative.api;

import java.lang.reflect.Type;
import java.util.Set;

import junit.framework.Assert;

import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.Descriptor;
import org.glassfish.hk2.api.DynamicConfiguration;
import org.glassfish.hk2.api.DynamicConfigurationService;
import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.api.FactoryDescriptors;
import org.glassfish.hk2.api.Filter;
import org.glassfish.hk2.api.Injectee;
import org.glassfish.hk2.api.MultiException;
import org.glassfish.hk2.api.ServiceHandle;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.api.ServiceLocatorFactory;
import org.glassfish.hk2.tests.locator.utilities.LocatorHelper;
import org.glassfish.hk2.utilities.AbstractActiveDescriptor;
import org.glassfish.hk2.utilities.BuilderHelper;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import org.junit.Test;

/**
 * Test various user input errors to the API, like nulls
 * and badly formed stuff
 * 
 * @author jwells
 *
 */
public class NegativeApiTest {
    private final static String TEST_NAME = "NegativeApiTest";
    private final static ServiceLocator locator = ServiceLocatorFactory.getInstance().create(TEST_NAME);
    private final static DynamicConfigurationService dcs = locator.getService(DynamicConfigurationService.class);
    
    /**
     * You cannot call getService with a null
     */
    @Test(expected=IllegalArgumentException.class)
    public void testNullGetService() {
        locator.getService(null);
    }
    
    /**
     * You cannot call getService with a name with a null
     */
    @Test(expected=IllegalArgumentException.class)
    public void testNullGetServiceWithName() {
        locator.getService(null, "");
    }
    
    /**
     * You cannot call getAllServices with a null
     */
    @Test(expected=IllegalArgumentException.class)
    public void testNullGetAllServices() {
        locator.getAllServices((Type) null);
    }
    
    /**
     * You cannot call getAllServices with a Filter with null
     */
    @Test(expected=IllegalArgumentException.class)
    public void testNullGetAllServicesWithFilter() {
        locator.getAllServices((Filter) null);
    }
    
    /**
     * You cannot call getServiceHandle with a null
     */
    @Test(expected=IllegalArgumentException.class)
    public void testNullGetServiceHandle() {
        locator.getServiceHandle((Type) null);
    }
    
    /**
     * You cannot call getServiceHandle with an ActiveDescriptor with a null
     */
    @Test(expected=IllegalArgumentException.class)
    public void testNullGetServiceHandleActive() {
        locator.getServiceHandle((ActiveDescriptor<?>) null);
    }
    
    /**
     * You cannot call getServiceHandle with a name with a null
     */
    @Test(expected=IllegalArgumentException.class)
    public void testNullGetServiceHandleWithName() {
        locator.getServiceHandle(null, "");
    }
    
    /**
     * You cannot call getAllServiceHandles with a null
     */
    @Test(expected=IllegalArgumentException.class)
    public void testNullGetAllSerivceHandles() {
        locator.getAllServiceHandles((Type) null);
    }
    
    /**
     * You cannot call getAllServiceHandles with a Filter with null
     */
    @Test(expected=IllegalArgumentException.class)
    public void testNullGetAllSerivceHandlesWithFilter() {
        locator.getAllServiceHandles((Filter) null);
    }
    
    /**
     * You cannot call getDescriptors with null
     */
    @Test(expected=IllegalArgumentException.class)
    public void testNullGetDescriptors() {
        locator.getDescriptors(null);
    }
    
    /**
     * You cannot call getDescriptors with null
     */
    @Test(expected=IllegalArgumentException.class)
    public void testNullGetBestDescriptor() {
        locator.getBestDescriptor(null);
    }
    
    /**
     * You cannot call getDescriptors with two arguments with null
     */
    @Test(expected=IllegalArgumentException.class)
    public void testNullReifyTwoArgDescriptor() {
        locator.reifyDescriptor(null, null);
    }
    
    /**
     * You cannot call getDescriptors with null
     */
    @Test(expected=IllegalArgumentException.class)
    public void testNullReifyDescriptor() {
        locator.reifyDescriptor(null);
    }
    
    /**
     * You cannot call reify a descriptor that forgot about its name
     */
    @Test
    public void testReifyDescriptorNoNameButThereShouldBe() {
        NullDescriptorImpl ndi = new NullDescriptorImpl();
        ndi.setImplementation(NamedService.class.getName());
        ndi.unNullContracts();
        ndi.unNullType(false);
        ndi.unNullMetadata();
        
        try {
            locator.reifyDescriptor(ndi);
            Assert.fail("Should have failed due to no name in descriptor, but name on the class");
        }
        catch (MultiException me) {
            Assert.assertTrue(me.getMessage(),
                    me.getMessage().contains("No name was in the descriptor, but this element("));
        }
    }
    
    /**
     * You cannot reify a descriptor with mismatched names
     */
    @Test
    public void testReifyDescriptorWrongName() {
        NullDescriptorImpl ndi = new NullDescriptorImpl();
        ndi.setImplementation(NamedService.class.getName());
        ndi.unNullContracts();
        ndi.unNullType(false);
        ndi.unNullMetadata();
        ndi.setName("NotTheName");
        
        try {
            locator.reifyDescriptor(ndi);
            Assert.fail("Should have failed due to no name in descriptor, but name on the class");
        }
        catch (MultiException me) {
            Assert.assertTrue(me.getMessage(),
                    me.getMessage().contains("The class had an @Named qualifier that was inconsistent."));
        }
    }
    
    /**
     * You cannot call getInjecteeDescriptor with null
     */
    @Test(expected=IllegalArgumentException.class)
    public void testNullGetInjecteeDescriptor() {
        locator.getInjecteeDescriptor(null);
    }
    
    /**
     * You cannot call getServiceHandle with ActiveDescriptor and Injectee null
     */
    @Test(expected=IllegalArgumentException.class)
    public void testNullGetServiceHandleTwoArgs() {
        locator.getServiceHandle((ActiveDescriptor<?>) null, (Injectee) null);
    }
    
    /**
     * You cannot call getServiceHandle with ActiveDescriptor and Injectee null
     */
    @Test(expected=IllegalArgumentException.class)
    public void testServiceWithRoot() {
        locator.getService((ActiveDescriptor<?>) null, (ServiceHandle<?>) null);
    }
    
    /**
     * You cannot call getServiceHandle with ActiveDescriptor and Injectee null
     */
    @Test(expected=IllegalArgumentException.class)
    public void testCreate() {
        locator.create(null);
    }
    
    /**
     * You cannot call getServiceHandle with ActiveDescriptor and Injectee null
     */
    @Test(expected=IllegalArgumentException.class)
    public void testInject() {
        locator.inject(null);
    }
    
    /**
     * You cannot call getServiceHandle with ActiveDescriptor and Injectee null
     */
    @Test(expected=IllegalArgumentException.class)
    public void testPostConstruct() {
        locator.postConstruct(null);
    }
    
    /**
     * You cannot call getServiceHandle with ActiveDescriptor and Injectee null
     */
    @Test(expected=IllegalArgumentException.class)
    public void testPreDestroy() {
        locator.preDestroy(null);
    }
    
    /**
     * You cannot call getServiceHandle with ActiveDescriptor and Injectee null
     */
    @Test
    public void testBadType() {
        try {
            locator.getAllServiceHandles(new Type() {});
            Assert.fail("service with bad type should fail");
        }
        catch (MultiException me) {
            Assert.assertTrue(me.getMessage(),
                    me.getMessage().contains("Type must be a class or parameterized type, it was "));
        }
    }
    
    /**
     * You cannot call bind with null descriptor
     */
    @Test(expected=IllegalArgumentException.class)
    public void testNullBind() {
        DynamicConfiguration config = dcs.createDynamicConfiguration();
        config.bind((Descriptor) null);
    }
    
    /**
     * You cannot call bind with descriptor with null impl
     */
    @Test(expected=IllegalArgumentException.class)
    public void testNullImplBind() {
        DynamicConfiguration config = dcs.createDynamicConfiguration();
        config.bind(new NullDescriptorImpl());
    }
    
    /**
     * You cannot call bind with descriptor with null contracts
     */
    @Test(expected=IllegalArgumentException.class)
    public void testNullContractBind() {
        DynamicConfiguration config = dcs.createDynamicConfiguration();
        NullDescriptorImpl ndi = new NullDescriptorImpl();
        ndi.setImplementation("");
        
        config.bind(ndi);
    }
    
    /**
     * You cannot call bind with descriptor with null descriptor type
     */
    @Test(expected=IllegalArgumentException.class)
    public void testNullDescriptorTypeBind() {
        DynamicConfiguration config = dcs.createDynamicConfiguration();
        NullDescriptorImpl ndi = new NullDescriptorImpl();
        ndi.setImplementation("");
        ndi.unNullContracts();
        
        config.bind(ndi);
    }
    
    /**
     * You cannot call bind with descriptor with null metadata type
     */
    @Test(expected=IllegalArgumentException.class)
    public void testNullMetadataBind() {
        DynamicConfiguration config = dcs.createDynamicConfiguration();
        NullDescriptorImpl ndi = new NullDescriptorImpl();
        ndi.setImplementation("");
        ndi.unNullContracts();
        ndi.unNullType(true);
        
        config.bind(ndi);
    }
    
    /**
     * You cannot call bind with descriptor with null qualifier
     */
    @Test(expected=IllegalArgumentException.class)
    public void testNullQualifierBind() {
        DynamicConfiguration config = dcs.createDynamicConfiguration();
        NullDescriptorImpl ndi = new NullDescriptorImpl();
        ndi.setImplementation("");
        ndi.unNullContracts();
        ndi.unNullType(true);
        ndi.unNullMetadata();
        
        config.bind(ndi);
    }
    
    /**
     * You cannot call bind with descriptor with null qualifier
     */
    @Test(expected=IllegalArgumentException.class)
    public void testNullFactoryBind() {
        DynamicConfiguration config = dcs.createDynamicConfiguration();
        config.bind((FactoryDescriptors) null);
    }
    
    private final static String A = "A";
    private final static String B = "B";
    
    /**
     * You cannot call bind mismatched factory descriptors
     */
    @Test
    public void testMismatchedFactoryBind() {
        DynamicConfiguration config = dcs.createDynamicConfiguration();
        
        FactoryDescriptorsImpl fdi = new FactoryDescriptorsImpl();
        fdi.setAsFactory(BuilderHelper.link(A).build());
        fdi.setAsService(BuilderHelper.link(B).build());
        
        try {
            config.bind(fdi);
        }
        catch (IllegalArgumentException iae) {
            Assert.assertTrue(iae.getMessage(),
                    iae.getMessage().contains("The implementation classes must match ("));
        }
    }
    
    /**
     * The factory descriptor is of type CLASS
     */
    @Test
    public void testFactoryIsAServiceBind() {
        DynamicConfiguration config = dcs.createDynamicConfiguration();
        
        FactoryDescriptorsImpl fdi = new FactoryDescriptorsImpl();
        fdi.setAsFactory(BuilderHelper.link(A).build());
        fdi.setAsService(BuilderHelper.link(A).build());
        
        try {
            config.bind(fdi);
        }
        catch (IllegalArgumentException iae) {
            Assert.assertTrue(iae.getMessage(),
                    iae.getMessage().contains("The getFactoryAsFactory descriptor must be of type PROVIDE_METHOD"));
        }
    }
    
    /**
     * The factory descriptor is of type CLASS
     */
    @Test
    public void testServiceIsAFactoryBind() {
        DynamicConfiguration config = dcs.createDynamicConfiguration();
        
        NullDescriptorImpl factoryNDI = new NullDescriptorImpl();
        factoryNDI.setImplementation(A);
        factoryNDI.unNullContracts();
        factoryNDI.unNullType(true);
        factoryNDI.unNullMetadata();
        factoryNDI.unNullQualifiers();
        Set<String> contracts = factoryNDI.getAdvertisedContracts();
        contracts.add(Factory.class.getName());
        
        NullDescriptorImpl classNDI = new NullDescriptorImpl();
        classNDI.setImplementation(A);
        classNDI.unNullContracts();
        classNDI.unNullType(true);  // This is the test, this is going in as a FACTORY
        classNDI.unNullMetadata();
        classNDI.unNullQualifiers();
        contracts = classNDI.getAdvertisedContracts();
        contracts.add(Factory.class.getName());
        
        FactoryDescriptorsImpl fdi = new FactoryDescriptorsImpl();
        fdi.setAsFactory(factoryNDI);
        fdi.setAsService(classNDI);
        
        try {
            config.bind(fdi);
        }
        catch (IllegalArgumentException iae) {
            Assert.assertTrue(iae.getMessage(),
                    iae.getMessage().contains("The getFactoryAsService descriptor must be of type CLASS"));
        }
    }
    
    /**
     * Given a factory that does not implement factory
     */
    @Test
    public void testFactoryDoesNotImplementFactoryBind() {
        DynamicConfiguration config = dcs.createDynamicConfiguration();
        NullDescriptorImpl ndi = new NullDescriptorImpl();
        ndi.setImplementation("");
        ndi.unNullContracts();
        ndi.unNullType(true);
        ndi.unNullMetadata();
        ndi.unNullQualifiers();
        
        try {
            config.bind(ndi);
        }
        catch (IllegalArgumentException iae) {
            Assert.assertTrue(iae.getMessage(),
                    iae.getMessage().contains("A descriptor of type FACTORY does not have Factory in its set of advertised contracts"));
        }
    }
    
    /**
     * You cannot call unbind with null
     */
    @Test(expected=IllegalArgumentException.class)
    public void testNullUnbind() {
        DynamicConfiguration config = dcs.createDynamicConfiguration();
        config.addUnbindFilter(null);
    }
    
    /**
     * You cannot call unbind with null
     */
    @Test(expected=IllegalStateException.class)
    public void testDoubleCommit() {
        DynamicConfiguration config = dcs.createDynamicConfiguration();
        config.commit();  // Not committing anything, but still works!
        config.commit();  // Whoops, don't do this twice
    }
    
    /**
     * Adds an unreified active descriptor
     */
    @Test(expected=IllegalArgumentException.class)
    public void testBindUnreifiedActiveDescriptor() {
        DynamicConfiguration config = dcs.createDynamicConfiguration();
        config.addActiveDescriptor(new UnreifiedActiveDescriptor());
    }
    
    /**
     * Same qualifier passed twice
     */
    @Test
    public void testAskingForDoubleQualifier() {
        try {
          locator.getService(NamedService.class,
                new IsAQualifierImpl(), new IsAQualifierImpl());
          Assert.fail("Asking for valid qualifier twice");
        }
        catch (IllegalArgumentException iae) {
            Assert.assertTrue(iae.getMessage(), iae.getMessage().contains(
                    " appears more than once in the qualifier list"));
        }
    }
    
    /**
     * Invalid name qualifier passed
     */
    @Test
    public void testAskingForBadName() {
        try {
          locator.getService(NamedService.class, "A different name",
                new NamedImpl("NotTheName"));
          Assert.fail("Asking for a name qualifier but named service has a different name");
        }
        catch (IllegalArgumentException iae) {
            Assert.assertTrue(iae.getMessage(), iae.getMessage().contains(
                    ") does not match the value of the @Named qualifier ("));
        }
    }
    
    /**
     * Invalid name qualifier passed (no name value)
     */
    @Test
    public void testAskingForNoName() {
        try {
          locator.getService(NamedService.class, new NamedImpl(""));
          Assert.fail("Asking for a name qualifier but named service has a different name");
        }
        catch (IllegalArgumentException iae) {
            Assert.assertTrue(iae.getMessage(), iae.getMessage().contains(
                    "The @Named qualifier must have a value"));
        }
    }
    
    /**
     * Trying to lookup a type which is neither scope nor qualifier
     */
    @Test
    public void testNotScopeOrQualifier() {
        try {
            locator.getService(NotAQualifier.class);
            Assert.fail("NotAQualifier is neither a qualifier nor scope");
          }
          catch (IllegalArgumentException iae) {
              Assert.assertTrue(iae.getMessage(), iae.getMessage().contains(
                      " must be a scope or annotation"));
          }
    }
    
    /**
     * Trying to create a handle with non-associated descriptor
     */
    @Test(expected=IllegalArgumentException.class)
    public void testHandleFromNonAssocaitedDescriptor() {
        ActiveDescriptor<?> desc = new ForeignDescriptor();
        locator.getServiceHandle(desc);
    }
    
    /**
     * Trying to create a handle generated with this locator, but never bound
     */
    @Test(expected=IllegalArgumentException.class)
    public void testHandleNeverBound() {
        AbstractActiveDescriptor<?> desc = new ForeignDescriptor();
        desc.setReified(false);
        
        ActiveDescriptor<?> reified = locator.reifyDescriptor(desc);
        
        locator.getServiceHandle(reified);
    }
    
    /**
     * Trying to create a handle generated with a different locator
     */
    @Test(expected=IllegalArgumentException.class)
    public void testHandleBoundToDifferentLocator() {
        ServiceLocator otherLocator = LocatorHelper.create();
        ActiveDescriptor<?> desc = ServiceLocatorUtilities.addClasses(otherLocator, ForeignService.class).get(0);
        
        locator.getServiceHandle(desc);
    }
}
