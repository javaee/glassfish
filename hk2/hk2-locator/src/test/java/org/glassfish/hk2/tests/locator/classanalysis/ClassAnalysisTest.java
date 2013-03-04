/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2013 Oracle and/or its affiliates. All rights reserved.
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
package org.glassfish.hk2.tests.locator.classanalysis;

import org.glassfish.hk2.api.DynamicConfiguration;
import org.glassfish.hk2.api.DynamicConfigurationService;
import org.glassfish.hk2.api.ServiceHandle;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.tests.locator.utilities.LocatorHelper;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for the {@link ClassAnalysis} feature
 * 
 * @author jwells
 *
 */
public class ClassAnalysisTest {
    private final static String TEST_NAME = "ClassAnalysisTest";
    private final static ServiceLocator locator = LocatorHelper.create(TEST_NAME, new ClassAnalysisModule());
    
    public static final String ALTERNATE_DEFAULT_ANALYZER = "AlternateDefaultAnalyzer";
    
    @Test
    public void testChangeDefaultAnalyzer() {
        AlternateDefaultAnalyzer alternate = null;
        
        locator.setDefaultClassAnalyzerName(ALTERNATE_DEFAULT_ANALYZER);
        try {
            alternate = locator.getService(AlternateDefaultAnalyzer.class);
            Assert.assertNotNull(alternate);
            
            alternate.reset();
            
            // Now lookup a simple service, which should be analyzed with the alternate
            Assert.assertNotNull(locator.getService(SimpleService1.class));
            
            alternate.check();
        }
        finally {
            locator.setDefaultClassAnalyzerName(null);
        }
        
        alternate.reset();
        
        // Now make sure that the other default has taken over
        Assert.assertNotNull(locator.getService(SimpleService2.class));
        
        alternate.unused();
    }
    
    @Test
    public void testCustomClassAnalyzer() {
        ServiceHandle<ServiceWithManyDoubles> handle = locator.getServiceHandle(ServiceWithManyDoubles.class);
        Assert.assertNotNull(handle);
        
        ServiceWithManyDoubles service = handle.getService();
        Assert.assertNotNull(service);
        
        handle.destroy();
        
        service.checkCalls();
    }
    
    @Test
    public void testCustomCreateStrategy() {
        ServiceWithManyDoubles service = locator.create(ServiceWithManyDoubles.class, DoubleClassAnalyzer.DOUBLE_ANALYZER);
        Assert.assertNotNull(service);
        
        service.checkAfterConstructor();
    }
    
    @Test
    public void testCustomInitializationStrategy() {
        ServiceWithManyDoubles service = new ServiceWithManyDoubles(DoubleFactory.DOUBLE);
        
        locator.inject(service, DoubleClassAnalyzer.DOUBLE_ANALYZER);
        
        service.checkAfterInitializeBeforePostConstruct();
    }
    
    @Test
    public void testCustomPostDestroyStrategy() {
        ServiceWithManyDoubles service = new ServiceWithManyDoubles(DoubleFactory.DOUBLE);
        
        locator.postConstruct(service, DoubleClassAnalyzer.DOUBLE_ANALYZER);
        
        service.checkAfterPostConstructWithNoInitialization();
    }
    
    @Test
    public void testCustomFullCreateAPI() {
        ServiceWithManyDoubles service = locator.createAndInitialize(ServiceWithManyDoubles.class,
                DoubleClassAnalyzer.DOUBLE_ANALYZER);
        
        service.checkFullCreateWithoutDestroy();
    }
    
    @Test
    public void testLongestConstructor() {
        JaxRsService jrs = locator.getService(JaxRsService.class);
        Assert.assertNotNull(jrs);
        
        jrs.checkProperConstructor();
    }
    
    /**
     * This test also ensures that the analyzer field of Service
     * is honored by the automatic analysis in addActiveDescriptor
     */
    @Test
    public void testLongestConstructorWithNoZeroArgConstructor() {
        DynamicConfigurationService dcs = locator.getService(DynamicConfigurationService.class);
        DynamicConfiguration config = dcs.createDynamicConfiguration();
        
        config.addActiveDescriptor(ServiceWithNoValidHK2Constructor.class);
        
        config.commit();
        
        ServiceWithNoValidHK2Constructor service = locator.getService(ServiceWithNoValidHK2Constructor.class);
        service.check();
    }
    
    /**
     * This test also ensures that the analyzer field of Service
     * is honored by the automatic analysis in addActiveDescriptor
     */
    @Test
    public void testLongestConstructorWithValidHK2Constructor() {
        DynamicConfigurationService dcs = locator.getService(DynamicConfigurationService.class);
        DynamicConfiguration config = dcs.createDynamicConfiguration();
        
        config.addActiveDescriptor(ServiceWithValidHK2NonZeroArgConstructor.class);
        
        config.commit();
        
        ServiceWithValidHK2NonZeroArgConstructor service = locator.getService(ServiceWithValidHK2NonZeroArgConstructor.class);
        service.check();
    }
    
    /**
     * This test also ensures that the analyzer field of Service
     * is honored by the automatic analysis in addActiveDescriptor
     */
    @Test
    public void testLongestConstructorWithValidHK2ZeroArgConstructor() {
        DynamicConfigurationService dcs = locator.getService(DynamicConfigurationService.class);
        DynamicConfiguration config = dcs.createDynamicConfiguration();
        
        config.addActiveDescriptor(ServiceWithValidHK2NoArgConstructor.class);
        
        config.commit();
        
        ServiceWithValidHK2NoArgConstructor service = locator.getService(ServiceWithValidHK2NoArgConstructor.class);
        service.check();
    }
}
