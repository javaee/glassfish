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

import junit.framework.Assert;

import org.glassfish.hk2.api.MultiException;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.api.ServiceLocatorFactory;
import org.junit.Before;
import org.junit.Test;

/**
 * @author jwells
 *
 */
public class ValidatingTest {
    private final String EXCEPTION_STRING = "There was no object available for injection at";
    
    public final static String TEST_NAME = "ValidatingTest";
    private ServiceLocator locator;
    
    @Before
    public void before() {
        locator = ServiceLocatorFactory.getInstance().create(TEST_NAME, new ValidatingModule());
        if (locator == null) {
            locator = ServiceLocatorFactory.getInstance().find(TEST_NAME);   
        }
    }

    @Test
    public void testSystemServiceIsOK() {
        SystemService systemService = locator.getService(SystemService.class);
        Assert.assertNotNull(systemService);  // If I got it, it worked!
    }
    
    @Test
    public void testUserServiceIsNotOK() {
        try {
            locator.getService(UserService.class);
            Assert.fail("This should have failed due to the validator");
        }
        catch (MultiException me) {
            List<Throwable> errors = me.getErrors();
            
            Assert.assertEquals(me.toString(), 1, errors.size());
            
            Throwable underlyingCause = errors.get(0);
            
            Assert.assertTrue(underlyingCause instanceof IllegalStateException);
            Assert.assertTrue(underlyingCause.getMessage().contains(EXCEPTION_STRING));
        }
    }
    
    /**
     * The validator does not allow direct lookup of this service
     */
    @Test
    public void testEvilLookup() {
        Assert.assertNull(locator.getService(SuperSecretService.class));
    }
    
    

}
