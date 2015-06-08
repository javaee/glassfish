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

package org.glassfish.hk2.tests.locator.negative.self;

import java.util.List;

import junit.framework.Assert;

import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.MultiException;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.tests.locator.utilities.LocatorHelper;
import org.glassfish.hk2.utilities.BuilderHelper;
import org.junit.Test;

/**
 * @author jwells
 *
 */
public class InvalidSelfInjectionTest {
    private final static String TEST_NAME = "InvalidSelfInjectionTest";
    private final static ServiceLocator locator = LocatorHelper.create(TEST_NAME, new InvalidSelfInjectionModule());
    
    /**
     * This tests that we catch all the bad things wrong with the
     * self injection points
     */
    @Test
    public void testCatchAllWrongSelfInjections() {
        ActiveDescriptor<?> ad =
                locator.getBestDescriptor(BuilderHelper.createContractFilter(AllBadSelfInjectionService.class.getName()));
        Assert.assertNotNull(ad);
        
        try {
            locator.reifyDescriptor(ad);
            Assert.fail("Should have failed to reify due to bad @Self injection points");
        }
        catch (MultiException me) {
            List<Throwable> errors = me.getErrors();
            
            int badOptional = 0;
            int badType = 0;
            int badQualifier = 0;
            
            for (Throwable th : errors) {
                if (th.getMessage().contains(" does not have the required type of ActiveDescriptor")) {
                    badType++;
                }
                if (th.getMessage().contains(" is marked both @Optional and @Self")) {
                    badOptional++;
                }
                if (th.getMessage().contains(" is marked @Self but has other qualifiers")) {
                    badQualifier++;
                }
            }
            
            Assert.assertTrue(2 == badOptional);
            Assert.assertTrue(2 == badType);
            Assert.assertTrue(1 == badQualifier);
        }
        
    }
    
    /**
     * This tests a service that has an Self in the constructor but is being constructed via the API
     */
    @Test
    public void testCreateWithSelf() {
        try {
            locator.create(AValidSelfInjectedService.class);
            Assert.fail("Should have failed to reify due to bad @Self injection point");
        }
        catch (MultiException me) {
            Assert.assertTrue(me.getMessage().contains(
                    " is being created or injected via the non-managed ServiceLocator API"));
        }
        
    }
    
    /**
     * This tests a service that has an Self in the constructor but is being constructed via the API
     */
    @Test
    public void testInjectWithMethodSelf() {
        try {
            AnotherValidSelfInjectedService obj = locator.create(AnotherValidSelfInjectedService.class);
            locator.inject(obj);
            
            Assert.fail("Should have failed to reify due to bad @Self injection point");
        }
        catch (MultiException me) {
            Assert.assertTrue(me.getMessage().contains(
                    " is being created or injected via the non-managed ServiceLocator API"));
        }
        
    }
    
    /**
     * This tests a service that has an Self in the constructor but is being constructed via the API
     */
    @Test
    public void testInjectWithFieldSelf() {
        try {
            ThirdValidSelfInjectedService obj = locator.create(ThirdValidSelfInjectedService.class);
            locator.inject(obj);
            
            Assert.fail("Should have failed to reify due to bad @Self injection point");
        }
        catch (MultiException me) {
            Assert.assertTrue(me.getMessage().contains(
                    " is being created or injected via the non-managed ServiceLocator API"));
        }
        
    }

}
