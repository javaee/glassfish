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

package org.glassfish.hk2.tests.api;

import java.lang.annotation.Annotation;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Named;
import javax.inject.Singleton;

import junit.framework.Assert;

import org.glassfish.hk2.api.Descriptor;
import org.glassfish.hk2.api.DescriptorType;
import org.glassfish.hk2.api.DescriptorVisibility;
import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.api.FactoryDescriptors;
import org.glassfish.hk2.api.HK2Loader;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.hk2.utilities.BuilderHelper;
import org.glassfish.hk2.utilities.DescriptorImpl;
import org.junit.Test;

/**
 * @author jwells
 *
 */
public class DescriptorBuilderTest {
    private final static String FACTORY_CLASS_NAME = "my.factory.Factory";
    private final static String NAME = "name";
    private final static String CONTRACT_NAME = "my.factory.Contract";
    private final static int MY_RANK = 13;
    private final static String KEY = "key";
    private final static String VALUE = "value";
    
    /**
     * Tests factory production
     */
    @Test
    public void testBuildFactoryNoArg() {
        BlueImpl blue = new BlueImpl();
        HK2LoaderImpl loader = new HK2LoaderImpl();
        
        FactoryDescriptors fds = BuilderHelper.link(FACTORY_CLASS_NAME).
            named(NAME).
            to(CONTRACT_NAME).
            in(Singleton.class).
            qualifiedBy(blue).
            ofRank(MY_RANK).
            andLoadWith(loader).
            has(KEY, VALUE).
            proxy(false).
            localOnly().
            buildFactory();
        
        {
            // Now ensure the resulting descriptors have the expected results
            Descriptor asService = fds.getFactoryAsAService();
        
            // The javadoc says the return from buildFactory will have DescriptorImpl
            Assert.assertTrue(asService instanceof DescriptorImpl);
        
            Assert.assertEquals(DescriptorType.CLASS, asService.getDescriptorType());
            Assert.assertEquals(DescriptorVisibility.NORMAL, asService.getDescriptorVisibility());
            Assert.assertEquals(FACTORY_CLASS_NAME, asService.getImplementation());
            Assert.assertEquals(PerLookup.class.getName(), asService.getScope());
            Assert.assertEquals(MY_RANK, asService.getRanking());
            Assert.assertNull(asService.getName());
            Assert.assertTrue(asService.getQualifiers().isEmpty());
            Assert.assertTrue(asService.getMetadata().isEmpty());
            Assert.assertEquals(loader, asService.getLoader());
            Assert.assertNull(asService.isProxiable());
        
            Set<String> serviceContracts = asService.getAdvertisedContracts();
            Assert.assertEquals(2, serviceContracts.size());
            Assert.assertTrue(serviceContracts.contains(FACTORY_CLASS_NAME));
            Assert.assertTrue(serviceContracts.contains(Factory.class.getName()));
        }
        
        {
            // Now ensure the resulting descriptors have the expected results
            Descriptor asFactory = fds.getFactoryAsAFactory();
        
            // The javadoc says the return from buildFactory will have DescriptorImpl
            Assert.assertTrue(asFactory instanceof DescriptorImpl);
        
            Assert.assertEquals(DescriptorType.PROVIDE_METHOD, asFactory.getDescriptorType());
            Assert.assertEquals(DescriptorVisibility.LOCAL, asFactory.getDescriptorVisibility());
            Assert.assertEquals(FACTORY_CLASS_NAME, asFactory.getImplementation());
            Assert.assertEquals(Singleton.class.getName(), asFactory.getScope());
            Assert.assertEquals(MY_RANK, asFactory.getRanking());
            Assert.assertEquals(NAME, asFactory.getName());
            Assert.assertEquals(loader, asFactory.getLoader());
            Assert.assertEquals(false, asFactory.isProxiable().booleanValue());
            
            Set<String> qualifiers = asFactory.getQualifiers();
            Assert.assertEquals(2, qualifiers.size());
            Assert.assertTrue(qualifiers.contains(Blue.class.getName()));
            Assert.assertTrue(qualifiers.contains(Named.class.getName()));
            
            Map<String, List<String>> metadata = asFactory.getMetadata();
            Assert.assertEquals(1, metadata.size());
            Assert.assertTrue(metadata.containsKey(KEY));
            
            List<String> values = metadata.get(KEY);
            Assert.assertEquals(1, values.size());
            Assert.assertEquals(VALUE, values.get(0));
        
            Set<String> serviceContracts = asFactory.getAdvertisedContracts();
            Assert.assertEquals(1, serviceContracts.size());
            Assert.assertTrue(serviceContracts.contains(CONTRACT_NAME));
        }
        
        Assert.assertTrue(fds.toString().contains("descriptorType=PROVIDE_METHOD"));
        Assert.assertTrue(fds.toString().contains("descriptorType=CLASS"));
    }
    
    /**
     * Tests factory production
     */
    @Test
    public void testImplNotAddedToContract() {
        DescriptorImpl desc = BuilderHelper.link(FACTORY_CLASS_NAME, false).
            to(CONTRACT_NAME).
            build();
        
        Assert.assertEquals(DescriptorType.CLASS, desc.getDescriptorType());
        Assert.assertEquals(FACTORY_CLASS_NAME, desc.getImplementation());
        Assert.assertNull(desc.getScope());
        Assert.assertEquals(0, desc.getRanking());
        Assert.assertNull(desc.getName());
        Assert.assertTrue(desc.getQualifiers().isEmpty());
        Assert.assertTrue(desc.getMetadata().isEmpty());
        Assert.assertNull(desc.getLoader());
        
        Set<String> serviceContracts = desc.getAdvertisedContracts();
        Assert.assertEquals(1, serviceContracts.size());
        Assert.assertTrue(serviceContracts.contains(CONTRACT_NAME));
    }
    
    /**
     * Tests double name
     */
    @Test(expected=IllegalArgumentException.class)
    public void testNotNamedTwice() {
        BuilderHelper.link(FACTORY_CLASS_NAME).
          named(NAME).
          named(NAME);
    }
    
    /**
     * Tests double loader
     */
    @Test(expected=IllegalArgumentException.class)
    public void testNotLoadedTwice() {
        HK2Loader loader = new HK2LoaderImpl();
        
        BuilderHelper.link(FACTORY_CLASS_NAME).
          andLoadWith(loader).
          andLoadWith(loader);
    }
    
    /**
     * Tests illegal null contract
     */
    @Test(expected=IllegalArgumentException.class)
    public void testIllegalClassContract() {
        BuilderHelper.link(FACTORY_CLASS_NAME).
          to((Class<?>) null);
    }
    
    /**
     * Tests illegal null contract
     */
    @Test(expected=IllegalArgumentException.class)
    public void testIllegalStringContract() {
        BuilderHelper.link(FACTORY_CLASS_NAME).
          to((String) null);
    }
    
    /**
     * Tests illegal null scope
     */
    @Test(expected=IllegalArgumentException.class)
    public void testIllegalClassScope() {
        BuilderHelper.link(FACTORY_CLASS_NAME).
          in((Class<? extends Annotation>) null);
    }
    
    /**
     * Tests illegal null scope
     */
    @Test(expected=IllegalArgumentException.class)
    public void testIllegalStringScope() {
        BuilderHelper.link(FACTORY_CLASS_NAME).
          in((String) null);
    }
    
    /**
     * Tests illegal null qualifier
     */
    @Test(expected=IllegalArgumentException.class)
    public void testIllegalClassQualifier() {
        BuilderHelper.link(FACTORY_CLASS_NAME).
          qualifiedBy((Annotation) null);
    }
    
    /**
     * Tests illegal null qualifier
     */
    @Test(expected=IllegalArgumentException.class)
    public void testIllegalStringQualifier() {
        BuilderHelper.link(FACTORY_CLASS_NAME).
          qualifiedBy((String) null);
    }
    
    /**
     * Tests illegal null key
     */
    @Test(expected=IllegalArgumentException.class)
    public void testIllegalMetadataKey() {
        BuilderHelper.link(FACTORY_CLASS_NAME).
          has(null, VALUE);
    }
    
    /**
     * Tests illegal null value
     */
    @Test(expected=IllegalArgumentException.class)
    public void testIllegalMetadataValue() {
        BuilderHelper.link(FACTORY_CLASS_NAME).
          has(KEY, (String) null);
    }
    
    /**
     * Tests illegal null list key
     */
    @Test(expected=IllegalArgumentException.class)
    public void testIllegalMetadataListKey() {
        LinkedList<String> values = new LinkedList<String>();
        values.add(VALUE);
        
        BuilderHelper.link(FACTORY_CLASS_NAME).
          has(null, values);
    }
    
    /**
     * Tests illegal null value
     */
    @Test(expected=IllegalArgumentException.class)
    public void testIllegalMetadataListNullValue() {
        BuilderHelper.link(FACTORY_CLASS_NAME).
          has(KEY, (List<String>) null);
    }
    
    /**
     * Tests illegal empty value list
     */
    @Test(expected=IllegalArgumentException.class)
    public void testIllegalMetadataListEmptyValue() {
        BuilderHelper.link(FACTORY_CLASS_NAME).
          has(KEY, new LinkedList<String>());
    }

}
