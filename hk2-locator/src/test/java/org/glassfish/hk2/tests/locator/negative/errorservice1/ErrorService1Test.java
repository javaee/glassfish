/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2014 Oracle and/or its affiliates. All rights reserved.
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
package org.glassfish.hk2.tests.locator.negative.errorservice1;

import java.util.List;

import javax.inject.Singleton;

import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.ErrorInformation;
import org.glassfish.hk2.api.ErrorType;
import org.glassfish.hk2.api.FactoryDescriptors;
import org.glassfish.hk2.api.MultiException;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.tests.locator.utilities.LocatorHelper;
import org.glassfish.hk2.utilities.BuilderHelper;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author jwells
 *
 */
public class ErrorService1Test {
    public static final String ERROR_STRING = "Expected Exception ErrorService1Test";
    
    /**
     * Tests that a service that fails in the constructor has the error passed to the error service
     */
    @Test
    public void testServiceFailsInConstructor() {
        ServiceLocator locator = LocatorHelper.getServiceLocator(RecordingErrorService.class,
                ServiceFailsInConstructor.class);
        
        ActiveDescriptor<?> serviceDescriptor = locator.getBestDescriptor(BuilderHelper.createContractFilter(
                ServiceFailsInConstructor.class.getName()));
        Assert.assertNotNull(serviceDescriptor);
        
        try {
            locator.getService(ServiceFailsInConstructor.class);
            Assert.fail("Should have failed");
        }
        catch (MultiException me) {
            Assert.assertTrue(me.getMessage().contains(ERROR_STRING));
        }
        
        List<ErrorInformation> errors = locator.getService(RecordingErrorService.class).getAndClearErrors();
        
        Assert.assertEquals(1, errors.size());
        
        ErrorInformation ei = errors.get(0);
        
        Assert.assertEquals(ErrorType.SERVICE_CREATION_FAILURE, ei.getErrorType());
        Assert.assertEquals(serviceDescriptor, ei.getDescriptor());
        Assert.assertNull(ei.getInjectee());
        
        Throwable associatedException = ei.getAssociatedException();
        Assert.assertTrue(associatedException.getMessage().contains(ERROR_STRING));
    }
    
    /**
     * Tests that a service that fails in an initializer method has the error passed to the error service
     */
    @Test
    public void testServiceFailsInInitializer() {
        ServiceLocator locator = LocatorHelper.getServiceLocator(RecordingErrorService.class,
                SimpleService.class,
                ServiceFailsInInitializerMethod.class);
        
        ActiveDescriptor<?> serviceDescriptor = locator.getBestDescriptor(BuilderHelper.createContractFilter(
                ServiceFailsInInitializerMethod.class.getName()));
        Assert.assertNotNull(serviceDescriptor);
        
        try {
            locator.getService(ServiceFailsInInitializerMethod.class);
            Assert.fail("Should have failed");
        }
        catch (MultiException me) {
            Assert.assertTrue(me.getMessage().contains(ERROR_STRING));
        }
        
        List<ErrorInformation> errors = locator.getService(RecordingErrorService.class).getAndClearErrors();
        
        Assert.assertEquals(1, errors.size());
        
        ErrorInformation ei = errors.get(0);
        
        Assert.assertEquals(ErrorType.SERVICE_CREATION_FAILURE, ei.getErrorType());
        Assert.assertEquals(serviceDescriptor, ei.getDescriptor());
        Assert.assertNull(ei.getInjectee());
        
        Throwable associatedException = ei.getAssociatedException();
        Assert.assertTrue(associatedException.getMessage().contains(ERROR_STRING));
    }
    
    /**
     * Tests that a service that fails in an initializer method has the error passed to the error service
     */
    @Test
    public void testServiceFailsInPostConstruct() {
        ServiceLocator locator = LocatorHelper.getServiceLocator(RecordingErrorService.class,
                ServiceFailsInPostConstruct.class);
        
        ActiveDescriptor<?> serviceDescriptor = locator.getBestDescriptor(BuilderHelper.createContractFilter(
                ServiceFailsInPostConstruct.class.getName()));
        Assert.assertNotNull(serviceDescriptor);
        
        try {
            locator.getService(ServiceFailsInPostConstruct.class);
            Assert.fail("Should have failed");
        }
        catch (MultiException me) {
            Assert.assertTrue(me.getMessage().contains(ERROR_STRING));
        }
        
        List<ErrorInformation> errors = locator.getService(RecordingErrorService.class).getAndClearErrors();
        
        Assert.assertEquals(1, errors.size());
        
        ErrorInformation ei = errors.get(0);
        
        Assert.assertEquals(ErrorType.SERVICE_CREATION_FAILURE, ei.getErrorType());
        Assert.assertEquals(serviceDescriptor, ei.getDescriptor());
        Assert.assertNull(ei.getInjectee());
        
        Throwable associatedException = ei.getAssociatedException();
        Assert.assertTrue(associatedException.getMessage().contains(ERROR_STRING));
    }
    
    /**
     * Tests that a service that fails in an initializer method has the error passed to the error service
     */
    @Test
    public void testFactorServiceFailsInProvide() {
        ServiceLocator locator = LocatorHelper.getServiceLocator(RecordingErrorService.class);
        
        FactoryDescriptors fds = BuilderHelper.link(FactoryFailsInProvideService.class)
                     .to(SimpleService.class.getName())
                     .in(Singleton.class.getName())
                     .buildFactory(Singleton.class.getName());
        
        ServiceLocatorUtilities.addFactoryDescriptors(locator, fds);
        
        ActiveDescriptor<?> serviceDescriptor = locator.getBestDescriptor(BuilderHelper.createContractFilter(
                SimpleService.class.getName()));
        Assert.assertNotNull(serviceDescriptor);
        
        try {
            locator.getService(SimpleService.class);
            Assert.fail("Should have failed");
        }
        catch (MultiException me) {
            Assert.assertTrue(me.getMessage().contains(ERROR_STRING));
        }
        
        List<ErrorInformation> errors = locator.getService(RecordingErrorService.class).getAndClearErrors();
        
        Assert.assertEquals(1, errors.size());
        
        ErrorInformation ei = errors.get(0);
        
        Assert.assertEquals(ErrorType.SERVICE_CREATION_FAILURE, ei.getErrorType());
        Assert.assertEquals(serviceDescriptor, ei.getDescriptor());
        Assert.assertNull(ei.getInjectee());
        
        Throwable associatedException = ei.getAssociatedException();
        Assert.assertTrue(associatedException.getMessage().contains(ERROR_STRING));
    }
    
    /**
     * Tests that a service that fails in an initializer method has the error passed to the error service
     */
    @Test
    public void testSilentFailureInPostConstruct() {
        ServiceLocator locator = LocatorHelper.getServiceLocator(RecordingErrorService.class,
                ServiceDirectsNoErrorService.class);
        
        ActiveDescriptor<?> serviceDescriptor = locator.getBestDescriptor(BuilderHelper.createContractFilter(
                ServiceDirectsNoErrorService.class.getName()));
        Assert.assertNotNull(serviceDescriptor);
        
        try {
            locator.getService(ServiceDirectsNoErrorService.class);
            Assert.fail("Should have failed");
        }
        catch (MultiException me) {
            Assert.assertTrue(me.getMessage().contains(ERROR_STRING));
        }
        
        List<ErrorInformation> errors = locator.getService(RecordingErrorService.class).getAndClearErrors();
        
        Assert.assertEquals(0, errors.size());
    }

}
