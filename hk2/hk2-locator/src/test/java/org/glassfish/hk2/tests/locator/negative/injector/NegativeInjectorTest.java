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

import java.lang.reflect.Field;
import java.util.List;

import junit.framework.Assert;

import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.DynamicConfiguration;
import org.glassfish.hk2.api.DynamicConfigurationService;
import org.glassfish.hk2.api.Injectee;
import org.glassfish.hk2.api.InjectionResolver;
import org.glassfish.hk2.api.MultiException;
import org.glassfish.hk2.api.ServiceHandle;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.api.UnsatisfiedDependencyException;
import org.glassfish.hk2.tests.locator.utilities.LocatorHelper;
import org.glassfish.hk2.utilities.BuilderHelper;
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
     * No class, I tell ya, no class!
     */
    public final static String NO_CLASS = "this.class.is.not.Here";
    
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
            Assert.assertTrue("Expected " + LocatorHelper.EXPECTED + " but got " + me.getMessage(), me.getMessage().contains(LocatorHelper.EXPECTED));
        }
    }
    
    private final static String FIELD_EXPECTED = " may not be static, final or have an Annotation type";
    
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
            Assert.assertTrue(me.getMessage(), me.getMessage().contains(FIELD_EXPECTED));
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
            Assert.assertTrue("Expected " + LocatorHelper.EXPECTED + " but got " + me.getMessage(),
                    me.getMessage().contains(LocatorHelper.EXPECTED));
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
            Assert.assertTrue("Expected " + LocatorHelper.EXPECTED + " but got " + me.getMessage(),
                    me.getMessage().contains(LocatorHelper.EXPECTED));
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
            Assert.assertTrue("Expected " + LocatorHelper.EXPECTED + " but got " + me.getMessage(),
                    me.getMessage().contains(LocatorHelper.EXPECTED));
        }
    }
    
    /**
     * tests a classloader failure (since there really is no class)
     */
    @Test
    public void testNoClass() {
        ActiveDescriptor<?> ad = locator.getBestDescriptor(BuilderHelper.createContractFilter(NO_CLASS));
        Assert.assertNotNull(ad);
        
        ServiceHandle<?> handle = locator.getServiceHandle(ad);
        Assert.assertNotNull(handle);
        
        try {
            handle.getService();
        }
        catch (MultiException me) {
            // Two exceptions, one from the HK2 classloader, one from the CCL
            List<Throwable> thList = me.getErrors();
            Assert.assertEquals(2, thList.size());
            
            Throwable th = thList.get(0);
            
            Assert.assertTrue(th instanceof ClassNotFoundException);
            
            Throwable th2 = thList.get(1);
            
            Assert.assertTrue(th2 instanceof ClassNotFoundException);
        }
    }
    
    /**
     * Tests an injection resolver that does not have a parameterized type
     */
    @Test
    public void testRawInjectionResolver() {
        DynamicConfigurationService dcs = locator.getService(DynamicConfigurationService.class);
        DynamicConfiguration config = dcs.createDynamicConfiguration();
        
        config.bind(BuilderHelper.link(RawInjectionResolver.class).
                to(InjectionResolver.class).build());
        
        try {
            config.commit();
            Assert.fail("Bad injection resolver should have caused commit to fail");
        }
        catch (MultiException me) {
            Assert.assertTrue(me.getMessage(),
                    me.getMessage().contains("An implementation of InjectionResolver must be a parameterized type and the actual type" +
                            " must be an annotation"));
        }
    }
    
    /**
     * Tests an injection resolver that has a type variable for its type
     */
    @Test
    public void testTypeVariableInjectionResolver() {
        DynamicConfigurationService dcs = locator.getService(DynamicConfigurationService.class);
        DynamicConfiguration config = dcs.createDynamicConfiguration();
        
        config.bind(BuilderHelper.link(TypeVariableInjectionResolver.class).
                to(InjectionResolver.class).build());
        
        try {
            config.commit();
            Assert.fail("Bad injection resolver should have caused commit to fail");
        }
        catch (MultiException me) {
            Assert.assertTrue(me.getMessage(),
                    me.getMessage().contains("An implementation of InjectionResolver must be a parameterized type and the actual type" +
                            " must be an annotation"));
        }
    }
    
    /**
     * Tests an injection resolver that has a non annotation as its actual type
     */
    @Test
    public void testNotAnnotationInjectionResolver() {
        DynamicConfigurationService dcs = locator.getService(DynamicConfigurationService.class);
        DynamicConfiguration config = dcs.createDynamicConfiguration();
        
        config.bind(BuilderHelper.link(NotAnnotationInjectionResolver.class).
                to(InjectionResolver.class).build());
        
        try {
            config.commit();
            Assert.fail("Bad injection resolver should have caused commit to fail");
        }
        catch (MultiException me) {
            Assert.assertTrue(me.getMessage(),
                    me.getMessage().contains("An implementation of InjectionResolver must be a parameterized type and the actual type" +
                            " must be an annotation"));
        }
    }
    
    /**
     * Tests an attempt to get a descriptor for a bad injectee
     */
    @Test
    public void testInvalidInjectee() {
        try {
            locator.getInjecteeDescriptor(new InjecteeImpl());
            Assert.fail("Bad injectee should have caused this to fail");
        }
        catch (MultiException me) {
            Assert.assertTrue(me.getMessage(),
                    me.getMessage().contains("Invalid injectee with required type of "));
        }
    }
    
    /**
     * This post construct has a bad parameter
     */
    @Test
    public void testInvalidPostConstruct() {
        BadPC badPC = new BadPC();
        
        try {
            locator.postConstruct(badPC);
            Assert.fail("This post construct should have caused failure");
        }
        catch (MultiException me) {
            Assert.assertTrue(me.getMessage(), me.getMessage().contains(
                    " annotated with @PostConstruct must not have any arguments"));
        }
    }
    
    /**
     * This post construct has a bad parameter
     */
    @Test
    public void testInvalidPreDestroy() {
        BadPD badPD = new BadPD();
        
        try {
            locator.preDestroy(badPD);
            Assert.fail("This pre destroy should have caused failure");
        }
        catch (MultiException me) {
            Assert.assertTrue(me.getMessage(), me.getMessage().contains(
                    " annotated with @PreDestroy must not have any arguments"));
        }
    }
    
    /**
     * Tests that a service that tries to inject an unknown service will get
     * the proper failure
     */
    @Test
    public void testInvalidInjectionPoint() {
        InjectsAClassThatIsNotAService iactinas = locator.create(InjectsAClassThatIsNotAService.class);
        Assert.assertNotNull(iactinas);
        
        Injectee badInjectee = null;
        try {
            locator.inject(iactinas);
            Assert.fail("This injection should have failed");
        }
        catch (MultiException me) {
            for (Throwable th : me.getErrors()) {
                if (th instanceof UnsatisfiedDependencyException) {
                    Assert.assertNull(badInjectee);  // Should only be one of these
                    
                    UnsatisfiedDependencyException ude = (UnsatisfiedDependencyException) th;
                    
                    badInjectee = ude.getInjectee();
                }
            }
        }
        
        Assert.assertNotNull(badInjectee);
        
        // OK, lets do a check of its fields
        Assert.assertEquals(NotAService.class, badInjectee.getRequiredType());
        Assert.assertTrue(badInjectee.getRequiredQualifiers().isEmpty());
        Assert.assertSame(-1, badInjectee.getPosition());
        Assert.assertEquals(InjectsAClassThatIsNotAService.class, badInjectee.getInjecteeClass());
        Assert.assertTrue(badInjectee.getParent() instanceof Field);
        Assert.assertFalse(badInjectee.isOptional());
        Assert.assertFalse(badInjectee.isSelf());
        Assert.assertNull(badInjectee.getUnqualified());
    }

}
