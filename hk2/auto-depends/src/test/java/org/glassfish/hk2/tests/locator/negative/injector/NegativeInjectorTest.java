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
package org.glassfish.hk2.tests.locator.negative.injector;

import junit.framework.Assert;

import org.glassfish.hk2.api.MultiException;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.tests.locator.utilities.LocatorHelper;
import org.junit.Test;

/**
 * A set of tests for negative path
 * 
 * @author jwells
 */
public class NegativeInjectorTest {
    private final static String TEST_NAME = "NegativeInjectorTest";
    private final static ServiceLocator locator = LocatorHelper.create(TEST_NAME, new NegativeInjectorModule());
    
    /**
     * null to create
     */
    @Test(expected=IllegalArgumentException.class)
    public void testNullCreate() {
        locator.create(null);
    }
    
    /**
     * null to inject
     */
    @Test(expected=IllegalArgumentException.class)
    public void testNullInject() {
        locator.inject(null);
    }
    
    /**
     * null to post construct
     */
    @Test(expected=IllegalArgumentException.class)
    public void testNullPostConstruct() {
        locator.postConstruct(null);
    }
    
    /**
     * null to preDestroy
     */
    @Test(expected=IllegalArgumentException.class)
    public void testNullPreDestroy() {
        locator.preDestroy(null);
    }
    
    /**
     * null to preDestroy
     */
    @Test
    public void testConstructorThrows() {
        try {
            locator.create(ThrowyC.class);
            Assert.fail("ThrowyC throws in its constructor");
        }
        catch (MultiException me) {
            Assert.assertEquals(LocatorHelper.EXPECTED, me.getMessage());
        }
    }
    
    private final static String FIELD_EXPECTED = "may not be static or final";
    
    /**
     * tries to inject into a bad string
     */
    @Test
    public void testBadField() {
        ThrowyF tf = new ThrowyF();
        
        try {
            locator.inject(tf);
            Assert.fail("ThrowyF has a final field to be injected");
        }
        catch (MultiException me) {
            Assert.assertTrue(me.getMessage().contains(FIELD_EXPECTED));
        }
    }
    
    /**
     * post construct throws
     */
    @Test
    public void testBadPostConstruct() {
        ThrowyPC tpc = new ThrowyPC();
        
        try {
            locator.postConstruct(tpc);
            Assert.fail("ThrowyPC throws in its post construct");
        }
        catch (MultiException me) {
            Assert.assertEquals(LocatorHelper.EXPECTED, me.getMessage());
        }
    }
    
    /**
     * pre destroy throws
     */
    @Test
    public void testBadPreDestroy() {
        ThrowyPC tpc = new ThrowyPC();
        
        try {
            locator.preDestroy(tpc);
            Assert.fail("ThrowyPC throws in its pre destroy");
        }
        catch (MultiException me) {
            Assert.assertEquals(LocatorHelper.EXPECTED, me.getMessage());
        }
    }
    
    /**
     * method body throws
     */
    @Test
    public void testBadMethod() {
        ThrowyM tpc = new ThrowyM();
        
        try {
            locator.inject(tpc);
            Assert.fail("ThrowyM throws in its initializer method");
        }
        catch (MultiException me) {
            Assert.assertEquals(LocatorHelper.EXPECTED, me.getMessage());
        }
    }

}
