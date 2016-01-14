/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2011-2016 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.hk2.tests.api;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Named;
import javax.inject.Singleton;

import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.Descriptor;
import org.glassfish.hk2.api.DescriptorType;
import org.glassfish.hk2.api.DescriptorVisibility;
import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.api.FactoryDescriptors;
import org.glassfish.hk2.api.Filter;
import org.glassfish.hk2.api.IndexedFilter;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.hk2.api.ServiceHandle;
import org.glassfish.hk2.tests.contracts.AnotherContract;
import org.glassfish.hk2.tests.contracts.SomeContract;
import org.glassfish.hk2.tests.services.AnotherService;
import org.glassfish.hk2.utilities.AbstractActiveDescriptor;
import org.glassfish.hk2.utilities.BuilderHelper;
import org.glassfish.hk2.utilities.DescriptorImpl;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author jwells
 *
 */
public class BuilderHelperTest {
	private final static String NAME = "hello";
	
	/** metadata1 key */
	public final static String METAKEY1 = "metakey1";
	/** metadata2 key */
	public final static String METAKEY2 = "metakey2";
	
	/** scope data */
	public final static String SCOPE_DATA = "scopeData";
	/** qualifier value */
	public final static String QUALIFIER_VALUE = "qualValue";
	/** another qualifier value */
	public final static int QUALIFIER_ANOTHER_VALUE = -1;
	
	/** analyze me */
	public final static String ANALYZE_SERVICE = "analyzeMe";
	
	/** Rank used in superclass tests */
	public final static int RANK_IN_SUPERCLASS = 7;
	
	/**
	 * This predicate will only have an implementation and a contract
	 */
	@Test
	public void testSimpleFilter() {
		Descriptor predicate = BuilderHelper.link(BuilderHelperTest.class).to(SomeContract.class).build();
		
		Assert.assertNotNull(predicate);
		
		Assert.assertNotNull(predicate.getImplementation());
		Assert.assertEquals(predicate.getImplementation(), BuilderHelperTest.class.getName());
		
		Assert.assertNotNull(predicate.getAdvertisedContracts());
		Assert.assertTrue(predicate.getAdvertisedContracts().size() == 2);
		
		Assert.assertNotNull(predicate.getMetadata());
		Assert.assertTrue(predicate.getMetadata().size() == 0);
		
		Assert.assertNotNull(predicate.getQualifiers());
		Assert.assertTrue(predicate.getQualifiers().size() == 0);
		
		Assert.assertNull(predicate.getName());
		
		Assert.assertNull(predicate.getScope());
	}
	
	private final static String KEY_A = "keya";
	private final static String KEY_B = "keyb";
	private final static String VALUE_A = "valuea";
	private final static String VALUE_B1 = "valueb1";
	private final static String VALUE_B2 = "valueb2";
	
	/**
	 * This predicate will have two of those things which allow multiples and
	 * one thing of all other things
	 */
	@Test
	public void testFullFilter() {
		LinkedList<String> multiValue = new LinkedList<String>();
		multiValue.add(VALUE_B1);
		multiValue.add(VALUE_B2);
		
		
		Descriptor predicate = BuilderHelper.link(AnotherService.class.getName()).
				to(SomeContract.class).
				to(AnotherContract.class.getName()).
				in(Singleton.class.getName()).
				named(NAME).
				has(KEY_A, VALUE_A).
				has(KEY_B, multiValue).
				qualifiedBy(Red.class.getName()).
				localOnly().
				analyzeWith(ANALYZE_SERVICE).
				build();
		
		Assert.assertNotNull(predicate);
		
		Assert.assertNotNull(predicate.getImplementation());
		Assert.assertEquals(predicate.getImplementation(), AnotherService.class.getName());
		
		HashSet<String> correctSet = new HashSet<String>();
		correctSet.add(SomeContract.class.getName());
		correctSet.add(AnotherContract.class.getName());
		correctSet.add(AnotherService.class.getName());
		
		Assert.assertNotNull(predicate.getAdvertisedContracts());
		Assert.assertTrue(predicate.getAdvertisedContracts().size() == 3);
		Assert.assertTrue(correctSet.containsAll(predicate.getAdvertisedContracts()));
		
		correctSet.clear();
		correctSet.add(Red.class.getName());
		correctSet.add(Named.class.getName());
		
		Assert.assertNotNull(predicate.getQualifiers());
		Assert.assertTrue(predicate.getQualifiers().size() == 2);  // One for @Named
		Assert.assertTrue(correctSet.containsAll(predicate.getQualifiers()));
		
		Assert.assertEquals(NAME, predicate.getName());
		
		Assert.assertEquals(DescriptorVisibility.LOCAL, predicate.getDescriptorVisibility());
		
		Assert.assertNotNull(predicate.getScope());
		Assert.assertEquals(Singleton.class.getName(), predicate.getScope());
		
		Assert.assertNotNull(predicate.getMetadata());
		Assert.assertTrue(predicate.getMetadata().size() == 2);
		
		Map<String, List<String>> metadata = predicate.getMetadata();
		Set<String> keySet = metadata.keySet();
		
		correctSet.clear();
		correctSet.add(KEY_A);
		correctSet.add(KEY_B);
		
		Assert.assertTrue(correctSet.containsAll(keySet));
		
		List<String> aValue = metadata.get(KEY_A);
		Assert.assertNotNull(aValue);
		Assert.assertTrue(aValue.size() == 1);
		Assert.assertEquals(aValue.get(0), VALUE_A);
		
		List<String> bValue = metadata.get(KEY_B);
		Assert.assertNotNull(bValue);
		Assert.assertTrue(bValue.size() == 2);
		Assert.assertEquals(bValue.get(0), VALUE_B1);
		Assert.assertEquals(bValue.get(1), VALUE_B2);
		
		Assert.assertSame(ANALYZE_SERVICE, predicate.getClassAnalysisName());
	}
	
	/**
	 * Tests the all descriptor filter
	 */
	@Test
    public void testAllDescriptorFilter() {
        Descriptor predicate = BuilderHelper.link("empty").build();
    
        Filter allFilter = BuilderHelper.allFilter();
    
        Assert.assertTrue(allFilter.matches(predicate));
    }
	
	/**
     * Tests the all descriptor filter
     */
    @Test
    public void testConstantFilter() {
        FullDescriptorImpl c = new FullDescriptorImpl();
        ActiveDescriptor<FullDescriptorImpl> cDesc = BuilderHelper.createConstantDescriptor(c);
        Assert.assertNotNull(cDesc);
        
        Assert.assertEquals(FullDescriptorImpl.class.getName(), cDesc.getImplementation());
        Assert.assertEquals(FullDescriptorImpl.class, cDesc.getImplementationClass());
        
        Assert.assertEquals(3, cDesc.getAdvertisedContracts().size());
        Assert.assertTrue(cDesc.getAdvertisedContracts().contains(FullDescriptorImpl.class.getName()));
        Assert.assertTrue(cDesc.getAdvertisedContracts().contains(MarkerInterface2.class.getName()));
        Assert.assertTrue(cDesc.getAdvertisedContracts().contains(MarkerInterface4.class.getName()));
        
        Assert.assertEquals(3, cDesc.getContractTypes().size());
        Assert.assertTrue(cDesc.getContractTypes().contains(FullDescriptorImpl.class));
        Assert.assertTrue(cDesc.getContractTypes().contains(MarkerInterface2.class));
        Assert.assertTrue(cDesc.getContractTypes().contains(MarkerInterface4.class));
        
        Assert.assertNull(cDesc.getName());
        
        Assert.assertEquals(PerLookup.class.getName(), cDesc.getScope());
        Assert.assertEquals(PerLookup.class, cDesc.getScopeAnnotation());
        
        Assert.assertEquals(3, cDesc.getQualifiers().size());
        Assert.assertTrue(cDesc.getQualifiers().contains(Red.class.getName()));
        Assert.assertTrue(cDesc.getQualifiers().contains(Green.class.getName()));
        Assert.assertTrue(cDesc.getQualifiers().contains(Blue.class.getName()));
        
        Assert.assertEquals(3, cDesc.getQualifierAnnotations().size());
        boolean red = false;
        boolean green = false;
        boolean blue = false;
        
        for (Annotation anno : cDesc.getQualifierAnnotations()) {
            if (Red.class.equals(anno.annotationType())) red = true;
            if (Green.class.equals(anno.annotationType())) green = true;
            if (Blue.class.equals(anno.annotationType())) blue = true;
        }
        
        Assert.assertTrue(red);
        Assert.assertTrue(green);
        Assert.assertTrue(blue);
        
        Assert.assertEquals(DescriptorType.CLASS, cDesc.getDescriptorType());
        Assert.assertTrue(cDesc.getMetadata().isEmpty());
        Assert.assertNull(cDesc.getLoader());
        Assert.assertEquals(0, cDesc.getRanking());
        Assert.assertNull(cDesc.getServiceId());
        Assert.assertNull(cDesc.getLocatorId());
        Assert.assertTrue(cDesc.isReified());
        Assert.assertTrue(cDesc.getInjectees().isEmpty());
        
        Assert.assertEquals(c, cDesc.create(null));
        
        // Call the destroy, though it should do nothing
        cDesc.dispose(c);
        
        // Check the cache
        Assert.assertEquals(c, cDesc.getCache());
        Assert.assertTrue(cDesc.isCacheSet());
        
        String asString = cDesc.toString();
        Assert.assertTrue(asString.contains("implementation=org.glassfish.hk2.tests.api.FullDescriptorImpl"));
    }
    
    /**
     * Tests the all descriptor filter
     */
    @Test
    public void testConstantWithContractsProvided() {
        ContractsProvidedService cps = new ContractsProvidedService();
        ActiveDescriptor<ContractsProvidedService> cDesc = BuilderHelper.createConstantDescriptor(cps);
        Assert.assertNotNull(cDesc);
        
        Assert.assertEquals(ContractsProvidedService.class.getName(), cDesc.getImplementation());
        Assert.assertEquals(ContractsProvidedService.class, cDesc.getImplementationClass());
        
        Assert.assertEquals(1, cDesc.getAdvertisedContracts().size());
        Assert.assertTrue(cDesc.getAdvertisedContracts().contains(MarkerInterface.class.getName()));
        
        Assert.assertEquals(1, cDesc.getContractTypes().size());
        Assert.assertTrue(cDesc.getContractTypes().contains(MarkerInterface.class));
        
        Assert.assertNull(cDesc.getName());
        
        Assert.assertEquals(PerLookup.class.getName(), cDesc.getScope());
        Assert.assertEquals(PerLookup.class, cDesc.getScopeAnnotation());
        
        Assert.assertEquals(0, cDesc.getQualifiers().size());
        
        Assert.assertEquals(0, cDesc.getQualifierAnnotations().size());
        
        Assert.assertEquals(DescriptorType.CLASS, cDesc.getDescriptorType());
        Assert.assertTrue(cDesc.getMetadata().isEmpty());
        Assert.assertNull(cDesc.getLoader());
        Assert.assertEquals(0, cDesc.getRanking());
        Assert.assertNull(cDesc.getServiceId());
        Assert.assertNull(cDesc.getLocatorId());
        Assert.assertTrue(cDesc.isReified());
        Assert.assertTrue(cDesc.getInjectees().isEmpty());
        
        Assert.assertEquals(cps, cDesc.create(null));
        
        // Call the destroy, though it should do nothing
        cDesc.dispose(cps);
        
        // Check the cache
        Assert.assertEquals(cps, cDesc.getCache());
        Assert.assertTrue(cDesc.isCacheSet());
        
        String asString = cDesc.toString();
        Assert.assertTrue(asString.contains("implementation=org.glassfish.hk2.tests.api.ContractsProvidedService"));
    }
    
    /**
     * Tests the contract filter
     */
    @Test
    public void testCreateContractFilter() {
        IndexedFilter iff = BuilderHelper.createContractFilter(Object.class.getName());
        
        Assert.assertEquals(Object.class.getName(), iff.getAdvertisedContract());
        Assert.assertNull(iff.getName());
        Assert.assertTrue(iff.matches(new DescriptorImpl()));
    }
    
    /**
     * Tests the contract filter
     */
    @Test
    public void testCreateNameFilter() {
        IndexedFilter iff = BuilderHelper.createNameFilter(NAME);
        
        Assert.assertEquals(NAME, iff.getName());
        Assert.assertNull(iff.getAdvertisedContract());
        Assert.assertTrue(iff.matches(new DescriptorImpl()));
    }
    
    /**
     * Tests the contract filter
     */
    @Test
    public void testCreateNameAndContractFilter() {
        IndexedFilter iff = BuilderHelper.createNameAndContractFilter(Object.class.getName(), NAME);
        
        Assert.assertEquals(NAME, iff.getName());
        Assert.assertEquals(Object.class.getName(), iff.getAdvertisedContract());
        Assert.assertTrue(iff.matches(new DescriptorImpl()));
    }
    
    /**
     * Tests the contract filter
     */
    @Test
    public void testDeepCopy() {
        DescriptorImpl a = new DescriptorImpl();
        a.addMetadata(KEY_A, VALUE_A);
        
        DescriptorImpl b = BuilderHelper.deepCopyDescriptor(a);
        
        Assert.assertEquals(a, b);
        
        // This is a bit tricky, make sure we have separated the metadata values!
        b.addMetadata(KEY_A, VALUE_B1);
        
        Assert.assertFalse(a.equals(b));
        
        Map<String, List<String>> aMeta= a.getMetadata();
        List<String> keyAValues = aMeta.get(KEY_A);
        Assert.assertEquals(1, keyAValues.size());
        
        Assert.assertEquals(VALUE_A, keyAValues.get(0));
    }
    
    /**
     * Tests that a class with a complex hierarchy is analyzed properly
     */
    @Test
    public void testComplexHierarchyClass() {
        Descriptor d = BuilderHelper.createDescriptorFromClass(ComplexHierarchy.class);
        
        Set<String> contracts = d.getAdvertisedContracts();
        
        Assert.assertTrue(contracts.contains(ComplexHierarchy.class.getName()));
        Assert.assertTrue(contracts.contains(MarkerInterfaceImpl.class.getName()));
        Assert.assertTrue(contracts.contains(MarkerInterface2.class.getName()));
        Assert.assertTrue(contracts.contains(ParameterizedInterface.class.getName()));
        
        Assert.assertEquals(4, contracts.size());
    }
    
    /**
     * Tests that an object with a complex hierarchy is analyzed properly
     */
    @Test
    public void testComplexHierarchyObject() {
        AbstractActiveDescriptor<ComplexHierarchy> d = BuilderHelper.createConstantDescriptor(new ComplexHierarchy());
        
        Set<String> contracts = d.getAdvertisedContracts();
        
        Assert.assertTrue(contracts.contains(ComplexHierarchy.class.getName()));
        Assert.assertTrue(contracts.contains(MarkerInterfaceImpl.class.getName()));
        Assert.assertTrue(contracts.contains(MarkerInterface2.class.getName()));
        Assert.assertTrue(contracts.contains(ParameterizedInterface.class.getName()));
        
        Assert.assertEquals(4, contracts.size());
        
        Set<Type> contractsAsTypes = d.getContractTypes();
        
        int lcv = 0;
        for (Type contractAsType : contractsAsTypes) {
            // This tests that this is an ordered iterator
            switch(lcv) {
            case 0:
                Assert.assertEquals(ComplexHierarchy.class, contractAsType);
                break;
            case 1:
                Assert.assertEquals(MarkerInterfaceImpl.class, contractAsType);
                break;
            case 2:
                Assert.assertEquals(MarkerInterface2.class, contractAsType);
                break;
            case 3:
                ParameterizedType pt = (ParameterizedType) contractAsType;
                
                Assert.assertEquals(ParameterizedInterface.class, pt.getRawType());
                Assert.assertEquals(String.class, pt.getActualTypeArguments()[0]);
                break;
            default:
                Assert.fail("Too many types: " + contractAsType);
            }
            
            lcv++;
        }
        
        Assert.assertNotNull(d.isProxiable());
        Assert.assertEquals(false, d.isProxiable().booleanValue());
        
        Assert.assertNotNull(d.isProxyForSameScope());
        Assert.assertEquals(true, d.isProxyForSameScope().booleanValue());
    }
    
    /**
     * Tests that the metadata is properly added to automaticaly generated descriptors
     */
    @Test
    public void testAutoMetadata() {
        ServiceWithAutoMetadata obj = new ServiceWithAutoMetadata();
        
        AbstractActiveDescriptor<?> ad = BuilderHelper.createConstantDescriptor(obj);
        
        Map<String, List<String>> metadata = ad.getMetadata();
        
        Assert.assertEquals(metadata.toString(), 2, metadata.size());
        
        {
            List<String> metadata1Values = metadata.get(METAKEY1);
            Assert.assertEquals(metadata1Values.toString(), 3, metadata1Values.size());
        
            Assert.assertTrue(metadata1Values.contains(SCOPE_DATA));
            Assert.assertTrue(metadata1Values.contains(QUALIFIER_VALUE));
        
            String findMe = QualifierWithMetadata.Mode.VALIDATING.toString();
        
            Assert.assertTrue(metadata1Values.contains(findMe));
        }
        
        {
            List<String> metadata2Values = metadata.get(METAKEY2);
            Assert.assertEquals(metadata2Values.toString(), 1, metadata2Values.size());
        
            Assert.assertTrue(metadata2Values.contains("-1"));
        }
        
        Assert.assertNotNull(ad.isProxiable());
        Assert.assertEquals(true, ad.isProxiable().booleanValue());
        
        Assert.assertNull(ad.isProxyForSameScope());
        
        Assert.assertEquals(DescriptorVisibility.LOCAL, ad.getDescriptorVisibility());
    }
    
    /**
     * Tests that the metadata is properly added to automaticaly generated descriptors
     */
    @Test
    public void testSpecificFilter() {
        SimpleService simpleService = new SimpleService();
        
        AbstractActiveDescriptor<SimpleService> aad1 = BuilderHelper.createConstantDescriptor(simpleService);
        AbstractActiveDescriptor<SimpleService> aad2 = BuilderHelper.createConstantDescriptor(simpleService);
        AbstractActiveDescriptor<SimpleService> aad3 = BuilderHelper.createConstantDescriptor(simpleService);
        
        aad1.setServiceId(new Long(1));
        aad1.setLocatorId(new Long(0));
        
        aad2.setServiceId(new Long(2));
        aad2.setLocatorId(new Long(0));
        
        aad3.setServiceId(new Long(1));
        aad3.setLocatorId(new Long(1));
        
        IndexedFilter specificFilter = BuilderHelper.createSpecificDescriptorFilter(aad1);
        
        Assert.assertEquals(SimpleService.class.getName(), specificFilter.getAdvertisedContract());
        Assert.assertNull(specificFilter.getName());
        
        Assert.assertTrue(specificFilter.matches(aad1));
        Assert.assertFalse(specificFilter.matches(aad2));
        Assert.assertFalse(specificFilter.matches(aad3));
        
    }
    
    /**
     * Tests I can create a factory that uses UseProxy
     */
    @Test
    public void testUseProxyOnMethod() {
        FactoryDescriptors dis = BuilderHelper.link(FactoryWithUseProxy.class.getName()).
                to(Boolean.class).
                proxy().
                proxyForSameScope().
                buildFactory();
        
        Descriptor serviceDI = dis.getFactoryAsAService();
        
        Assert.assertEquals(FactoryWithUseProxy.class.getName(), serviceDI.getImplementation());
        Assert.assertTrue(serviceDI.getAdvertisedContracts().contains(Factory.class.getName()));
        Assert.assertNull(serviceDI.isProxiable());
        Assert.assertNull(serviceDI.isProxyForSameScope());
        
        Descriptor providerMethodDI = dis.getFactoryAsAFactory();
        
        Assert.assertEquals(FactoryWithUseProxy.class.getName(), providerMethodDI.getImplementation());
        Assert.assertTrue(providerMethodDI.getAdvertisedContracts().contains(Boolean.class.getName()));
        Assert.assertEquals(Boolean.TRUE, providerMethodDI.isProxiable());
        Assert.assertEquals(Boolean.TRUE, providerMethodDI.isProxyForSameScope());
    }
    
    /**
     * Tests createDescriptorFromClass honors UseProxy
     */
    @Test
    public void testUseProxyOnClass() {
        Descriptor di = BuilderHelper.createDescriptorFromClass(ServiceWithUseProxy.class);
        
        Assert.assertEquals(ServiceWithUseProxy.class.getName(), di.getImplementation());
        Assert.assertTrue(di.getAdvertisedContracts().contains(ServiceWithUseProxy.class.getName()));
        Assert.assertEquals(Boolean.FALSE, di.isProxiable());
        Assert.assertEquals(Boolean.FALSE, di.isProxyForSameScope());
    }
    
    /**
     * This predicate will have two of those things which allow multiples and
     * one thing of all other things
     */
    @Test
    public void testSetVisibility() {
        
        Descriptor predicate1 = BuilderHelper.link(AnotherService.class.getName()).
                visibility(DescriptorVisibility.LOCAL).
                build();
        
        Assert.assertNotNull(predicate1);
        Assert.assertEquals(DescriptorVisibility.LOCAL, predicate1.getDescriptorVisibility());
        
        Descriptor predicate2 = BuilderHelper.link(AnotherService.class.getName()).
                visibility(DescriptorVisibility.NORMAL).
                build();
        
        Assert.assertNotNull(predicate2);
        Assert.assertEquals(DescriptorVisibility.NORMAL, predicate2.getDescriptorVisibility());
        
        Assert.assertFalse(predicate1.equals(predicate2));
        
        try {
            BuilderHelper.link(AnotherService.class.getName()).
                visibility(null);
            Assert.fail("Should have thrown IllegalArgumentException");
        }
        catch (IllegalArgumentException iae) {
            // Success
        }
        
    }
    
    /**
     * Tests createDescriptorFromClass honors Visibility
     */
    @Test
    public void testVisibilityOnClass() {
        Descriptor di = BuilderHelper.createDescriptorFromClass(ServiceWithLocalVisibility.class);
        
        Assert.assertEquals(DescriptorVisibility.LOCAL, di.getDescriptorVisibility());
        
        di = BuilderHelper.createDescriptorFromClass(ServiceWithNormalVisibility.class);
        
        Assert.assertEquals(DescriptorVisibility.NORMAL, di.getDescriptorVisibility());
    }
    
    /**
     * Tests rank is honored in from class
     */
    @Test // @org.junit.Ignore
    public void testServicesWithRanksFromClass() {
        Descriptor ss = BuilderHelper.createDescriptorFromClass(SimpleService.class);
        Descriptor r10 = BuilderHelper.createDescriptorFromClass(ServiceWithRank10.class);
        Descriptor r20 = BuilderHelper.createDescriptorFromClass(ServiceWithRank20.class);
        
        Assert.assertEquals(0, ss.getRanking());
        Assert.assertEquals(10, r10.getRanking());
        Assert.assertEquals(20, r20.getRanking());
    }
    
    /**
     * Tests rank is honored on constants
     */
    @Test // @org.junit.Ignore
    public void testServicesWithRanksFromConstants() {
        Descriptor ss = BuilderHelper.createConstantDescriptor(new SimpleService());
        Descriptor r10 = BuilderHelper.createConstantDescriptor(new ServiceWithRank10());
        Descriptor r20 = BuilderHelper.createConstantDescriptor(new ServiceWithRank20());
        
        Assert.assertEquals(0, ss.getRanking());
        Assert.assertEquals(10, r10.getRanking());
        Assert.assertEquals(20, r20.getRanking());
    }
    
    private final static String ACME_IMPL = "com.acme.FooImpl";
    private final static String ACME_INTF = "com.acme.Foo";
    private final static String NAME1 = "name1";
    private final static String QUAL1 = "qual1";
    private final static String QUAL2 = "qual2";
    
    private final static String TOKEN1 = ACME_INTF;
    private final static String TOKEN2 = ACME_INTF +
            BuilderHelper.TOKEN_SEPARATOR + BuilderHelper.NAME_KEY + "=" + NAME1;
    private final static String TOKEN3 = ACME_INTF +
            BuilderHelper.TOKEN_SEPARATOR + BuilderHelper.QUALIFIER_KEY + "=" + QUAL2;
    private final static String TOKEN4 = ACME_INTF +
            BuilderHelper.TOKEN_SEPARATOR + BuilderHelper.QUALIFIER_KEY + "=" + QUAL2 +
            BuilderHelper.TOKEN_SEPARATOR + BuilderHelper.NAME_KEY + "=" + NAME1 +
            BuilderHelper.TOKEN_SEPARATOR + BuilderHelper.QUALIFIER_KEY + "=" + QUAL1;
    private final static String TOKEN5 = 
            BuilderHelper.TOKEN_SEPARATOR + BuilderHelper.QUALIFIER_KEY + "=" + QUAL1;
    
    /**
     * A bunch of tests for tokenized strings
     */
    @Test
    public void testTokenizedString1() {
        Descriptor d1 = BuilderHelper.link(ACME_IMPL).to(ACME_INTF).named(NAME1).qualifiedBy(QUAL1).build();
        
        {
            IndexedFilter f1 = BuilderHelper.createTokenizedFilter(TOKEN1);
            Assert.assertEquals(f1.getAdvertisedContract(),ACME_INTF);
            Assert.assertNull(f1.getName());
            Assert.assertTrue(f1.matches(d1));
            Assert.assertTrue(f1.toString().contains(ACME_INTF));
        }
        
        {
            IndexedFilter f2 = BuilderHelper.createTokenizedFilter(TOKEN2);
            Assert.assertEquals(f2.getAdvertisedContract(),ACME_INTF);
            Assert.assertEquals(NAME1, f2.getName());
            Assert.assertTrue(f2.matches(d1));
            Assert.assertTrue(f2.toString().contains(ACME_INTF));
            Assert.assertTrue(f2.toString().contains(NAME1));
        }
        
        {
            IndexedFilter f3 = BuilderHelper.createTokenizedFilter(TOKEN3);
            Assert.assertEquals(f3.getAdvertisedContract(),ACME_INTF);
            Assert.assertNull(f3.getName());
            Assert.assertFalse(f3.matches(d1));
            Assert.assertTrue(f3.toString().contains(ACME_INTF));
            Assert.assertTrue(f3.toString().contains(QUAL2));
        }
        
        {
            IndexedFilter f4 = BuilderHelper.createTokenizedFilter(TOKEN4);
            Assert.assertEquals(f4.getAdvertisedContract(),ACME_INTF);
            Assert.assertEquals(NAME1, f4.getName());
            Assert.assertFalse(f4.matches(d1));
            Assert.assertTrue(f4.toString().contains(ACME_INTF));
            Assert.assertTrue(f4.toString().contains(QUAL1));
            Assert.assertTrue(f4.toString().contains(QUAL2));
            Assert.assertTrue(f4.toString().contains(NAME1));
        }
        
        {
            IndexedFilter f5 = BuilderHelper.createTokenizedFilter(TOKEN5);
            Assert.assertNull(f5.getAdvertisedContract());
            Assert.assertNull(f5.getName());
            Assert.assertTrue(f5.matches(d1));
            Assert.assertTrue(f5.toString().contains(QUAL1));
        }
        
        
    }
    
    private final static String FOO = "foo";
    private final static String BAR = "bar";
    
    @Test
    public void testCreateServiceHandle() {
        ServiceHandle<String> handle = BuilderHelper.createConstantServiceHandle(FOO);
        Assert.assertNotNull(handle);
        
        Assert.assertEquals(FOO, handle.getService());
        Assert.assertNull(handle.getActiveDescriptor());
        Assert.assertNull(handle.getServiceData());
        
        handle.setServiceData(BAR);
        Assert.assertEquals(BAR, handle.getServiceData());
        
        ServiceHandle<Object> nullMe = BuilderHelper.createConstantServiceHandle(null);
        Assert.assertNotNull(nullMe);
        
        Assert.assertNull(nullMe.getService());
        Assert.assertNull(nullMe.getActiveDescriptor());
        Assert.assertNull(nullMe.getServiceData());
        
        nullMe.setServiceData(FOO);
        Assert.assertEquals(FOO, nullMe.getServiceData());
        
        nullMe.setServiceData(null);
        Assert.assertNull(nullMe.getServiceData());
    }
    
    /**
     * Tests scope and qualifier annotations that have array types
     * with the Metadata annotation
     */
    @Test // @org.junit.Ignore
    public void testArrayMetadata() {
        ArrayMetadataService ams = new ArrayMetadataService();
        
        AbstractActiveDescriptor<ArrayMetadataService> desc = BuilderHelper.createConstantDescriptor(ams);
        
        Map<String, List<String>> metadata = desc.getMetadata();
        
        {
            List<String> value = metadata.get(ArrayMetadataScope.STRING_KEY);
            Assert.assertNotNull(value);
            
            Assert.assertEquals(2, value.size());
            Assert.assertEquals("a", value.get(0));
            Assert.assertEquals("b", value.get(1));
        }
        
        {
            List<String> value = metadata.get(ArrayMetadataScope.BYTE_KEY);
            Assert.assertNotNull(value);
            
            Assert.assertEquals(2, value.size());
            Assert.assertEquals("1", value.get(0));
            Assert.assertEquals("2", value.get(1));
        }
        
        {
            List<String> value = metadata.get(ArrayMetadataScope.SHORT_KEY);
            Assert.assertNotNull(value);
            
            Assert.assertEquals(2, value.size());
            Assert.assertEquals("3", value.get(0));
            Assert.assertEquals("4", value.get(1));
        }
        
        {
            List<String> value = metadata.get(ArrayMetadataScope.INT_KEY);
            Assert.assertNotNull(value);
            
            Assert.assertEquals(2, value.size());
            Assert.assertEquals("5", value.get(0));
            Assert.assertEquals("6", value.get(1));
        }
        
        {
            List<String> value = metadata.get(ArrayMetadataScope.CHAR_KEY);
            Assert.assertNotNull(value);
            
            Assert.assertEquals(2, value.size());
            Assert.assertEquals("c", value.get(0));
            Assert.assertEquals("d", value.get(1));
        }
        
        {
            List<String> value = metadata.get(ArrayMetadataScope.LONG_KEY);
            Assert.assertNotNull(value);
            
            Assert.assertEquals(2, value.size());
            Assert.assertEquals("7", value.get(0));
            Assert.assertEquals("8", value.get(1));
        }
        
        {
            List<String> value = metadata.get(ArrayMetadataScope.CLASS_KEY);
            Assert.assertNotNull(value);
            
            Assert.assertEquals(2, value.size());
            Assert.assertEquals(ArrayMetadataScope.class.getName(), value.get(0));
            Assert.assertEquals(ArrayMetadataQualifier.class.getName(), value.get(1));
        }
        
        {
            List<String> value = metadata.get(ArrayMetadataScope.FLOAT_KEY);
            Assert.assertNotNull(value);
            
            Assert.assertEquals(2, value.size());
            Assert.assertEquals("9.0", value.get(0));
            Assert.assertEquals("10.0", value.get(1));
        }
        
        {
            List<String> value = metadata.get(ArrayMetadataScope.DOUBLE_KEY);
            Assert.assertNotNull(value);
            
            Assert.assertEquals(2, value.size());
            Assert.assertEquals("11.0", value.get(0));
            Assert.assertEquals("12.0", value.get(1));
        }
        
        {
            List<String> value = metadata.get(ArrayMetadataQualifier.STRING_KEY);
            Assert.assertNotNull(value);
            
            Assert.assertEquals(2, value.size());
            Assert.assertEquals("e", value.get(0));
            Assert.assertEquals("f", value.get(1));
        }
        
        {
            List<String> value = metadata.get(ArrayMetadataQualifier.BYTE_KEY);
            Assert.assertNotNull(value);
            
            Assert.assertEquals(2, value.size());
            Assert.assertEquals("13", value.get(0));
            Assert.assertEquals("14", value.get(1));
        }
        
        {
            List<String> value = metadata.get(ArrayMetadataQualifier.SHORT_KEY);
            Assert.assertNotNull(value);
            
            Assert.assertEquals(2, value.size());
            Assert.assertEquals("15", value.get(0));
            Assert.assertEquals("16", value.get(1));
        }
        
        {
            List<String> value = metadata.get(ArrayMetadataQualifier.INT_KEY);
            Assert.assertNotNull(value);
            
            Assert.assertEquals(2, value.size());
            Assert.assertEquals("17", value.get(0));
            Assert.assertEquals("18", value.get(1));
        }
        
        {
            List<String> value = metadata.get(ArrayMetadataQualifier.CHAR_KEY);
            Assert.assertNotNull(value);
            
            Assert.assertEquals(2, value.size());
            Assert.assertEquals("g", value.get(0));
            Assert.assertEquals("h", value.get(1));
        }
        
        {
            List<String> value = metadata.get(ArrayMetadataQualifier.LONG_KEY);
            Assert.assertNotNull(value);
            
            Assert.assertEquals(2, value.size());
            Assert.assertEquals("19", value.get(0));
            Assert.assertEquals("20", value.get(1));
        }
        
        {
            List<String> value = metadata.get(ArrayMetadataQualifier.CLASS_KEY);
            Assert.assertNotNull(value);
            
            Assert.assertEquals(2, value.size());
            Assert.assertEquals(Blue.class.getName(), value.get(0));
            Assert.assertEquals(Green.class.getName(), value.get(1));
        }
        
        {
            List<String> value = metadata.get(ArrayMetadataQualifier.FLOAT_KEY);
            Assert.assertNotNull(value);
            
            Assert.assertEquals(2, value.size());
            Assert.assertEquals("21.0", value.get(0));
            Assert.assertEquals("22.0", value.get(1));
        }
        
        {
            List<String> value = metadata.get(ArrayMetadataQualifier.DOUBLE_KEY);
            Assert.assertNotNull(value);
            
            Assert.assertEquals(2, value.size());
            Assert.assertEquals("23.0", value.get(0));
            Assert.assertEquals("24.0", value.get(1));
        }
        
    }
    
    /**
     * This modifies the incoming DescriptorImpl after the filter was created to ensure a copy was made
     */
    @Test
    public void testCreateDescriptorImplFilter() {
        DescriptorImpl di = BuilderHelper.link("an.implementation.Thing").to("a.contract.Thing").qualifiedBy("a.qualifier.Thing").build();
        
        IndexedFilter filter = BuilderHelper.createDescriptorFilter(di);
        Assert.assertTrue(BuilderHelper.filterMatches(di, filter));
        
        di.addQualifier("another.qualifier.Thing");
        Assert.assertFalse(BuilderHelper.filterMatches(di, filter));
    }
    
    /**
     * This modifies the incoming DescriptorImpl after the filter was created to ensure a copy was made
     */
    @Test
    public void testCreateDescriptorImplFilterNoCopy() {
        DescriptorImpl di1 = BuilderHelper.link("an.implementation.Thing1").to("a.contract.Thing1").qualifiedBy("a.qualifier.Thing1").build();
        DescriptorImpl di2 = BuilderHelper.link("an.implementation.Thing2").to("a.contract.Thing2").qualifiedBy("a.qualifier.Thing2").build();
        
        IndexedFilter filter = BuilderHelper.createDescriptorFilter(di1, false);
        Assert.assertTrue(BuilderHelper.filterMatches(di1, filter));
        Assert.assertFalse(BuilderHelper.filterMatches(di2, filter));
        
        di1.addQualifier("another.qualifier.Thing");
        Assert.assertTrue(BuilderHelper.filterMatches(di1, filter));
        Assert.assertFalse(BuilderHelper.filterMatches(di2, filter));
    }
    
    /**
     * Tests that the Rank annotation can be in the superclass
     */
    @Test
    public void testRankAnnotationCanBeOnSuperclass() {
        DescriptorImpl di = BuilderHelper.createDescriptorFromClass(ClassWithRankInSuperclass.class);
        Assert.assertEquals(RANK_IN_SUPERCLASS, di.getRanking());
    }
}
