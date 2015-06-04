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
package org.glassfish.hk2.tests.locator.negative.validation;

import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.Descriptor;
import org.glassfish.hk2.api.ErrorInformation;
import org.glassfish.hk2.api.ErrorType;
import org.glassfish.hk2.api.MultiException;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.tests.locator.utilities.LocatorHelper;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author jwells
 *
 */
public class ValidateThrowsTest {
    public final static String EXPECTED_EXCEPTION = "Expected Exception";
    
    /**
     * Tests that an exception during lookup operation does not throw
     * an exception, but instead invisibles the service
     */
    @Test // @org.junit.Ignore
    public void testExceptionInValidateDuringLookupDoesNotThrow() {
        ServiceLocator locator = LocatorHelper.getServiceLocator(ValidationServiceImpl.class,
                SimpleService.class);
        
        ValidationServiceImpl vsi = locator.getService(ValidationServiceImpl.class);
        SimpleService ss = locator.getService(SimpleService.class);
        Assert.assertNotNull(ss);
        
        vsi.setThrowFromValidate(true);
        
        ss = locator.getService(SimpleService.class);
        Assert.assertNull(ss);
        
        vsi.setThrowFromValidate(false);
        
        ss = locator.getService(SimpleService.class);
        Assert.assertNotNull(ss);
    }
    
    /**
     * Tests that an exception during bind operation causes MultiException
     * to be thrown
     */
    @Test // @org.junit.Ignore
    public void testExceptionInValidateDuringBindDoesNotThrow() {
        ServiceLocator locator = LocatorHelper.getServiceLocator(ValidationServiceImpl.class);
        
        ValidationServiceImpl vsi = locator.getService(ValidationServiceImpl.class);
        
        vsi.setThrowFromValidate(true);
        
        try {
            ServiceLocatorUtilities.addClasses(locator, SimpleService.class);
            Assert.fail("Should have failed with MultiException");
        }
        catch (MultiException me) {
            // This is ok
        }
        catch (Throwable th) {
            // Any other exception is a fail
            if (th instanceof RuntimeException) {
                throw (RuntimeException) th;
            }
            
            throw new RuntimeException(th);
        }
        
        // Make sure service was not actually added
        SimpleService ss = locator.getService(SimpleService.class);
        Assert.assertNull(ss);
    }
    
    /**
     * Tests that an exception during unbind operation causes MultiException
     * to be thrown
     */
    @Test // @org.junit.Ignore
    public void testExceptionInValidateDuringUnBindDoesNotThrow() {
        ServiceLocator locator = LocatorHelper.getServiceLocator(ValidationServiceImpl.class);
        
        ValidationServiceImpl vsi = locator.getService(ValidationServiceImpl.class);
        
        ActiveDescriptor<?> disposeMe = ServiceLocatorUtilities.addClasses(locator, SimpleService.class).get(0);
        
        vsi.setThrowFromValidate(true);
        
        try {
            ServiceLocatorUtilities.removeOneDescriptor(locator, disposeMe);
            Assert.fail("Should have failed with MultiException");
        }
        catch (MultiException me) {
            // Correct
        }
        catch (Throwable th) {
            // Any other exception is a fail
            if (th instanceof RuntimeException) {
                throw (RuntimeException) th;
            }
            
            throw new RuntimeException(th);
        }
        
        vsi.setThrowFromValidate(false);
        
        // Make sure service was not actually unbound
        SimpleService ss = locator.getService(SimpleService.class);
        Assert.assertNotNull(ss);
    }
    
    private void checkErrorInformation(ErrorInformation ei) {
        Assert.assertEquals(ErrorType.VALIDATE_FAILURE, ei.getErrorType());
        Assert.assertNull(ei.getInjectee());
        Assert.assertTrue(ei.getAssociatedException().toString().contains(EXPECTED_EXCEPTION));
        
        Descriptor d = ei.getDescriptor();
        Assert.assertEquals(d.getImplementation(), SimpleService.class.getName());
    }
    
    /**
     * Tests that error handler is invoked properly when looking
     * up a service where the validator throws an exception
     */
    @Test // @org.junit.Ignore
    public void testErrorHandlerInvokedWhenValidateThrowsInLookup() {
        ServiceLocator locator = LocatorHelper.getServiceLocator(ValidationServiceImpl.class,
                SimpleService.class,
                ErrorServiceImpl.class);
        
        ValidationServiceImpl vsi = locator.getService(ValidationServiceImpl.class);
        
        vsi.setThrowFromValidate(true);
        
        SimpleService ss = locator.getService(SimpleService.class);
        Assert.assertNull(ss);
        
        vsi.setThrowFromValidate(false);
        
        ErrorServiceImpl esi = locator.getService(ErrorServiceImpl.class);
        ErrorInformation ei = esi.getLastError();
        
        checkErrorInformation(ei);
    }
    
    /**
     * Tests that error handler is invoked properly when binding
     * a service where the validator throws an exception
     */
    @Test // @org.junit.Ignore
    public void testErrorHandlerInvokedWhenValidateThrowsInBind() {
        ServiceLocator locator = LocatorHelper.getServiceLocator(ValidationServiceImpl.class,
                ErrorServiceImpl.class);
        
        ValidationServiceImpl vsi = locator.getService(ValidationServiceImpl.class);
        ErrorServiceImpl esi = locator.getService(ErrorServiceImpl.class);
        
        vsi.setThrowFromValidate(true);
        
        try {
            ServiceLocatorUtilities.addClasses(locator, SimpleService.class);
            Assert.fail("Should have failed with MultiException");
        }
        catch (MultiException me) {
            // Correct
        }
        catch (Throwable th) {
            // Any other exception is a fail
            if (th instanceof RuntimeException) {
                throw (RuntimeException) th;
            }
            
            throw new RuntimeException(th);
        }
        
        ErrorInformation ei = esi.getLastError();
        
        checkErrorInformation(ei);
    }
    
    /**
     * Tests that error handler is invoked properly when binding
     * a service where the validator throws an exception
     */
    @Test // @org.junit.Ignore
    public void testErrorHandlerInvokedWhenValidateThrowsInUnBind() {
        ServiceLocator locator = LocatorHelper.getServiceLocator(ValidationServiceImpl.class,
                ErrorServiceImpl.class);
        
        ValidationServiceImpl vsi = locator.getService(ValidationServiceImpl.class);
        ErrorServiceImpl esi = locator.getService(ErrorServiceImpl.class);
        ActiveDescriptor<?> disposeMe = ServiceLocatorUtilities.addClasses(locator, SimpleService.class).get(0);
        
        vsi.setThrowFromValidate(true);
        
        try {
            ServiceLocatorUtilities.removeOneDescriptor(locator, disposeMe);
            Assert.fail("Should have failed with MultiException");
        }
        catch (MultiException me) {
            // Correct
        }
        catch (Throwable th) {
            // Any other exception is a fail
            if (th instanceof RuntimeException) {
                throw (RuntimeException) th;
            }
            
            throw new RuntimeException(th);
        }
        
        ErrorInformation ei = esi.getLastError();
        checkErrorInformation(ei);
    }
    
    /**
     * Tests that an exception during lookup operation AND from the
     * error service does not throw an exception, but instead invisibles the service
     */
    @Test // @org.junit.Ignore
    public void testExceptionInValidateAndOnFailureDuringLookupDoesNotThrow() {
        ServiceLocator locator = LocatorHelper.getServiceLocator(ValidationServiceImpl.class,
                SimpleService.class,
                ErrorServiceImpl.class);
        
        ValidationServiceImpl vsi = locator.getService(ValidationServiceImpl.class);
        ErrorServiceImpl esi = locator.getService(ErrorServiceImpl.class);
        
        SimpleService ss = locator.getService(SimpleService.class);
        Assert.assertNotNull(ss);
        
        vsi.setThrowFromValidate(true);
        esi.setThrowInOnFailure(true);
        
        ss = locator.getService(SimpleService.class);
        Assert.assertNull(ss);
        
        vsi.setThrowFromValidate(false);
        
        ss = locator.getService(SimpleService.class);
        Assert.assertNotNull(ss);
    }
    
    /**
     * Tests that an exception during bind operation causes MultiException
     * to be thrown
     */
    @Test // @org.junit.Ignore
    public void testExceptionInValidateDuringBindAndOnFailureDoesNotThrow() {
        ServiceLocator locator = LocatorHelper.getServiceLocator(ValidationServiceImpl.class,
                ErrorServiceImpl.class);
        
        ValidationServiceImpl vsi = locator.getService(ValidationServiceImpl.class);
        ErrorServiceImpl esi = locator.getService(ErrorServiceImpl.class);
        
        vsi.setThrowFromValidate(true);
        esi.setThrowInOnFailure(true);
        
        try {
            ServiceLocatorUtilities.addClasses(locator, SimpleService.class);
            Assert.fail("Should have failed with MultiException");
        }
        catch (MultiException me) {
            // Correct
        }
        catch (Throwable th) {
            // Any other exception is a fail
            if (th instanceof RuntimeException) {
                throw (RuntimeException) th;
            }
            
            throw new RuntimeException(th);
        }
        
        // Make sure service was not actually added
        SimpleService ss = locator.getService(SimpleService.class);
        Assert.assertNull(ss);
    }
    
    /**
     * Tests that an exception during unbind operation causes MultiException
     * to be thrown
     */
    @Test // @org.junit.Ignore
    public void testExceptionInValidateDuringUnBindAndOnErrorDoesNotThrow() {
        ServiceLocator locator = LocatorHelper.getServiceLocator(ValidationServiceImpl.class, ErrorServiceImpl.class);
        
        ValidationServiceImpl vsi = locator.getService(ValidationServiceImpl.class);
        ErrorServiceImpl esi = locator.getService(ErrorServiceImpl.class);
        
        ActiveDescriptor<?> disposeMe = ServiceLocatorUtilities.addClasses(locator, SimpleService.class).get(0);
        
        vsi.setThrowFromValidate(true);
        esi.setThrowInOnFailure(true);
        
        try {
            ServiceLocatorUtilities.removeOneDescriptor(locator, disposeMe);
            Assert.fail("Should have failed with MultiException");
        }
        catch (MultiException me) {
            // Correct
        }
        catch (Throwable th) {
            // Any other exception is a fail
            if (th instanceof RuntimeException) {
                throw (RuntimeException) th;
            }
            
            throw new RuntimeException(th);
        }
        
        vsi.setThrowFromValidate(false);
        
        // Make sure service was not actually unbound
        SimpleService ss = locator.getService(SimpleService.class);
        Assert.assertNotNull(ss);
    }

}
