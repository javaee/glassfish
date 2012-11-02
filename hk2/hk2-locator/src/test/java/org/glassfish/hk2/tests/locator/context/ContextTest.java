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
package org.glassfish.hk2.tests.locator.context;

import java.util.List;

import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.tests.locator.utilities.LocatorHelper;
import org.glassfish.hk2.utilities.BuilderHelper;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author jwells
 *
 */
public class ContextTest {
    private final static String TEST_NAME = "CustomContextTest";
    private final static ServiceLocator locator = LocatorHelper.create(TEST_NAME, new ContextModule());
    
    /**
     * Tests you can look up via context
     */
    @Test
    public void testLookupViaContext() {
        List<Object> viaContext = locator.<Object>getAllServices(CustomContext.class);
        Assert.assertNotNull(viaContext);
        
        Assert.assertTrue(2 == viaContext.size());
        
        Assert.assertTrue(viaContext.get(0) instanceof CustomService1);
        Assert.assertTrue(viaContext.get(1) instanceof CustomService2);
    }
    
    /**
     * Tests you can look up via context
     */
    @Test
    public void testDynamicallyAddAndRemoveFromCustomContext() {
        ActiveDescriptor<CustomService3> desc = ServiceLocatorUtilities.addOneDescriptor(locator,
                BuilderHelper.createDescriptorFromClass(CustomService3.class));
        
        List<Object> viaContext = locator.<Object>getAllServices(CustomContext.class);
        
        Assert.assertTrue("Expected size 3, got size " + viaContext.size(), 3 == viaContext.size());
        
        Assert.assertTrue(viaContext.get(0) instanceof CustomService1);
        Assert.assertTrue(viaContext.get(1) instanceof CustomService2);
        Assert.assertTrue(viaContext.get(2) instanceof CustomService3);
        
        ServiceLocatorUtilities.removeOneDescriptor(locator, desc);
        
        viaContext = locator.<Object>getAllServices(CustomContext.class);
        
        Assert.assertTrue(2 == viaContext.size());
        
        Assert.assertTrue(viaContext.get(0) instanceof CustomService1);
        Assert.assertTrue(viaContext.get(1) instanceof CustomService2);
    }

}
