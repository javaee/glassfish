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
package org.glassfish.hk2.tests.locator.validating;

import java.util.List;

import javax.inject.Singleton;

import junit.framework.Assert;

import org.glassfish.hk2.api.DynamicConfiguration;
import org.glassfish.hk2.api.DynamicConfigurationService;
import org.glassfish.hk2.api.MultiException;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.api.ValidationService;
import org.glassfish.hk2.tests.locator.utilities.LocatorHelper;
import org.glassfish.hk2.utilities.Binder;
import org.glassfish.hk2.utilities.BuilderHelper;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author jwells
 *
 */
public class ValidatingTest {
    private final String EXCEPTION_STRING = "There was no object available for injection at";
    
    private final static String TEST_NAME = "ValidatingTest";
    private final static ServiceLocator locator = LocatorHelper.create(TEST_NAME, new ValidatingModule());

    /**
     * I can get the system service, who gets a secret service
     */
    @Test
    public void testSystemServiceIsOK() {
        SystemService systemService = locator.getService(SystemService.class);
        Assert.assertNotNull(systemService);  // If I got it, it worked!
    }
    
    /**
     * The user service should NOT be able to see the secret service
     */
    @Test
    public void testUserServiceIsNotOK() {
        try {
            locator.getService(UserService.class);
            Assert.fail("This should have failed due to the validator");
        }
        catch (MultiException me) {
            List<Throwable> errors = me.getErrors();
            
            Assert.assertEquals(me.toString(), 3, errors.size());
            
            for (Throwable lookAtMe : errors) {
                if (lookAtMe.getMessage().contains(EXCEPTION_STRING)) {
                    // Success
                    return;
                }
            }
            
            Assert.fail("None of the exceptions in the multi exception had the expected string " + me);
        }
    }
    
    /**
     * The validator does not allow direct lookup of this service
     */
    @Test
    public void testEvilLookup() {
        Assert.assertNull(locator.getService(SuperSecretService.class));
    }
    
    /**
     * Tests a bind that should fail validation
     */
    @Test
    public void testBindValidationFailure() {
        DynamicConfigurationService dcs = locator.getService(DynamicConfigurationService.class);
        DynamicConfiguration configuration = dcs.createDynamicConfiguration();
        
        configuration.bind(BuilderHelper.link(NeverBindMeService.class).build());
        
        try {
            configuration.commit();
            Assert.fail("Bind should have failed due to validation problem");
        }
        catch (MultiException me) {
            Assert.assertTrue(me.getMessage(), me.getMessage().contains(
                " did not pass the BIND validation"));
        }
    }
    
    /**
     * Tests a bind that should fail validation
     */
    @Test
    public void testUnBindValidationFailure() {
        DynamicConfigurationService dcs = locator.getService(DynamicConfigurationService.class);
        DynamicConfiguration configuration = dcs.createDynamicConfiguration();
        
        configuration.addUnbindFilter(BuilderHelper.createContractFilter(NeverUnbindMeService.class.getName()));
        
        try {
            configuration.commit();
            Assert.fail("Bind should have failed due to unbind validation problem");
        }
        catch (MultiException me) {
            Assert.assertTrue(me.getMessage(), me.getMessage().contains(
                    " did not pass the UNBIND validation"));
        }
    }
    
    /**
     * This test ensures that validation changes in the parent properly affects children
     */
    @Test
    public void testValidationChangeInParent() {
        ServiceLocator childLocator = LocatorHelper.create("child", locator, null);
        
        DynamicValidator dv = childLocator.getService(DynamicValidator.class);
        
        dv.addBadGuy(DynamicServiceImpl1.class.getName());
        
        List<DynamicService> services = childLocator.getAllServices(DynamicService.class);
        
        // Only one, because the other is not valid in the parent
        Assert.assertEquals(1, services.size());
        
        DynamicService ds2 = services.get(0);
        
        Assert.assertEquals(2, ds2.getImplNumber());
        
        // Now dynamically change the validation, so that 1 is ok again
        dv.removeBadGuy(DynamicServiceImpl1.class.getName());
        
        services = childLocator.getAllServices(DynamicService.class);
        
        // Should now have both services
        Assert.assertEquals(2, services.size());
        
        DynamicService ds1 = services.get(0);
        ds2 = services.get(1);
        
        Assert.assertEquals(1, ds1.getImplNumber());
        Assert.assertEquals(2, ds2.getImplNumber());
    }
    
    /**
     * Tests a state based validation using getService
     */
    @Test
    public void testStateBasedValidation() {
        // Overrides global one
        ServiceLocator locator = LocatorHelper.create("ValidationTest_2", null);
        
        ServiceLocatorUtilities.bind(locator, new Binder() {

            @Override
            public void bind(DynamicConfiguration config) {
                config.bind(BuilderHelper.link(DynamicServiceImpl1.class.getName()).
                        to(DynamicService.class.getName()).
                        in(Singleton.class.getName()).
                        build());
                config.bind(BuilderHelper.link(DynamicServiceImpl2.class.getName()).
                        to(DynamicService.class.getName()).
                        in(Singleton.class.getName()).
                        build());
                config.bind(BuilderHelper.link(StateBasedValidationService.class.getName()).
                        to(ValidationService.class.getName()).
                        in(Singleton.class.getName()).
                        build());
                
            }
            
        });
        
        // First get one
        StateBasedValidationService sbvs = (StateBasedValidationService)
                locator.getService(ValidationService.class);
        Assert.assertNotNull(sbvs);
        
        sbvs.setCurrentState(1);
        
        DynamicService d1 = locator.getService(DynamicService.class);
        Assert.assertNotNull(d1);
        Assert.assertEquals(1, d1.getImplNumber());
        
        // Now switch the state to state 2
        sbvs.setCurrentState(2);
        
        DynamicService d2 = locator.getService(DynamicService.class);
        Assert.assertNotNull(d2);
        Assert.assertEquals(2, d2.getImplNumber());
    }
    
    private static ServiceLocator generateGetCallerLocators(String testName) {
        ServiceLocator retVal = LocatorHelper.create(TEST_NAME + "." + testName, null);
        
        ServiceLocatorUtilities.addClasses(retVal, CheckCallerValidationService.class);
        
        return retVal;
    }
    
    /**
     * Tests that the stack frame for a direct lookup call is this one!
     */
    @Test @Ignore
    public void testImmediateCaller() {
        ServiceLocator testLocator = generateGetCallerLocators("testImmediateCaller");
        
        CheckCallerValidationService val = testLocator.getService(CheckCallerValidationService.class);
        Assert.assertNotNull(val);
        
        StackTraceElement callerFrame = val.getLastCaller();
        Assert.assertNotNull(callerFrame);
        
        Assert.assertEquals(getClass().getName(), callerFrame.getClassName());
    }
}
