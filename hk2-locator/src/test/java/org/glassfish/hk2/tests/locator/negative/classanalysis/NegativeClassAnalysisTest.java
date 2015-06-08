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

package org.glassfish.hk2.tests.locator.negative.classanalysis;

import junit.framework.Assert;

import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.ClassAnalyzer;
import org.glassfish.hk2.api.MultiException;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.tests.locator.utilities.LocatorHelper;
import org.glassfish.hk2.utilities.BuilderHelper;
import org.junit.Test;

/**
 * Negative tests for class analysis
 * 
 * @author jwells
 *
 */
public class NegativeClassAnalysisTest {
    private final static String TEST_NAME = "NegativeClassAnalysisTest";
    private final static ServiceLocator locator = LocatorHelper.create(TEST_NAME, new NegativeClassAnalysisModule());
    
    public final static String C_THROW = "Expected throw from constructor";
    public final static String M_THROW = "Expected throw from method";
    public final static String F_THROW = "Expected throw from field";
    public final static String PC_THROW = "Expected throw from pc";
    public final static String PD_THROW = "Expected throw from pd";
    
    public final static String NULL_RETURN = "null return";
    public final static String SELF_ANALYZER = "Narcissus";
    
    @Test
    public void testBadConstructorThrow() {
        ConfigurablyBadClassAnalyzer cbca = locator.getService(ConfigurablyBadClassAnalyzer.class);
        cbca.resetToGood();
        
        cbca.setThrowFromConstructor(true);
        try {
            locator.create(SimpleService.class, ConfigurablyBadClassAnalyzer.BAD_ANALYZER_NAME);
            Assert.fail("Should have failed due to bad constructor throw");
        }
        catch (MultiException me) {
            Assert.assertTrue(me.toString().contains(C_THROW));
        }
        
    }
    
    @Test
    public void testBadMethodThrow() {
        ConfigurablyBadClassAnalyzer cbca = locator.getService(ConfigurablyBadClassAnalyzer.class);
        cbca.resetToGood();
        
        cbca.setThrowFromMethods(true);
        try {
            SimpleService ss = new SimpleService();
            locator.inject(ss, ConfigurablyBadClassAnalyzer.BAD_ANALYZER_NAME);
            Assert.fail("Should have failed due to bad method throw");
        }
        catch (MultiException me) {
            Assert.assertTrue(me.toString().contains(M_THROW));
        }
        
    }
    
    @Test
    public void testBadFieldThrow() {
        ConfigurablyBadClassAnalyzer cbca = locator.getService(ConfigurablyBadClassAnalyzer.class);
        cbca.resetToGood();
        
        cbca.setThrowFromFields(true);
        try {
            SimpleService ss = new SimpleService();
            locator.inject(ss, ConfigurablyBadClassAnalyzer.BAD_ANALYZER_NAME);
            Assert.fail("Should have failed due to bad field throw");
        }
        catch (MultiException me) {
            Assert.assertTrue(me.toString().contains(F_THROW));
        }
        
    }
    
    @Test
    public void testBadPCThrow() {
        ConfigurablyBadClassAnalyzer cbca = locator.getService(ConfigurablyBadClassAnalyzer.class);
        cbca.resetToGood();
        
        cbca.setThrowFromPostConstruct(true);
        try {
            SimpleService ss = new SimpleService();
            locator.postConstruct(ss, ConfigurablyBadClassAnalyzer.BAD_ANALYZER_NAME);
            Assert.fail("Should have failed due to bad pc throw");
        }
        catch (MultiException me) {
            Assert.assertTrue(me.toString().contains(PC_THROW));
        }
        
    }
    
    @Test
    public void testBadPDThrow() {
        ConfigurablyBadClassAnalyzer cbca = locator.getService(ConfigurablyBadClassAnalyzer.class);
        cbca.resetToGood();
        
        cbca.setThrowFromPreDestroy(true);
        try {
            SimpleService ss = new SimpleService();
            locator.preDestroy(ss, ConfigurablyBadClassAnalyzer.BAD_ANALYZER_NAME);
            Assert.fail("Should have failed due to bad pd throw");
        }
        catch (MultiException me) {
            Assert.assertTrue(me.toString(), me.toString().contains(PD_THROW));
        }
        
    }
    
    @Test
    public void testBadConstructorNull() {
        ConfigurablyBadClassAnalyzer cbca = locator.getService(ConfigurablyBadClassAnalyzer.class);
        cbca.resetToGood();
        
        cbca.setNullFromConstructor(true);
        try {
            locator.create(SimpleService.class, ConfigurablyBadClassAnalyzer.BAD_ANALYZER_NAME);
            Assert.fail("Should have failed due to null constructor return");
        }
        catch (MultiException me) {
            Assert.assertTrue(me.toString().contains("null return"));
        }
        
    }
    
    @Test
    public void testBadMethodNull() {
        ConfigurablyBadClassAnalyzer cbca = locator.getService(ConfigurablyBadClassAnalyzer.class);
        cbca.resetToGood();
        
        cbca.setNullFromMethods(true);
        try {
            SimpleService ss = new SimpleService();
            locator.inject(ss, ConfigurablyBadClassAnalyzer.BAD_ANALYZER_NAME);
            Assert.fail("Should have failed due to null method return");
        }
        catch (MultiException me) {
            Assert.assertTrue(me.toString().contains("null return"));
        }
        
    }
    
    @Test
    public void testBadFieldNull() {
        ConfigurablyBadClassAnalyzer cbca = locator.getService(ConfigurablyBadClassAnalyzer.class);
        cbca.resetToGood();
        
        cbca.setNullFromFields(true);
        try {
            SimpleService ss = new SimpleService();
            locator.inject(ss, ConfigurablyBadClassAnalyzer.BAD_ANALYZER_NAME);
            Assert.fail("Should have failed due to null method return");
        }
        catch (MultiException me) {
            Assert.assertTrue(me.toString().contains("null return"));
        }
        
    }
    
    /**
     * This test makes sure a class analyzer is not its own analyzer
     */
    @Test
    public void testSelfAnalyzer() {
        ActiveDescriptor<?> selfDescriptor =
                locator.getBestDescriptor(BuilderHelper.createNameAndContractFilter(
                        ClassAnalyzer.class.getName(),
                        SELF_ANALYZER));
        Assert.assertNotNull(selfDescriptor);
        
        try {
            locator.reifyDescriptor(selfDescriptor);
            Assert.fail("Should have failed, a class may not analyze itself");
        }
        catch (MultiException me) {
            Assert.assertTrue(me.toString().contains("is its own ClassAnalyzer"));
        }
    }

}
