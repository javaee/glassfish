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

package org.glassfish.hk2.tests.locator.alias;


import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.Descriptor;
import org.glassfish.hk2.api.DynamicConfiguration;
import org.glassfish.hk2.api.DynamicConfigurationService;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.api.ServiceLocatorFactory;
import org.glassfish.hk2.utilities.AliasDescriptor;
import org.glassfish.hk2.utilities.BuilderHelper;
import org.glassfish.hk2.utilities.NamedImpl;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import org.junit.Assert;
import org.junit.Test;

import javax.inject.Named;
import javax.inject.Singleton;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;


/**
 * Tests for the AliasDescriptor.
 *
 * @author tbeerbower
 */
public class AliasDescriptorTest {

    /**
     * Tests adding alias descriptors to the system
     * 
     * @throws Exception
     */
    @Test
    public void testAddAliasDescriptors() throws Exception {
        ServiceLocator locator = ServiceLocatorFactory.getInstance().create("testAddDescriptor");
        DynamicConfigurationService dcs = locator.getService(DynamicConfigurationService.class);
        DynamicConfiguration config = dcs.createDynamicConfiguration();

        ActiveDescriptor<MyService> descriptor =
                (ActiveDescriptor<MyService>) config.<MyService>bind(BuilderHelper.link(MyService.class).
                        to(MyInterface1.class).in(Singleton.class.getName()).build());

        config.commit();


        MyService s1 = locator.getService(MyService.class);
        assertNotNull(s1);

        assertEquals(s1, locator.getService(MyInterface1.class));

        config = dcs.createDynamicConfiguration();
        config.addActiveDescriptor(new AliasDescriptor<MyService>(locator, descriptor, MyInterface2.class.getName(), "foo"));
        config.addActiveDescriptor(new AliasDescriptor<MyService>(locator, descriptor, MyInterface3.class.getName(), "bar"));
        config.commit();

        MyInterface2 s2 = locator.getService(MyInterface2.class, "foo");

        assertEquals(s1, s2);

        MyInterface3 s3 = locator.getService(MyInterface3.class, "bar");

        assertEquals(s1, s3);

        assertNull(locator.getService(MyInterface1.class, "foo"));
        assertNull(locator.getService(MyInterface1.class, "bar"));

        assertNull(locator.getService(MyInterface2.class, ""));
        assertNull(locator.getService(MyInterface2.class, "bar"));

        assertNull(locator.getService(MyInterface3.class, ""));
        assertNull(locator.getService(MyInterface3.class, "foo"));
    }

    @Test
    public void testIsReified() throws Exception {
        AliasDescriptor<MyService> aliasDescriptor = getAliasDescriptor("testIsReified", "foo");

        assertTrue(aliasDescriptor.isReified());
    }

    @Test
    public void testGetImplementation() throws Exception {
        AliasDescriptor<MyService> aliasDescriptor = getAliasDescriptor("testGetImplementation", "foo");

        assertEquals(MyService.class.getName(), aliasDescriptor.getImplementation());
    }

    @Test
    public void testGetContractTypes() throws Exception {
        ServiceLocator locator = ServiceLocatorFactory.getInstance().create("testGetContractTypes");
        DynamicConfigurationService dcs = locator.getService(DynamicConfigurationService.class);
        DynamicConfiguration config = dcs.createDynamicConfiguration();

        ActiveDescriptor<MyService> descriptor =
                (ActiveDescriptor<MyService>)config.<MyService>bind(BuilderHelper.link(MyService.class).
                        to(MyInterface1.class).in(Singleton.class.getName()).build());

        config.commit();

        AliasDescriptor<MyService> aliasDescriptor =
                new AliasDescriptor<MyService>(locator, descriptor, MyInterface2.class.getName(), "foo");

        Set<Type> contractTypes = aliasDescriptor.getContractTypes();

        assertSame(1, contractTypes.size());
        assertSame(MyInterface2.class, contractTypes.iterator().next());
    }

    @Test
    public void testGetScopeAnnotation() throws Exception {
        AliasDescriptor<MyService> aliasDescriptor = getAliasDescriptor("testGetScopeAnnotation", "foo");

        assertEquals(Singleton.class, aliasDescriptor.getScopeAnnotation());
    }

    @Test
    public void testGetQualifierAnnotations() throws Exception {
        AliasDescriptor<MyService> aliasDescriptor = getAliasDescriptor("testGetQualifierAnnotations", "foo");

        final Set<Annotation> qualifierAnnotations = aliasDescriptor.getQualifierAnnotations();

        boolean foundNamed = false;
        boolean foundQ1 = false;
        assertSame(2, qualifierAnnotations.size());
        for (Annotation anno : qualifierAnnotations) {
            if (Named.class.equals(anno.annotationType())) {
                Named named = (Named) anno;
                
                assertSame("foo", named.value());
                
                foundNamed = true;
            }
            else if (Qualifier1.class.equals(anno.annotationType())) {
                foundQ1 = true;
            }
            else {
                Assert.fail("Unknown annotation found: " + anno);
            }
        }
        
        Assert.assertTrue(foundNamed);
        Assert.assertTrue(foundQ1);
        
        // Also check the String version
        Set<String> qualifierTypes = aliasDescriptor.getQualifiers();
        
        foundNamed = false;
        foundQ1 = false;
        assertSame(2, qualifierTypes.size());
        for (String anno : qualifierTypes) {
            if (Named.class.getName().equals(anno)) {
                foundNamed = true;
            }
            else if (Qualifier1.class.getName().equals(anno)) {
                foundQ1 = true;
            }
            else {
                Assert.fail("Unknown annotation found: " + anno);
            }
        }
        
        Assert.assertTrue(foundNamed);
        Assert.assertTrue(foundQ1);
    }

    @Test
    public void testGetImplementationClass() throws Exception {
        AliasDescriptor<MyService> aliasDescriptor = getAliasDescriptor("testGetImplementationClass", "foo");

        ActiveDescriptor<MyService> descriptor = aliasDescriptor.getDescriptor();
        assertFalse(descriptor.isReified());

        assertEquals(MyService.class, aliasDescriptor.getImplementationClass());

        assertTrue(descriptor.isReified());
    }

    @Test
    public void testCreate() throws Exception {
        ServiceLocator locator = ServiceLocatorFactory.getInstance().create("testCreate");
        DynamicConfigurationService dcs = locator.getService(DynamicConfigurationService.class);
        DynamicConfiguration config = dcs.createDynamicConfiguration();

        ActiveDescriptor<MyService> descriptor =
                (ActiveDescriptor<MyService>)config.<MyService>bind(BuilderHelper.link(MyService.class).
                        to(MyInterface1.class).in(Singleton.class.getName()).build());

        AliasDescriptor<MyService> aliasDescriptor =
                new AliasDescriptor<MyService>(locator, descriptor, MyInterface2.class.getName(), "foo");

        config = dcs.createDynamicConfiguration();
        config.addActiveDescriptor(aliasDescriptor);
        config.commit();

        MyService s1 = locator.getService(descriptor, null);
        MyService s2 = aliasDescriptor.create(null);
        assertSame(s1, s2);
    }
    
    /**
     * Tests that hashCode and equals is working
     */
    @Test
    public void testHashCodeEquals() {
        ServiceLocator locator = ServiceLocatorFactory.getInstance().create("testHashCodeEquals");
        DynamicConfigurationService dcs = locator.getService(DynamicConfigurationService.class);
        DynamicConfiguration config = dcs.createDynamicConfiguration();

        ActiveDescriptor<MyService> descriptor1 =
                (ActiveDescriptor<MyService>)config.<MyService>bind(BuilderHelper.link(MyService.class).
                        to(MyInterface1.class).in(Singleton.class.getName()).
                        qualifiedBy(Qualifier1.class.getName()).build());
        
        ActiveDescriptor<MyService> descriptor2 =
                (ActiveDescriptor<MyService>)config.<MyService>bind(BuilderHelper.link(MyService.class).
                        to(MyInterface1.class).in(Singleton.class.getName()).
                        qualifiedBy(Qualifier1.class.getName()).build());

        config.commit();
        
        AliasDescriptor<MyService> a1 = new AliasDescriptor<MyService>(locator, descriptor1, MyInterface2.class.getName(), "foo");
        AliasDescriptor<MyService> a2 = new AliasDescriptor<MyService>(locator, descriptor1, MyInterface2.class.getName(), "foo");
        AliasDescriptor<MyService> a3 = new AliasDescriptor<MyService>(locator, descriptor1, MyInterface3.class.getName(), "foo");
        AliasDescriptor<MyService> a4 = new AliasDescriptor<MyService>(locator, descriptor1, MyInterface2.class.getName(), "bar");
        AliasDescriptor<MyService> a5 = new AliasDescriptor<MyService>(locator, descriptor2, MyInterface2.class.getName(), "foo");
        
        Assert.assertEquals(a1, a2);
        Assert.assertEquals(a2, a1);
        Assert.assertEquals(a1.hashCode(), a2.hashCode());
        Assert.assertEquals(a3, a3);
        
        Assert.assertNotSame(a1, a3);  // Contract different
        Assert.assertNotSame(a2, a3);  // Contract different
        
        Assert.assertNotSame(a1, a4);  // Name different
        Assert.assertNotSame(a2, a4);  // Name different
        
        Assert.assertNotSame(a1, a5);  // Underlying descriptor different
        Assert.assertNotSame(a2, a5);  // Underlying descriptor different
    }
    
    /**
     * Tests removing alias descriptors to the system
     * 
     * @throws Exception
     */
    @Test
    public void testRemoveAliasDescriptors() throws Exception {
        ServiceLocator locator = ServiceLocatorFactory.getInstance().create("testRemoveAliasDescriptors");
        DynamicConfigurationService dcs = locator.getService(DynamicConfigurationService.class);
        DynamicConfiguration config = dcs.createDynamicConfiguration();

        ActiveDescriptor<MyService> descriptor =
                (ActiveDescriptor<MyService>) config.<MyService>bind(BuilderHelper.link(MyService.class).
                        to(MyInterface1.class).in(Singleton.class.getName()).build());

        config.commit();

        config = dcs.createDynamicConfiguration();
        config.addActiveDescriptor(new AliasDescriptor<MyService>(locator, descriptor, MyInterface2.class.getName(), "foo"));
        config.addActiveDescriptor(new AliasDescriptor<MyService>(locator, descriptor, MyInterface3.class.getName(), "bar"));
        config.commit();

        {
            MyInterface2 s2 = locator.getService(MyInterface2.class, "foo");
            Assert.assertNotNull(s2);

            MyInterface3 s3 = locator.getService(MyInterface3.class, "bar");
            Assert.assertNotNull(s3);
        }

        // Should remove the alias descriptors as well
        ServiceLocatorUtilities.removeOneDescriptor(locator, descriptor, true);
        
        {
            MyInterface2 s2 = locator.getService(MyInterface2.class, "foo");
            Assert.assertNull(s2);

            MyInterface3 s3 = locator.getService(MyInterface3.class, "bar");
            Assert.assertNull(s3);
        }
    }
    
    /**
     * Tests removing alias descriptors to the system (from the original descriptor)
     * 
     * @throws Exception
     */
    @Test
    public void testRemoveAliasDescriptorsOriginalDescriptor() throws Exception {
        ServiceLocator locator = ServiceLocatorFactory.getInstance().create("testRemoveAliasDescriptorsOriginal");
        DynamicConfigurationService dcs = locator.getService(DynamicConfigurationService.class);
        DynamicConfiguration config = dcs.createDynamicConfiguration();

        Descriptor originalDescriptor = BuilderHelper.link(MyService.class).
                to(MyInterface1.class).in(Singleton.class.getName()).build();
        
        ActiveDescriptor<MyService> descriptor =
                (ActiveDescriptor<MyService>) config.<MyService>bind(originalDescriptor);

        config.commit();

        config = dcs.createDynamicConfiguration();
        config.addActiveDescriptor(new AliasDescriptor<MyService>(locator, descriptor, MyInterface2.class.getName(), "foo"));
        config.addActiveDescriptor(new AliasDescriptor<MyService>(locator, descriptor, MyInterface3.class.getName(), "bar"));
        config.commit();

        {
            MyInterface2 s2 = locator.getService(MyInterface2.class, "foo");
            Assert.assertNotNull(s2);

            MyInterface3 s3 = locator.getService(MyInterface3.class, "bar");
            Assert.assertNotNull(s3);
        }

        // Should remove the alias descriptors as well
        ServiceLocatorUtilities.removeOneDescriptor(locator, originalDescriptor, true);
        
        {
            MyInterface2 s2 = locator.getService(MyInterface2.class, "foo");
            Assert.assertNull(s2);

            MyInterface3 s3 = locator.getService(MyInterface3.class, "bar");
            Assert.assertNull(s3);
        }
    }
    
    /**
     * Tests removing alias descriptors to the system (via Filter)
     * 
     * @throws Exception
     */
    @Test
    public void testRemoveAliasDescriptorsWithFilterDescriptor() throws Exception {
        ServiceLocator locator = ServiceLocatorFactory.getInstance().create("testRemoveAliasDescriptorsOriginal");
        DynamicConfigurationService dcs = locator.getService(DynamicConfigurationService.class);
        DynamicConfiguration config = dcs.createDynamicConfiguration();

        Descriptor originalDescriptor = BuilderHelper.link(MyService.class).
                to(MyInterface1.class).in(Singleton.class.getName()).build();
        
        ActiveDescriptor<MyService> descriptor =
                (ActiveDescriptor<MyService>) config.<MyService>bind(originalDescriptor);

        config.commit();

        config = dcs.createDynamicConfiguration();
        config.addActiveDescriptor(new AliasDescriptor<MyService>(locator, descriptor, MyInterface2.class.getName(), "foo"));
        config.addActiveDescriptor(new AliasDescriptor<MyService>(locator, descriptor, MyInterface3.class.getName(), "bar"));
        config.commit();

        {
            MyInterface2 s2 = locator.getService(MyInterface2.class, "foo");
            Assert.assertNotNull(s2);

            MyInterface3 s3 = locator.getService(MyInterface3.class, "bar");
            Assert.assertNotNull(s3);
        }

        // Should remove the alias descriptors as well
        ServiceLocatorUtilities.removeFilter(locator, BuilderHelper.createContractFilter(MyInterface1.class.getName()), true);
        
        {
            MyInterface2 s2 = locator.getService(MyInterface2.class, "foo");
            Assert.assertNull(s2);

            MyInterface3 s3 = locator.getService(MyInterface3.class, "bar");
            Assert.assertNull(s3);
        }
    }


    // ----- Utility methods ------------------------------------------------

    private AliasDescriptor<MyService> getAliasDescriptor(String locatorName, String aliasName) {
        ServiceLocator locator = ServiceLocatorFactory.getInstance().create(locatorName);
        DynamicConfigurationService dcs = locator.getService(DynamicConfigurationService.class);
        DynamicConfiguration config = dcs.createDynamicConfiguration();

        ActiveDescriptor<MyService> descriptor =
                (ActiveDescriptor<MyService>)config.<MyService>bind(BuilderHelper.link(MyService.class).
                        to(MyInterface1.class).in(Singleton.class.getName()).
                        qualifiedBy(Qualifier1.class.getName()).build());

        config.commit();

        return new AliasDescriptor<MyService>(locator, descriptor, MyInterface2.class.getName(), aliasName);
    }
}
