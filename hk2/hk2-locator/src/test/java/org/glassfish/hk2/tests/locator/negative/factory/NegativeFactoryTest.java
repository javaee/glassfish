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
package org.glassfish.hk2.tests.locator.negative.factory;

import junit.framework.Assert;

import org.glassfish.hk2.api.MultiException;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.tests.locator.utilities.LocatorHelper;
import org.glassfish.hk2.utilities.BuilderHelper;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author jwells
 *
 */
public class NegativeFactoryTest {
    private final static String TEST_NAME = "NegativeFactoryTest";
    private final static ServiceLocator locator = LocatorHelper.create(TEST_NAME, new NegativeFactoryModule());
    
    /* package */ final static String THROW_STRING = "Expected thrown exception";
    
    /**
     * Factories cannot have a Named annation with no value
     */
    @Test
    public void testFactoryWithBadName() {
        try {
            locator.reifyDescriptor(locator.getBestDescriptor(BuilderHelper.createContractFilter(
                    SimpleService2.class.getName())));
            Assert.fail("The SimpleService2 factory has a bad name and so is invalid");
        }
        catch (MultiException me) {
            Assert.assertTrue(me.getMessage(), me.getMessage().contains(
                    "@Named on the provide method of a factory must have an explicit value"));
        }
        
    }
    
    /**
     * Ensures that a factory producing for the singleton scope works properly
     */
    @Test
    public void testFactoryThatThrowsInSingletonScope() {
        try {
            locator.getService(SimpleService.class);
            Assert.fail("The factory throws an exception, so should not have gotten here");
        }
        catch (MultiException me) {
            Assert.assertTrue(me.getMessage().contains(THROW_STRING));
        }
    }

}
