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
package org.jvnet.hk2.metadata.tests;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Named;
import javax.inject.Singleton;

import org.junit.Assert;
import org.glassfish.hk2.api.DescriptorType;
import org.glassfish.hk2.api.DescriptorVisibility;
import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.DescriptorImpl;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import org.junit.Test;
import org.jvnet.hk2.metadata.tests.stub.LargeInterface;

/**
 * Tests for the inhabitant generator
 * 
 * @author jwells
 */
public class InhabitantsGeneratorTest {
    private final static String ZIP_FILE_INHABITANT_NAME = "META-INF/hk2-locator/default";
    
    private final static Map<DescriptorImpl, Integer> EXPECTED_DESCRIPTORS = new HashMap<DescriptorImpl, Integer>();
    
    static {
        
        {
            // This is a descriptor with a defaulted Name and a qualifier and metadata
            DescriptorImpl di = new DescriptorImpl();
            di.setImplementation("org.jvnet.hk2.metadata.tests.ServiceWithDefaultName");
            di.addAdvertisedContract("org.jvnet.hk2.metadata.tests.ServiceWithDefaultName");
            di.setName("ServiceWithDefaultName");
            di.addQualifier(Named.class.getName());
            di.addQualifier(Blue.class.getName());
            di.addMetadata(Constants.KEY1, Constants.VALUE1);
            di.addMetadata(Constants.KEY2, Constants.VALUE2);
        
            EXPECTED_DESCRIPTORS.put(di, 0);
        }
        
        {
            // This is a descriptor with a non-defaulted Name from @Named
            DescriptorImpl di = new DescriptorImpl();
            di.setImplementation("org.jvnet.hk2.metadata.tests.GivenNameFromQualifier");
            di.addAdvertisedContract("org.jvnet.hk2.metadata.tests.GivenNameFromQualifier");
            di.setName(Constants.NON_DEFAULT_NAME);
            di.addQualifier(Named.class.getName());
            di.setScope(Singleton.class.getName());
        
            EXPECTED_DESCRIPTORS.put(di, 0);
        }
        
        {
            // This is a descriptor with a non-defaulted Name from @Service
            DescriptorImpl di = new DescriptorImpl();
            di.setImplementation("org.jvnet.hk2.metadata.tests.ServiceWithName");
            di.addAdvertisedContract("org.jvnet.hk2.metadata.tests.ServiceWithName");
            di.setName(Constants.NON_DEFAULT_NAME);
            di.setScope(Singleton.class.getName());
        
            EXPECTED_DESCRIPTORS.put(di, 0);
        }
        
        {
            // ComplexFactory as a service
            DescriptorImpl di = new DescriptorImpl();
            di.setImplementation("org.jvnet.hk2.metadata.tests.ComplexFactory");
            di.addAdvertisedContract("org.jvnet.hk2.metadata.tests.ComplexFactory");
            di.addAdvertisedContract(Factory.class.getName());
            di.setName("ComplexFactory");
            di.setScope(Singleton.class.getName());
            di.addQualifier(Named.class.getName());
        
            EXPECTED_DESCRIPTORS.put(di, 0);
        }
        
        {
            // ComplexFactory as a factory
            DescriptorImpl di = new DescriptorImpl();
            di.setImplementation("org.jvnet.hk2.metadata.tests.ComplexFactory");
            di.addAdvertisedContract("org.jvnet.hk2.metadata.tests.ComplexImpl");
            di.addAdvertisedContract("org.jvnet.hk2.metadata.tests.ComplexA");
            di.addAdvertisedContract("org.jvnet.hk2.metadata.tests.ComplexC");
            di.addAdvertisedContract("org.jvnet.hk2.metadata.tests.ComplexE");
            di.addAdvertisedContract("org.jvnet.hk2.metadata.tests.ComplexDImpl");
            di.setName(Constants.NON_DEFAULT_NAME);
            di.setScope(PerLookup.class.getName());
            di.setDescriptorType(DescriptorType.PROVIDE_METHOD);
            di.addQualifier(Blue.class.getName());
            di.addQualifier(Named.class.getName());
        
            EXPECTED_DESCRIPTORS.put(di, 0);
        }
        
        {
            // Another complex hierarchy
            DescriptorImpl di = new DescriptorImpl();
            di.setImplementation(TwoContractImpl.class.getName());
            di.addAdvertisedContract(TwoContractImpl.class.getName());
            di.addAdvertisedContract(ComplexG.class.getName());
            di.addAdvertisedContract(ComplexF.class.getName());
            di.addAdvertisedContract(ComplexA.class.getName());
            di.setScope(Singleton.class.getName());
        
            EXPECTED_DESCRIPTORS.put(di, 0);
        }
        
        {
            // This is a descriptor with a non-defaulted Name from @Service
            DescriptorImpl di = new DescriptorImpl();
            di.setImplementation("org.jvnet.hk2.metadata.tests.ContractsProvidedService");
            di.addAdvertisedContract("org.jvnet.hk2.metadata.tests.SimpleInterface");
            di.setScope(Singleton.class.getName());
        
            EXPECTED_DESCRIPTORS.put(di, 0);
        }
        
        {
            // This is a service with a rank
            DescriptorImpl di = new DescriptorImpl();
            di.setImplementation(ServiceWithRank.class.getName());
            di.addAdvertisedContract(ServiceWithRank.class.getName());
            di.setScope(Singleton.class.getName());
            di.setRanking(Constants.RANK);
        
            EXPECTED_DESCRIPTORS.put(di, Constants.RANK);
        }
        
        {
            // This is the Factory that should be generated
            DescriptorImpl envFactory = new DescriptorImpl();
            envFactory.setImplementation(FactoryWithRanks.class.getName());
            envFactory.addAdvertisedContract(FactoryWithRanks.class.getName());
            envFactory.addAdvertisedContract(Factory.class.getName());
            envFactory.setScope(Singleton.class.getName());
            envFactory.setRanking(Constants.RANK);
        
            EXPECTED_DESCRIPTORS.put(envFactory, Constants.RANK);
        }
        
        {
            // This is a factory with a rank
            DescriptorImpl envItself = new DescriptorImpl();
            envItself.setImplementation(FactoryWithRanks.class.getName());
            envItself.addAdvertisedContract(SimpleInterface.class.getName());
            envItself.setRanking(Constants.FACTORY_METHOD_RANK);
            envItself.setDescriptorType(DescriptorType.PROVIDE_METHOD);
        
            EXPECTED_DESCRIPTORS.put(envItself, Constants.FACTORY_METHOD_RANK);
        }
        
        {
            // This is a service with automatic metadata
            DescriptorImpl envItself = new DescriptorImpl();
            envItself.setImplementation(ServiceWithMetadata.class.getName());
            envItself.addAdvertisedContract(ServiceWithMetadata.class.getName());
            envItself.setScope(ScopeWithMetadata.class.getName());
            envItself.addQualifier(QualifierWithMetadata.class.getName());
            envItself.addMetadata(Constants.KEY1, Constants.VALUE1);
            envItself.addMetadata(Constants.KEY2, Constants.VALUE2);
            envItself.addMetadata(Constants.KEY3, Constants.VALUE3);
            envItself.addMetadata(Constants.KEY4, Constants.VALUE4);
            envItself.addMetadata(Constants.KEY5, Constants.VALUE5_1);
            envItself.addMetadata(Constants.KEY5, Constants.VALUE5_2);
            envItself.addMetadata(Constants.KEY5, Constants.VALUE5_3);
            envItself.addMetadata(Constants.KEY6, new Long(Constants.VALUE6_1).toString());
            envItself.addMetadata(Constants.KEY6, new Long(Constants.VALUE6_2).toString());
            envItself.addMetadata(Constants.KEY6, new Long(Constants.VALUE6_3).toString());
        
            EXPECTED_DESCRIPTORS.put(envItself, 0);
        }
        
        {
            // From a service with @UseProxy explicitly set to true
            DescriptorImpl envItself = new DescriptorImpl();
            envItself.setImplementation(ServiceWithTrueProxy.class.getName());
            envItself.addAdvertisedContract(ServiceWithTrueProxy.class.getName());
            envItself.setScope(Singleton.class.getName());
            envItself.setProxiable(Boolean.TRUE);
            
            EXPECTED_DESCRIPTORS.put(envItself, 0);
        }
        
        {
            // From a service with @UseProxy explicitly set to false
            DescriptorImpl envItself = new DescriptorImpl();
            envItself.setImplementation(ServiceWithFalseProxy.class.getName());
            envItself.addAdvertisedContract(ServiceWithFalseProxy.class.getName());
            envItself.setScope(PerLookup.class.getName());
            envItself.setProxiable(Boolean.FALSE);
            
            EXPECTED_DESCRIPTORS.put(envItself, 0);
        }
        
        {
            // From a service with @UseProxy using default value (should be true)
            DescriptorImpl envItself = new DescriptorImpl();
            envItself.setImplementation(ServiceWithDefaultProxy.class.getName());
            envItself.addAdvertisedContract(ServiceWithDefaultProxy.class.getName());
            envItself.setScope(Singleton.class.getName());
            envItself.setProxiable(Boolean.TRUE);
            
            EXPECTED_DESCRIPTORS.put(envItself, 0);
        }
        
        {
            // From a factory with default @UseProxy on the provide method.  Service descriptor
            DescriptorImpl envItself = new DescriptorImpl();
            envItself.setImplementation(FactoryWithDefaultProxy.class.getName());
            envItself.addAdvertisedContract(FactoryWithDefaultProxy.class.getName());
            envItself.addAdvertisedContract(Factory.class.getName());
            envItself.setScope(Singleton.class.getName());
            
            EXPECTED_DESCRIPTORS.put(envItself, 0);
        }
        
        {
            // From a factory with default @UseProxy on the provide method.  Method descriptor
            DescriptorImpl envItself = new DescriptorImpl();
            envItself.setImplementation(FactoryWithDefaultProxy.class.getName());
            envItself.addAdvertisedContract(Object.class.getName());
            envItself.setScope(Singleton.class.getName());
            envItself.setProxiable(Boolean.TRUE);
            envItself.setDescriptorType(DescriptorType.PROVIDE_METHOD);
            
            EXPECTED_DESCRIPTORS.put(envItself, 0);
        }
        
        {
            // From a factory with false @UseProxy on the provide method.  Service descriptor
            DescriptorImpl envItself = new DescriptorImpl();
            envItself.setImplementation(FactoryWithFalseProxy.class.getName());
            envItself.addAdvertisedContract(FactoryWithFalseProxy.class.getName());
            envItself.addAdvertisedContract(Factory.class.getName());
            envItself.setScope(Singleton.class.getName());
            
            EXPECTED_DESCRIPTORS.put(envItself, 0);
        }
        
        {
            // From a factory with false @UseProxy on the provide method.  Method descriptor
            DescriptorImpl envItself = new DescriptorImpl();
            envItself.setImplementation(FactoryWithFalseProxy.class.getName());
            envItself.addAdvertisedContract(Object.class.getName());
            envItself.setScope(Singleton.class.getName());
            envItself.setProxiable(Boolean.FALSE);
            envItself.setDescriptorType(DescriptorType.PROVIDE_METHOD);
            
            EXPECTED_DESCRIPTORS.put(envItself, 0);
        }
        
        {
            // From a service with LOCAL visibility
            DescriptorImpl envItself = new DescriptorImpl();
            envItself.setImplementation(LocalService.class.getName());
            envItself.addAdvertisedContract(LocalService.class.getName());
            envItself.setScope(Singleton.class.getName());
            envItself.setDescriptorVisibility(DescriptorVisibility.LOCAL);
            
            EXPECTED_DESCRIPTORS.put(envItself, 0);
        }
        
        {
            // From a service with NORMAL visiblity
            DescriptorImpl envItself = new DescriptorImpl();
            envItself.setImplementation(NormalService.class.getName());
            envItself.addAdvertisedContract(NormalService.class.getName());
            envItself.setScope(Singleton.class.getName());
            
            EXPECTED_DESCRIPTORS.put(envItself, 0);
        }
        
        {
            // From a factory with LOCAL visiblity (service descriptor)
            DescriptorImpl envItself = new DescriptorImpl();
            envItself.setImplementation(FactoryWithVisibility.class.getName());
            envItself.addAdvertisedContract(FactoryWithVisibility.class.getName());
            envItself.addAdvertisedContract(Factory.class.getName());
            envItself.setDescriptorVisibility(DescriptorVisibility.LOCAL);
            
            EXPECTED_DESCRIPTORS.put(envItself, 0);
        }
        
        {
            // From a factory with LOCAL visibility (method descriptor)
            DescriptorImpl envItself = new DescriptorImpl();
            envItself.setImplementation(FactoryWithVisibility.class.getName());
            envItself.addAdvertisedContract(String.class.getName());
            envItself.setDescriptorVisibility(DescriptorVisibility.LOCAL);
            envItself.setDescriptorType(DescriptorType.PROVIDE_METHOD);
            
            EXPECTED_DESCRIPTORS.put(envItself, 0);
        }
        
        {
            // From a factory with LOCAL visibility (method descriptor)
            DescriptorImpl envItself = new DescriptorImpl();
            envItself.setImplementation(CustomAnalysisService.class.getName());
            envItself.addAdvertisedContract(CustomAnalysisService.class.getName());
            envItself.setClassAnalysisName(Constants.CUSTOM_ANALYZER);
            envItself.setScope(Singleton.class.getName());
            
            EXPECTED_DESCRIPTORS.put(envItself, 0);
        }
        
        {
            // This is a descriptor of an abstract factory resolved by parent
            DescriptorImpl di = new DescriptorImpl();
            di.setImplementation("org.jvnet.hk2.metadata.tests.ConcreteFactory");
            di.addAdvertisedContract("java.lang.Integer");
            di.addQualifier(Blue.class.getName());
            di.setDescriptorType(DescriptorType.PROVIDE_METHOD);
            di.setScope(Singleton.class.getName());
        
            EXPECTED_DESCRIPTORS.put(di, 0);
        }
        
        {
            // This is a descriptor of an abstract factory resolved by parent
            DescriptorImpl di = new DescriptorImpl();
            di.setImplementation("org.jvnet.hk2.metadata.tests.ConcreteFactory");
            di.addAdvertisedContract("org.jvnet.hk2.metadata.tests.ConcreteFactory");
            di.addAdvertisedContract(Factory.class.getName());
            di.setScope(Singleton.class.getName());
        
            EXPECTED_DESCRIPTORS.put(di, 0);
        }
        
        {
            // This is a descriptor of an abstract factory resolved by parent in a complex hierarchy of Types
            DescriptorImpl di = new DescriptorImpl();
            di.setImplementation("org.jvnet.hk2.metadata.tests.complextypefactory.ConcreteComplexFactory");
            di.addAdvertisedContract("org.jvnet.hk2.metadata.tests.complextypefactory.SomeInterface");
            di.setDescriptorType(DescriptorType.PROVIDE_METHOD);
            di.setScope(PerLookup.class.getName());
        
            EXPECTED_DESCRIPTORS.put(di, 0);
        }
        
        {
            // This is a descriptor of an abstract factory resolved by parent in a complex hierarchy of Types
            DescriptorImpl di = new DescriptorImpl();
            di.setImplementation("org.jvnet.hk2.metadata.tests.complextypefactory.ConcreteComplexFactory");
            di.addAdvertisedContract("org.jvnet.hk2.metadata.tests.complextypefactory.ConcreteComplexFactory");
            di.addAdvertisedContract("org.jvnet.hk2.metadata.tests.complextypefactory.InterfaceWithTwoTypes");
            di.addAdvertisedContract("org.glassfish.hk2.api.Factory");
            di.setScope(Singleton.class.getName());
        
            EXPECTED_DESCRIPTORS.put(di, 0);
        }
        
        {
            // This descriptor has embedded classes
            DescriptorImpl di = new DescriptorImpl();
            di.setImplementation(TripleTroubleService.class.getName());
            di.addAdvertisedContract(TripleTroubleService.class.getName());
            di.setScope(Singleton.class.getName());
        
            EXPECTED_DESCRIPTORS.put(di, 0);
        }
        
        {
            // This descriptor has embedded classes
            DescriptorImpl di = new DescriptorImpl();
            di.setImplementation(TripleTroubleService.DoubleTroubleService.class.getName());
            di.addAdvertisedContract(TripleTroubleService.DoubleTroubleService.class.getName());
            di.setScope(Singleton.class.getName());
        
            EXPECTED_DESCRIPTORS.put(di, 0);
        }
        
        {
            // This descriptor has embedded classes
            DescriptorImpl di = new DescriptorImpl();
            di.setImplementation(TripleTroubleService.DoubleTroubleService.SingleTroubleService.class.getName());
            di.addAdvertisedContract(TripleTroubleService.DoubleTroubleService.SingleTroubleService.class.getName());
            di.setScope(Singleton.class.getName());
        
            EXPECTED_DESCRIPTORS.put(di, 0);
        }
        
        {
            // This descriptor has @ProxyForSameScope with no explicit value set (should be true)
            DescriptorImpl di = new DescriptorImpl();
            di.setImplementation(ServiceWithDefaultProxyForSameScope.class.getName());
            di.addAdvertisedContract(ServiceWithDefaultProxyForSameScope.class.getName());
            di.setScope(ProxiableScope.class.getName());
            di.setProxyForSameScope(Boolean.TRUE);
        
            EXPECTED_DESCRIPTORS.put(di, 0);
        }
        
        {
            // This descriptor has @ProxyForSameScope set to false
            DescriptorImpl di = new DescriptorImpl();
            di.setImplementation(ServiceWithFalseProxyForSameScope.class.getName());
            di.addAdvertisedContract(ServiceWithFalseProxyForSameScope.class.getName());
            di.setScope(ProxiableScope.class.getName());
            di.setProxyForSameScope(Boolean.FALSE);
        
            EXPECTED_DESCRIPTORS.put(di, 0);
        }
        
        {
            // This descriptor is a factory for a provide method with @ProxyForSameScope with no explicit value set (should be true)
            DescriptorImpl di = new DescriptorImpl();
            di.setImplementation(FactoryWithDefaultProxyForSameScope.class.getName());
            di.addAdvertisedContract(FactoryWithDefaultProxyForSameScope.class.getName());
            di.addAdvertisedContract(Factory.class.getName());
            di.setScope(Singleton.class.getName());
        
            EXPECTED_DESCRIPTORS.put(di, 0);
        }
        
        {
            // This descriptor is the provide method for a factory with @ProxyForSameScope with no explicit value set (should be true)
            DescriptorImpl di = new DescriptorImpl();
            di.setImplementation(FactoryWithDefaultProxyForSameScope.class.getName());
            di.addAdvertisedContract(List.class.getName());
            di.setScope(ProxiableScope.class.getName());
            di.setDescriptorType(DescriptorType.PROVIDE_METHOD);
            di.setProxyForSameScope(Boolean.TRUE);
        
            EXPECTED_DESCRIPTORS.put(di, 0);
        }
        
        {
            // This descriptor is a factory for a provide method with @ProxyForSameScope with no explicit value set (should be true)
            DescriptorImpl di = new DescriptorImpl();
            di.setImplementation(FactoryWithFalseProxyForSameScope.class.getName());
            di.addAdvertisedContract(FactoryWithFalseProxyForSameScope.class.getName());
            di.addAdvertisedContract(Factory.class.getName());
            di.setScope(Singleton.class.getName());
        
            EXPECTED_DESCRIPTORS.put(di, 0);
        }
        
        {
            // This descriptor is the provide method for a factory with @ProxyForSameScope with no explicit value set (should be true)
            DescriptorImpl di = new DescriptorImpl();
            di.setImplementation(FactoryWithFalseProxyForSameScope.class.getName());
            di.addAdvertisedContract(Map.class.getName());
            di.setScope(ProxiableScope.class.getName());
            di.setDescriptorType(DescriptorType.PROVIDE_METHOD);
            di.setProxyForSameScope(Boolean.FALSE);
        
            EXPECTED_DESCRIPTORS.put(di, 0);
        }
        
        {
            // This descriptor is the provide method for a factory with @ProxyForSameScope with no explicit value set (should be true)
            DescriptorImpl di = new DescriptorImpl();
            di.setImplementation("org.jvnet.hk2.metadata.tests.faux.stub.AbstractLargeInterface_hk2Stub");
            di.addAdvertisedContract("org.jvnet.hk2.metadata.tests.faux.stub.AbstractLargeInterface_hk2Stub");
            di.addAdvertisedContract("org.jvnet.hk2.metadata.tests.stub.LargeInterface");
            di.setScope(Singleton.class.getName());
            di.setRanking(1);
        
            EXPECTED_DESCRIPTORS.put(di, 1);
        }
        
        {
            // This descriptor is the provide method for a factory with @ProxyForSameScope with no explicit value set (should be true)
            DescriptorImpl di = new DescriptorImpl();
            di.setImplementation("org.jvnet.hk2.metadata.tests.stub.impl.NotUseableLargeInterface");
            di.addAdvertisedContract("org.jvnet.hk2.metadata.tests.stub.impl.NotUseableLargeInterface");
            di.addAdvertisedContract("org.jvnet.hk2.metadata.tests.stub.LargeInterface");
            di.setScope(Singleton.class.getName());
        
            EXPECTED_DESCRIPTORS.put(di, 0);
        }
    }
    
    private void getAllDescriptorsFromInputStream(InputStream is, Set<DescriptorImpl> retVal) throws IOException {
        BufferedReader pr = new BufferedReader(new InputStreamReader(is));
        
        while (pr.ready()) {
            DescriptorImpl di = new DescriptorImpl();
            
            if (!di.readObject(pr)) {
                continue;
            }
            
            retVal.add(di);
        }
    }
    
    private void checkDescriptors(Set<DescriptorImpl> dis) {
        for (DescriptorImpl di : dis) {
            Assert.assertTrue("Did not find " + di + " in the expected descriptors <<<" +
              EXPECTED_DESCRIPTORS + ">>>", EXPECTED_DESCRIPTORS.containsKey(di));
            
            // The rank is not part of the calculated equals or hash code (since it can change
            // over the course of the lifeycle of the object) and hence must be checked
            // separately from the containsKey above
            int expectedRank = EXPECTED_DESCRIPTORS.get(di);
            Assert.assertEquals("Expected Descriptor is: " + di + " with expected rank " + expectedRank, expectedRank, di.getRanking());
        }
        
        HashMap<DescriptorImpl, Integer> missing = new HashMap<DescriptorImpl, Integer>(EXPECTED_DESCRIPTORS);
        for (DescriptorImpl disMe : dis) {
            missing.remove(disMe);
        }
        
        Assert.assertEquals("Expecting " + EXPECTED_DESCRIPTORS.size() + " descriptors, but only got " + dis.size() +
                ".  The missing descriptors are " + missing,
                EXPECTED_DESCRIPTORS.size(), dis.size());
    }
    
    /**
     * Tests generating into a directory
     * @throws IOException 
     * @throws FileNotFoundException 
     */
    @Test // @org.junit.Ignore
    public void testDefaultDirectoryGeneration() throws IOException {
        Enumeration<URL> defaultFiles = getClass().getClassLoader().getResources(ZIP_FILE_INHABITANT_NAME);
        Assert.assertNotNull(defaultFiles);
        
        Set<DescriptorImpl> generatedImpls = new HashSet<DescriptorImpl>();
        while (defaultFiles.hasMoreElements()) {
            URL defaultFile = defaultFiles.nextElement();
            
            InputStream defaultFileStream = defaultFile.openStream();
            try {
                getAllDescriptorsFromInputStream(
                    defaultFile.openStream(), generatedImpls);
            
                
            }
            finally {
                // The test should be clean
                defaultFileStream.close();
            }
        }
        
        checkDescriptors(generatedImpls);
    }
    
    /**
     * Makes sure that the stubbed interface is used not the one from the main jar
     */
    @Test
    public void testGetsStubImplementationRatherThanOneFromMain() {
        ServiceLocator locator = ServiceLocatorUtilities.createAndPopulateServiceLocator();
        
        LargeInterface li = locator.getService(LargeInterface.class);
        
        Assert.assertEquals(0, li.methodInt(27));
    }
}
