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
package org.glassfish.hk2.tests.locator.types;

import java.util.List;
import java.util.Map;

import javax.inject.Singleton;

import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.Descriptor;
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
public class TypesTest {
    public static final int INTEGER_FACTORY_VALUE = 13;
    public static final float FLOAT_KEY = (float) 14.00;
    public static final Double DOUBLE_VALUE = new Double(15.00);
    public static final String BEST_TEAM = "Eagles";
    public static final long LONG_FACTORY_VALUE = 16L;
    
    /**
     * FullService extends a typed abstract class, but is itself a
     * fully qualified version of that interface (String, String).
     * Hence, it should be injectable into InjectedService
     */
    @Test // @org.junit.Ignore
    public void testAbstractSuperclass() {
        ServiceLocator locator = LocatorHelper.create();
        
        ServiceLocatorUtilities.addClasses(locator,
                FullService.class, InjectedService.class);
        
        InjectedService is = locator.getService(InjectedService.class);
        Assert.assertNotNull(is);
        Assert.assertNotNull(is.getInjectedService());
    }
    
    /**
     * FullService extends a typed abstract class, but is itself a
     * fully qualified version of that interface (String, String).
     * Hence, it should be injectable into InjectedService
     */
    @Test // @org.junit.Ignore
    public void testAbstractSuperclassFromBasicDescriptor() {
        ServiceLocator locator = LocatorHelper.create();
        
        ServiceLocatorUtilities.addClasses(locator,
                InjectedService.class);
        
        Descriptor addMe = BuilderHelper.link(FullService.class.getName())
            .to(ServiceInterface.class.getName())
            .in(Singleton.class.getName()).build();
        
        ActiveDescriptor<?> added = ServiceLocatorUtilities.addOneDescriptor(locator, addMe);
        
        added = locator.reifyDescriptor(added);
        
        InjectedService is = locator.getService(InjectedService.class);
        Assert.assertNotNull(is);
        Assert.assertNotNull(is.getInjectedService());
    }
    
    /**
     * InjectedBaseClass has injected types that are fully specified as classes by the subclass
     */
    @Test // @org.junit.Ignore
    public void testSuperclassHasTypeInjectees() {
        ServiceLocator locator = LocatorHelper.getServiceLocator(AlphaService.class,
                BetaService.class,
                AlphaInjectedService.class,
                BetaInjectedService.class);
        
        {
            AlphaInjectedService ais = locator.getService(AlphaInjectedService.class);
        
            Assert.assertNotNull(ais.getFromConstructor());
            Assert.assertTrue(ais.getFromConstructor() instanceof AlphaService);
        
            Assert.assertNotNull(ais.getFromField());
            Assert.assertTrue(ais.getFromField() instanceof AlphaService);
            
            Assert.assertNotNull(ais.getFromMethod());
            Assert.assertTrue(ais.getFromMethod() instanceof AlphaService);
        }
        
        {
            BetaInjectedService bis = locator.getService(BetaInjectedService.class);
        
            Assert.assertNotNull(bis.getFromConstructor());
            Assert.assertTrue(bis.getFromConstructor() instanceof BetaService);
        
            Assert.assertNotNull(bis.getFromField());
            Assert.assertTrue(bis.getFromField() instanceof BetaService);
            
            Assert.assertNotNull(bis.getFromMethod());
            Assert.assertTrue(bis.getFromMethod() instanceof BetaService);
        }
        
    }
    
    /**
     * Tests that services can have parameterized types all filled in
     * by the subclasses
     */
    @Test // @org.junit.Ignore
    public void testHardenedParameterizedTypes() {
        ServiceLocator locator = LocatorHelper.getServiceLocator(
                ListMapServiceIntIntLong.class,
                ListMapServiceStringFloatDouble.class,
                IntLongMapFactory.class,
                FloatDoubleMapFactory.class,
                ListIntFactory.class,
                ListStringFactory.class);
        
        {
            ListMapServiceIntIntLong iil = locator.getService(ListMapServiceIntIntLong.class);
        
            List<Integer> aList = iil.getAList();
            Assert.assertNotNull(aList);
            
            Map<Integer, Long> aMap = iil.getAMap();
            Assert.assertNotNull(aMap);
            
            Assert.assertEquals(aList, iil.getIList());
            Assert.assertEquals(aMap, iil.getIMap());
            
            int fromList = aList.get(0);
            Assert.assertEquals(INTEGER_FACTORY_VALUE, fromList);
            
            long fromMap = aMap.get(INTEGER_FACTORY_VALUE);
            Assert.assertEquals(LONG_FACTORY_VALUE, fromMap);
        }
        
        {
            ListMapServiceStringFloatDouble sfd = locator.getService(ListMapServiceStringFloatDouble.class);
        
            List<String> aList = sfd.getAList();
            Assert.assertNotNull(aList);
            
            Map<Float, Double> aMap = sfd.getAMap();
            Assert.assertNotNull(aMap);
            
            Assert.assertEquals(aList, sfd.getIList());
            Assert.assertEquals(aMap, sfd.getIMap());
            
            String fromList = aList.get(0);
            Assert.assertEquals(BEST_TEAM, fromList);
            
            Double fromMap = aMap.get(FLOAT_KEY);
            Assert.assertEquals(DOUBLE_VALUE, fromMap);
        }
    }

}
