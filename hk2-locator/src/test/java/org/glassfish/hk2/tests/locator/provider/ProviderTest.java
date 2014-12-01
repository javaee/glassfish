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
package org.glassfish.hk2.tests.locator.provider;

import javax.inject.Provider;

import junit.framework.Assert;

import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.Injectee;
import org.glassfish.hk2.api.IterableProvider;
import org.glassfish.hk2.api.ServiceHandle;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.api.TypeLiteral;
import org.glassfish.hk2.tests.locator.utilities.LocatorHelper;
import org.glassfish.hk2.utilities.BuilderHelper;
import org.junit.Test;

/**
 * @author jwells
 *
 */
public class ProviderTest {
    /** Big Whale */
    public final static String MOBY_DICK = "Moby Dick";
    /** probably not his real name */
    public final static String ISHMAEL = "Ishmael";
    /** Escape on his coffin */
    public final static String QUEEQUEG = "QueeQueg";
    
    /** Epic team */
    public final static String EAGLES = "Eagles";
    /** Best back in the NFL */
    public final static String SHADY = "LeShaun McCoy";
    
    /** Epic FAIL */
    public final static String GIANTS = "Giants";
    /** Jerk */
    public final static String ELI = "Eli Manning";
    
            
    private final static String TEST_NAME = "ProviderTest";
    private final static ServiceLocator locator = LocatorHelper.create(TEST_NAME, new ProviderModule());

    /**
     * Tests a simple provider works properly
     */
    @Test
    public void testSimpleProvider() {
        ProviderInjected pi = locator.getService(ProviderInjected.class);
        Assert.assertNotNull(pi);
        
        Assert.assertFalse(InstantiationChecker.getIsInstantiated());
        
        pi.doTheGet();
        
        Assert.assertTrue(InstantiationChecker.getIsInstantiated());
    }
    
    /**
     * Tests an iterable with one backing item
     */
    @Test
    public void testSingleShotIterable() {
        Menagerie zoo = locator.getService(Menagerie.class);
        Assert.assertNotNull(zoo);
        
        zoo.validateAllEagles();
    }
    
    /**
     * Tests a qualified iterable
     */
    @Test
    public void testIterableQualifiedBy() {
        Menagerie zoo = locator.getService(Menagerie.class);
        Assert.assertNotNull(zoo);
        
        zoo.validateAllGiants();
    }
    
    /**
     * Tests handle iterable as an iterable
     */
    @Test
    public void testIterableOfIterableAndHandleIterable() {
        Menagerie zoo = locator.getService(Menagerie.class);
        Assert.assertNotNull(zoo);
        
        zoo.validateQueequeg();
    }
    
    /**
     * Test scoped by type
     */
    @Test
    public void testIterableOfType() {
        Menagerie zoo = locator.getService(Menagerie.class);
        Assert.assertNotNull(zoo);
        
        zoo.validateBookCharacters();
        
    }
    
    /**
     * Test scoped by name
     */
    @Test
    public void testIterableNamed() {
        Menagerie zoo = locator.getService(Menagerie.class);
        Assert.assertNotNull(zoo);
        
        zoo.validateAllCharacters();
        
    }
    
    /**
     * Tests that the descriptor returned from the injectee
     * of something that injects Provider can be used
     * in a getService call later
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testInjecteeOfAProvider() {
        ActiveDescriptor<?> parentDescriptor = locator.getBestDescriptor(BuilderHelper.createContractFilter(ProviderInjectedPerLookup.class.getName()));
        Assert.assertNotNull(parentDescriptor);
        
        Injectee injectee = parentDescriptor.getInjectees().get(0);
        Assert.assertNotNull(injectee);
        
        ActiveDescriptor<?> childDescriptor = locator.getInjecteeDescriptor(injectee);
        Assert.assertNotNull(childDescriptor);
        
        ServiceHandle<?> handle = locator.getServiceHandle(childDescriptor, injectee);
        Assert.assertNotNull(handle);
        
        Object result = handle.getService();
        Assert.assertTrue(result instanceof Provider);
        
        Provider<PerLookupService> provider0 = (Provider<PerLookupService>) result;
        Assert.assertNotNull(provider0.get());
        
        Provider<PerLookupService> provider = (Provider<PerLookupService>) locator.getService(childDescriptor, null, injectee);
        Assert.assertNotNull(provider);
        
        Assert.assertNotNull(provider.get());
    }
    
    /**
     * Tests that a Provider can be looked up from the normal getService API
     */
    @Test // @org.junit.Ignore
    public void testServiceLookupOfProvider() {
        Provider<PerLookupService> provider = locator.getService((new TypeLiteral<Provider<PerLookupService>>() {}).getType());
        Assert.assertNotNull(provider);
        Assert.assertNotNull(provider.get());
        Assert.assertTrue(provider.get() instanceof PerLookupService);
    }
    
    /**
     * Tests that a IterableProvider can be looked up from the normal getService API
     */
    @Test // @org.junit.Ignore
    public void testServiceLookupOfIterableProvider() {
        IterableProvider<FootballCharacter> provider = locator.getService((new TypeLiteral<IterableProvider<FootballCharacter>>() {}).getType());
        Assert.assertNotNull(provider);
        Assert.assertEquals(2, provider.getSize());
        
        boolean foundEli = false;
        boolean foundShady = false;
        for (FootballCharacter fc : provider) {
            Assert.assertTrue(fc instanceof FootballCharacter);
            
            if (fc instanceof EliManning) {
                foundEli = true;
            }
            if (fc instanceof ShadyMcCoy) {
                foundShady = true;
            }
        }
        
        Assert.assertTrue(foundEli);
        Assert.assertTrue(foundShady);
    }
}
