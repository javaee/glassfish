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
package org.glassfish.hk2.tests.locator.initialization;

import java.util.SortedSet;

import junit.framework.Assert;

import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.Descriptor;
import org.glassfish.hk2.api.DescriptorFilter;
import org.glassfish.hk2.api.Filter;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.api.ServiceLocatorFactory;
import org.glassfish.hk2.utilities.BuilderHelper;
import org.junit.Before;
import org.junit.Test;

/**
 * @author jwells
 *
 */
public class InitializationTest {
    public final static String TEST_CLASS_A = "this.thing.isnt.actually.There";
    public final static String TEST_CLASS_B = "this.thing.isnt.added.in.the.Module";
    public final static String SIMPLE_NAME = "InitializationTest";
    
    private final static DescriptorFilter aFilter = BuilderHelper.link(InitializationTest.TEST_CLASS_A).build();
    private final static DescriptorFilter bFilter = BuilderHelper.link(InitializationTest.TEST_CLASS_B).build();
    private final static DescriptorFilter namedFilter = BuilderHelper.link().named(SIMPLE_NAME).build();
    
    private ServiceLocator locator;
    
    @Before
    public void before() {
        locator = ServiceLocatorFactory.getInstance().create(SIMPLE_NAME, new InitializationModule());
        if (locator == null) {
            locator = ServiceLocatorFactory.getInstance().find(SIMPLE_NAME);   
        }
    }
    
    @Test
    public void testGetName() {
        Assert.assertEquals(SIMPLE_NAME, locator.getName());
    }
    
    @Test
    public void testFindDescriptors() {
        SortedSet<ActiveDescriptor<?>> descriptors = locator.getDescriptors(aFilter);
        Assert.assertNotNull(descriptors);
        Assert.assertTrue(descriptors.size() == 2);
        
        long bestId = -1L;
        long lastId = -1L;
        for (Descriptor d : descriptors) {
            Assert.assertEquals(TEST_CLASS_A, d.getImplementation());
            
            long id = d.getServiceId().longValue();
            
            if (bestId < 0L) {
                bestId = id;
            }
            
            Assert.assertTrue("lastId=" + lastId + " currentId=" + id, lastId < id);
            lastId = id;
        }
        
        Descriptor bestDescriptor = locator.getBestDescriptor(aFilter);
        Assert.assertNotNull(bestDescriptor);
        
        Assert.assertEquals(bestId, bestDescriptor.getServiceId().longValue());
    }
    
    @Test
    public void testDidNotFindDescriptors() {
        SortedSet<ActiveDescriptor<?>> descriptors = locator.getDescriptors(bFilter);
        Assert.assertNotNull(descriptors);
        Assert.assertTrue(descriptors.size() == 0);
        
        Assert.assertNull(locator.getBestDescriptor(bFilter));
    }
    
    @Test
    public void testNoMatchFilter() {
        SortedSet<ActiveDescriptor<?>> descriptors = locator.getDescriptors(new Filter<Descriptor>() {

            @Override
            public boolean matches(Descriptor d) {
                // This silly filter matches nothing!
                return false;
            }
        });
        
        Assert.assertNotNull(descriptors);
        Assert.assertTrue(descriptors.size() == 0);
        
        Assert.assertNull(locator.getBestDescriptor(bFilter));
    }
    
    @Test
    public void testLookupByName() {
        SortedSet<ActiveDescriptor<?>> descriptors = locator.getDescriptors(namedFilter);
        
        Assert.assertNotNull(descriptors);
        Assert.assertTrue("Expecting 1 descriptor, found " + descriptors.size(), descriptors.size() == 1);
        
        for (Descriptor d : descriptors) {
            Assert.assertEquals(SIMPLE_NAME, d.getName());
        }
        
        Descriptor d = locator.getBestDescriptor(namedFilter);
        Assert.assertNotNull(d);
        
        Assert.assertEquals(SIMPLE_NAME, d.getName());
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testBadFilter() {
        locator.getDescriptors(null);
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testBadBestFilter() {
        locator.getBestDescriptor(null);
    }

}
