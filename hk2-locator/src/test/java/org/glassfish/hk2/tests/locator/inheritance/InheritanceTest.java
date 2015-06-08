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

package org.glassfish.hk2.tests.locator.inheritance;

import java.lang.annotation.Annotation;
import java.util.Set;

import junit.framework.Assert;

import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.DynamicConfiguration;
import org.glassfish.hk2.api.DynamicConfigurationService;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.tests.locator.utilities.LocatorHelper;
import org.junit.Test;

/**
 * @author jwells
 *
 */
public class InheritanceTest {
    private final static String TEST_NAME = "InheritanceTest";
    private final static ServiceLocator locator = LocatorHelper.create(TEST_NAME, new InheritanceModule());
    
    /**
     * Tests ensures that all proper qualifiers are added to a class
     */
    @Test
    public void testQualifiersInAHierarchy() {
        DynamicConfigurationService dcs = locator.getService(DynamicConfigurationService.class);
        DynamicConfiguration config = dcs.createDynamicConfiguration();
        
        ActiveDescriptor<?> football = config.addActiveDescriptor(AmericanFootball.class);
        
        // Check the qualifiers
        Set<Annotation> qualifiers = football.getQualifierAnnotations();
        Assert.assertEquals(3, qualifiers.size());
        
        boolean foundSuperbowl = false;
        boolean foundOutdoors = false;
        boolean foundHasWinner = false;
        
        for (Annotation qualifier : qualifiers) {
            if (Superbowl.class.equals(qualifier.annotationType())) {
                foundSuperbowl = true;
            }
            else if (Outdoors.class.equals(qualifier.annotationType())) {
                foundOutdoors = true;
            }
            else if (HasWinner.class.equals(qualifier.annotationType())) {
                foundHasWinner = true;
            }
            else {
                Assert.fail("Unexpected qualifier found: " + qualifier);
            }
        }
        
        Assert.assertTrue(foundSuperbowl);
        Assert.assertTrue(foundOutdoors);
        Assert.assertTrue(foundHasWinner);
    }
    
    /**
     * Tests ensures that the non-inherited scope of Sports wipes out
     * the inherited scope of Games, leaving AmericanFootball with its
     * own declared scope of PerLookup
     */
    @Test
    public void testNonInheritedScopeWipesOutInheritedScope() {
        DynamicConfigurationService dcs = locator.getService(DynamicConfigurationService.class);
        DynamicConfiguration config = dcs.createDynamicConfiguration();
        
        ActiveDescriptor<?> football = config.addActiveDescriptor(AmericanFootball.class);
        
        Assert.assertEquals(PerLookup.class, football.getScopeAnnotation());
    }
    
    /**
     * Tests ensures that the non-inherited scope of Sports wipes out
     * the inherited scope of Games, leaving AmericanFootball with its
     * own declared scope of PerLookup
     */
    @Test
    public void testInheritedScope() {
        DynamicConfigurationService dcs = locator.getService(DynamicConfigurationService.class);
        DynamicConfiguration config = dcs.createDynamicConfiguration();
        
        ActiveDescriptor<?> chess = config.addActiveDescriptor(Chess.class);
        
        Assert.assertEquals(InheritedScope.class, chess.getScopeAnnotation());
    }
    
    /**
     * Tests ensures that the non-inherited scope of Sports wipes out
     * the inherited scope of Games, leaving AmericanFootball with its
     * own declared scope of PerLookup
     */
    @Test
    public void testFactoryInheritanceOfScope() {
        InheritedScopeContext context = locator.getService(InheritedScopeContext.class);
        Assert.assertTrue(context.getSeen().isEmpty());
        
        SimpleService ss = locator.getService(SimpleService.class);
        Assert.assertNotNull(ss);
        
        Assert.assertEquals(1, context.getSeen().size());
        
        Assert.assertEquals(ss, context.getSeen().get(0));
    }

}
