/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2013-2015 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.hk2.tests.locator.dynamicpopulate;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;

import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.DescriptorFileFinder;
import org.glassfish.hk2.api.DynamicConfigurationService;
import org.glassfish.hk2.api.MultiException;
import org.glassfish.hk2.api.Populator;
import org.glassfish.hk2.api.PopulatorPostProcessor;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.tests.locator.utilities.LocatorHelper;
import org.glassfish.hk2.utilities.BuilderHelper;
import org.glassfish.hk2.utilities.DescriptorImpl;
import org.glassfish.hk2.utilities.DuplicatePostProcessor;
import org.glassfish.hk2.utilities.DuplicatePostProcessorMode;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author jwells
 *
 */
public class DynamicPopulateTest {
    private final static String TEST_NAME = "DynamicPopulateTest";
    private final static ServiceLocator locator = LocatorHelper.create(TEST_NAME, null);
    
    private final static String DUMMY_IMPL = "com.acme.dummy.Dummy";
    private final static String DUMMY_IMPL_2 = "com.acme.dummy.Dummy2";
    private final static String DUMMY_IMPL_3 = "com.acme.dummy.Dummy3";
    private final static String DUMMY_IMPL_4 = "com.acme.dummy.Dummy4";
    private final static String DUMMY_IMPL_5 = "com.acme.dummy.Dummy5";
    private final static String DUMMY_IMPL_6 = "com.acme.dummy.Dummy6";
    private final static String DUMMY_IMPL_7 = "com.acme.dummy.Dummy7";
    private final static String DUMMY_IMPL_8 = "com.acme.dummy.Dummy8";
    private final static String DUMMY_IMPL_9 = "com.acme.dummy.Dummy9";
    private final static String DUMMY_IMPL_10 = "com.acme.dummy.Dummy10";
    private final static String DUMMY_IMPL_11 = "com.acme.dummy.Dummy11";
    
    private final static String KEY = "key";
    private final static String VALUE = "value";
    private final static String VALUE2 = "value2";
    
    private final static String EXPECTED = "Expected exception from test";
    
    /**
     * Tests the most basic of populations
     * 
     * @throws IOException
     */
    @Test
    public void testBasicPopulation() throws IOException {
        DescriptorImpl di = new DescriptorImpl();
        di.setImplementation(DUMMY_IMPL);
        di.addAdvertisedContract(DUMMY_IMPL);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintWriter pw = new PrintWriter(baos);
        
        di.writeObject(pw);
        
        pw.close();
        
        DynamicConfigurationService dcs = locator.getService(DynamicConfigurationService.class);
        Assert.assertNotNull(dcs);
        
        Populator populator = dcs.getPopulator();
        
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        populator.populate(new MyDescriptorFinder(bais));
        
        ActiveDescriptor<?> ad = locator.getBestDescriptor(BuilderHelper.createContractFilter(DUMMY_IMPL));
        Assert.assertNotNull(ad);
        
        Assert.assertEquals(ad.getImplementation(), DUMMY_IMPL);
    }
    
    /**
     * Tests that we can add things to the metadata of the descriptor
     * 
     * @throws IOException
     */
    @Test
    public void testAddToMetadata() throws IOException {
        DescriptorImpl di = new DescriptorImpl();
        di.setImplementation(DUMMY_IMPL_2);
        di.addAdvertisedContract(DUMMY_IMPL_2);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintWriter pw = new PrintWriter(baos);
        
        di.writeObject(pw);
        
        pw.close();
        
        DynamicConfigurationService dcs = locator.getService(DynamicConfigurationService.class);
        Assert.assertNotNull(dcs);
        
        Populator populator = dcs.getPopulator();
        
        LinkedList<PopulatorPostProcessor> postProcessors = new LinkedList<PopulatorPostProcessor>();
        postProcessors.add(new MetadataPostProcessor(locator));
        
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        populator.populate(new MyDescriptorFinder(bais), postProcessors.toArray(new PopulatorPostProcessor[postProcessors.size()]));
        
        ActiveDescriptor<?> ad = locator.getBestDescriptor(BuilderHelper.createContractFilter(DUMMY_IMPL_2));
        Assert.assertNotNull(ad);
        
        Assert.assertEquals(ad.getImplementation(), DUMMY_IMPL_2);
        
        List<String> values = ad.getMetadata().get(KEY);
        Assert.assertNotNull(values);
        
        Assert.assertTrue(1 == values.size());
        
        Assert.assertEquals(VALUE, values.get(0));
    }
    
    /**
     * Tests that we can multiple post-processors and they
     * are run in order
     * 
     * @throws IOException
     */
    @Test
    public void testAddToMetadataInTwoPostProcessors() throws IOException {
        DescriptorImpl di = new DescriptorImpl();
        di.setImplementation(DUMMY_IMPL_7);
        di.addAdvertisedContract(DUMMY_IMPL_7);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintWriter pw = new PrintWriter(baos);
        
        di.writeObject(pw);
        
        pw.close();
        
        DynamicConfigurationService dcs = locator.getService(DynamicConfigurationService.class);
        Assert.assertNotNull(dcs);
        
        Populator populator = dcs.getPopulator();
        
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        populator.populate(new MyDescriptorFinder(bais), new MetadataPostProcessor(locator),
                new MetadataPostProcessor2());
        
        ActiveDescriptor<?> ad = locator.getBestDescriptor(BuilderHelper.createContractFilter(DUMMY_IMPL_7));
        Assert.assertNotNull(ad);
        
        Assert.assertEquals(ad.getImplementation(), DUMMY_IMPL_7);
        
        List<String> values = ad.getMetadata().get(KEY);
        Assert.assertNotNull(values);
        
        Assert.assertTrue(2 == values.size());
        
        Assert.assertEquals(VALUE, values.get(0));
        Assert.assertEquals(VALUE2, values.get(1));
    }
    
    /**
     * Tests that we can remove things using the post processors
     * 
     * @throws IOException
     */
    @Test
    public void testRemoveOne() throws IOException {
        DescriptorImpl di_3 = new DescriptorImpl();
        di_3.setImplementation(DUMMY_IMPL_3);
        di_3.addAdvertisedContract(DUMMY_IMPL_3);
        
        DescriptorImpl di_4 = new DescriptorImpl();
        di_4.setImplementation(DUMMY_IMPL_4);
        di_4.addAdvertisedContract(DUMMY_IMPL_4);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintWriter pw = new PrintWriter(baos);
        
        di_3.writeObject(pw);
        di_4.writeObject(pw);
        
        pw.close();
        
        DynamicConfigurationService dcs = locator.getService(DynamicConfigurationService.class);
        Assert.assertNotNull(dcs);
        
        Populator populator = dcs.getPopulator();
        
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        populator.populate(new MyDescriptorFinder(bais), new RemoveDI3PostProcessor());
        
        ActiveDescriptor<?> ad = locator.getBestDescriptor(BuilderHelper.createContractFilter(DUMMY_IMPL_3));
        Assert.assertNull(ad);
        
        ad = locator.getBestDescriptor(BuilderHelper.createContractFilter(DUMMY_IMPL_4));
        Assert.assertNotNull(ad);
        
        Assert.assertEquals(ad.getImplementation(), DUMMY_IMPL_4);
        
        List<String> values = ad.getMetadata().get(KEY);
        Assert.assertNull(values);
    }
    
    /**
     * Tests using a service as the descriptor finder
     * 
     * @throws IOException
     */
    @Test
    public void testNullDescriptorFinder() throws IOException {
        DescriptorImpl di = new DescriptorImpl();
        di.setImplementation(DUMMY_IMPL_5);
        di.addAdvertisedContract(DUMMY_IMPL_5);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintWriter pw = new PrintWriter(baos);
        
        di.writeObject(pw);
        
        pw.close();
        
        DynamicConfigurationService dcs = locator.getService(DynamicConfigurationService.class);
        Assert.assertNotNull(dcs);
        
        Populator populator = dcs.getPopulator();
        
        List<ActiveDescriptor<?>> populated = populator.populate(null);
        Assert.assertNotNull(populated);
        Assert.assertTrue(populated.isEmpty());
        
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ActiveDescriptor<?> descriptorFinderService = ServiceLocatorUtilities.addOneConstant(locator, new MyDescriptorFinder(bais));
        
        try {
            populated = populator.populate(null);
            Assert.assertNotNull(populated);
            Assert.assertTrue(1 == populated.size());
        
            ActiveDescriptor<?> ad = locator.getBestDescriptor(BuilderHelper.createContractFilter(DUMMY_IMPL_5));
            Assert.assertNotNull(ad);
        
            Assert.assertEquals(ad.getImplementation(), DUMMY_IMPL_5);
        
            Assert.assertEquals(ad, populated.get(0));
        }
        finally {
            // Clean up after test
            ServiceLocatorUtilities.removeOneDescriptor(locator, descriptorFinderService);
        }
    }
    
    /**
     * Tests what happens when user code in the post processor bombs
     * 
     * @throws IOException
     */
    @Test
    public void testPostProcessorThrowsException() throws IOException {
        DescriptorImpl di = new DescriptorImpl();
        di.setImplementation(DUMMY_IMPL_6);
        di.addAdvertisedContract(DUMMY_IMPL_6);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintWriter pw = new PrintWriter(baos);
        
        di.writeObject(pw);
        
        pw.close();
        
        DynamicConfigurationService dcs = locator.getService(DynamicConfigurationService.class);
        Assert.assertNotNull(dcs);
        
        Populator populator = dcs.getPopulator();
        
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        
        try {
            populator.populate(new MyDescriptorFinder(bais), new ThrowExceptionPostProcessor());
            Assert.fail("Should not have succeded, the post-processor threw");
        }
        catch (MultiException me) {
            boolean found = false;
            for (Throwable th : me.getErrors()) {
                if (th.getMessage().equals(EXPECTED)) {
                    found = true;
                    break;
                }
            }
            
            if (!found) throw me;
        }
    }
    
    /**
     * Tests what happens when user code in the descriptor finder bombs
     * 
     * @throws IOException
     */
    @Test
    public void testDescriptorFinderThrowsIOException() throws IOException {
        DynamicConfigurationService dcs = locator.getService(DynamicConfigurationService.class);
        Assert.assertNotNull(dcs);
        
        Populator populator = dcs.getPopulator();
        
        try {
            populator.populate(new ThrowIOExceptionFinder());
            Assert.fail("Should not have succeded, the descriptor finder threw");
        }
        catch (IOException ioe) {
            Assert.assertEquals(EXPECTED, ioe.getMessage());
        }
    }
    
    /**
     * Tests what happens when user code in the descriptor throws
     * some unexpected exception
     * 
     * @throws IOException
     */
    @Test
    public void testFinderThrowsOtherException() throws IOException {
        DynamicConfigurationService dcs = locator.getService(DynamicConfigurationService.class);
        Assert.assertNotNull(dcs);
        
        Populator populator = dcs.getPopulator();
        
        try {
            populator.populate(new ThrowOtherExceptionFinder());
            Assert.fail("Should not have succeded, the descriptor finder threw an unexpected exception");
        }
        catch (MultiException me) {
            boolean found = false;
            for (Throwable th : me.getErrors()) {
                if (th.getMessage().equals(EXPECTED)) {
                    found = true;
                    break;
                }
            }
            
            if (!found) throw me;
        }
    }
    
    /**
     * Tests the duplicate post processor will remove duplicates found
     * in the input stream
     * 
     * @throws IOException
     */
    @Test
    public void testDuplicatePostProcessor() throws IOException {
        DescriptorImpl di_8 = new DescriptorImpl();
        di_8.setImplementation(DUMMY_IMPL_8);
        di_8.addAdvertisedContract(DUMMY_IMPL_8);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintWriter pw = new PrintWriter(baos);
        
        di_8.writeObject(pw);
        di_8.writeObject(pw);  // Doing it twice is twice as nice!
        
        pw.close();
        
        DynamicConfigurationService dcs = locator.getService(DynamicConfigurationService.class);
        Assert.assertNotNull(dcs);
        
        Populator populator = dcs.getPopulator();
        
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        populator.populate(new MyDescriptorFinder(bais), new DuplicatePostProcessor());
        
        List<ActiveDescriptor<?>> lucky8list = locator.getDescriptors(BuilderHelper.createContractFilter(DUMMY_IMPL_8));
        
        // Duplicator should have gotten rid of one of them!
        Assert.assertEquals(1, lucky8list.size());
    }
    
    /**
     * Tests the duplicate post processor will remove duplicates found
     * in the input stream
     * 
     * @throws IOException
     */
    @Test
    public void testDuplicatePostProcessorImplOnly() throws IOException {
        DescriptorImpl di_10 = new DescriptorImpl();
        di_10.setImplementation(DUMMY_IMPL_10);
        di_10.addAdvertisedContract(DUMMY_IMPL_10);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintWriter pw = new PrintWriter(baos);
        
        di_10.writeObject(pw);
        di_10.writeObject(pw);  // Doing it twice is twice as nice!
        
        pw.close();
        
        DynamicConfigurationService dcs = locator.getService(DynamicConfigurationService.class);
        Assert.assertNotNull(dcs);
        
        Populator populator = dcs.getPopulator();
        
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        populator.populate(new MyDescriptorFinder(bais), new DuplicatePostProcessor(DuplicatePostProcessorMode.IMPLEMENTATION_ONLY));
        
        List<ActiveDescriptor<?>> lucky10list = locator.getDescriptors(BuilderHelper.createContractFilter(DUMMY_IMPL_10));
        
        // Duplicator should have gotten rid of one of them!
        Assert.assertEquals(1, lucky10list.size());
    }
    
    /**
     * Tests the duplicate post processor will remove duplicates already
     * in the service locator
     * 
     * @throws IOException
     */
    @Test
    public void testDuplicatePostProcessorWithExistingService() throws IOException {
        DescriptorImpl di_9 = new DescriptorImpl();
        di_9.setImplementation(DUMMY_IMPL_9);
        di_9.addAdvertisedContract(DUMMY_IMPL_9);
        
        ServiceLocatorUtilities.addOneDescriptor(locator, di_9);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintWriter pw = new PrintWriter(baos);
        
        di_9.writeObject(pw);
        
        pw.close();
        
        DynamicConfigurationService dcs = locator.getService(DynamicConfigurationService.class);
        Assert.assertNotNull(dcs);
        
        Populator populator = dcs.getPopulator();
        
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        populator.populate(new MyDescriptorFinder(bais), new DuplicatePostProcessor());
        
        List<ActiveDescriptor<?>> lucky9list = locator.getDescriptors(BuilderHelper.createContractFilter(DUMMY_IMPL_9));
        
        // Duplicator should have gotten rid of one of them!
        Assert.assertEquals(1, lucky9list.size());
    }
    
    /**
     * Tests the duplicate post processor will remove duplicates already
     * in the service locator
     * 
     * @throws IOException
     */
    @Test
    public void testDuplicatePostProcessorWithExistingServiceImplMode() throws IOException {
        DescriptorImpl di_11 = new DescriptorImpl();
        di_11.setImplementation(DUMMY_IMPL_11);
        di_11.addAdvertisedContract(DUMMY_IMPL_11);
        
        ServiceLocatorUtilities.addOneDescriptor(locator, di_11);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintWriter pw = new PrintWriter(baos);
        
        di_11.writeObject(pw);
        
        pw.close();
        
        DynamicConfigurationService dcs = locator.getService(DynamicConfigurationService.class);
        Assert.assertNotNull(dcs);
        
        Populator populator = dcs.getPopulator();
        
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        populator.populate(new MyDescriptorFinder(bais), new DuplicatePostProcessor(DuplicatePostProcessorMode.IMPLEMENTATION_ONLY));
        
        List<ActiveDescriptor<?>> lucky11list = locator.getDescriptors(BuilderHelper.createContractFilter(DUMMY_IMPL_11));
        
        // Duplicator should have gotten rid of one of them!
        Assert.assertEquals(1, lucky11list.size());
    }
    
    private static class MyDescriptorFinder implements DescriptorFileFinder {
        private final ByteArrayInputStream bais;
        
        private MyDescriptorFinder(ByteArrayInputStream bais) {
            this.bais = bais;
        }

        @Override
        public List<InputStream> findDescriptorFiles() throws IOException {
            LinkedList<InputStream> retVal = new LinkedList<InputStream>();
            retVal.add(bais);
            
            return retVal;
        }
        
    }
    
    private static class MetadataPostProcessor implements PopulatorPostProcessor {
        private final ServiceLocator locator;
        
        private MetadataPostProcessor(ServiceLocator locator) {
            this.locator = locator;
            
        }

        @Override
        public DescriptorImpl process(ServiceLocator serviceLocator,
                DescriptorImpl descriptorImpl) {
            if (!serviceLocator.equals(locator)) {
                Assert.fail("ServiceLocators should be equal, we got " + serviceLocator + " but were expecting " + locator);
            }
            
            descriptorImpl.addMetadata(KEY, VALUE);
            
            return descriptorImpl;
        }
        
    }
    
    private static class MetadataPostProcessor2 implements PopulatorPostProcessor {
        @Override
        public DescriptorImpl process(ServiceLocator serviceLocator,
                DescriptorImpl descriptorImpl) {
            descriptorImpl.addMetadata(KEY, VALUE2);
            
            return descriptorImpl;
        }
        
    }
    
    private static class RemoveDI3PostProcessor implements PopulatorPostProcessor {
        @Override
        public DescriptorImpl process(ServiceLocator serviceLocator,
                DescriptorImpl descriptorImpl) {
            if (descriptorImpl.getImplementation().equals(DUMMY_IMPL_3)) return null;
            
            return descriptorImpl;
        }
        
    }
    
    private static class ThrowExceptionPostProcessor implements PopulatorPostProcessor {
        @Override
        public DescriptorImpl process(ServiceLocator serviceLocator,
                DescriptorImpl descriptorImpl) {
            throw new RuntimeException(EXPECTED);
        }
        
    }
    
    private static class ThrowIOExceptionFinder implements DescriptorFileFinder {
        @Override
        public List<InputStream> findDescriptorFiles() throws IOException {
            throw new IOException(EXPECTED);
        }
        
    }
    
    private static class ThrowOtherExceptionFinder implements DescriptorFileFinder {
        @Override
        public List<InputStream> findDescriptorFiles() throws IOException {
            throw new RuntimeException(EXPECTED);
        }
        
    }

}
