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

package org.glassfish.hk2.tests.locator.proxiable;

import org.junit.Assert;

import org.glassfish.hk2.api.ProxyCtl;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.tests.locator.utilities.LocatorHelper;
import org.junit.Test;

/**
 * @author jwells
 *
 */
public class ProxiableTest {
    private final static String TEST_NAME = "ProxiableTest";
    private final static ServiceLocator locator = LocatorHelper.create(TEST_NAME, new ProxiableModule());
    
    /** Many flowers */
    public final static String SPRING = "Spring";
    /** Beach time! */
    public final static String SUMMER = "Summer";
    /** Colorful leaves */
    public final static String FALL = "Fall";
    /** Snowstorms! */
    public final static String WINTER = "Winter";

    /**
     * This test proves that the underlying services are proxied because
     * there is a cycle (spring -> summer -> fall -> winter).  If these
     * were not proxied an infinite stack would occur
     */
    @Test
    public void testSeasonCycle() {
        Winter winter = locator.getService(Winter.class);
        Assert.assertNotNull(winter);
        Assert.assertEquals(WINTER, winter.getName());
        
        Season spring = winter.getNextSeason();
        Assert.assertNotNull(spring);
        Assert.assertEquals(SPRING, spring.getName());
        
        Season summer = spring.getNextSeason();
        Assert.assertNotNull(summer);
        Assert.assertEquals(SUMMER, summer.getName());
        
        Season fall = summer.getNextSeason();
        Assert.assertNotNull(fall);
        Assert.assertEquals(FALL, fall.getName());
        
        Season winter2 = fall.getNextSeason();
        Assert.assertNotNull(winter2);
        Assert.assertEquals(WINTER, winter2.getName());
    }
    
    /**
     * Tests the ProxyCtl interface of proxies
     */
    @Test
    public void testProxyCtl() {
        PostConstructedProxiedService pcps = locator.getService(PostConstructedProxiedService.class);
        Assert.assertNotNull(pcps);
        
        Assert.assertTrue(pcps instanceof ProxyCtl);
        
        ProxyCtl pc = (ProxyCtl) pcps;
        
        Assert.assertFalse(PostConstructedProxiedService.wasPostConstructCalled());
        
        Object o = pc.__make();
        
        Assert.assertNotNull(o);
        Assert.assertTrue(o instanceof PostConstructedProxiedService);
        
        Assert.assertTrue(PostConstructedProxiedService.wasPostConstructCalled());
        
    }
    
    /**
     * Tests method access levels in proxies
     */
    @Test @org.junit.Ignore
    public void testMethodAccessInAProxy() {
    	SouthernHemisphere sh = locator.getService(SouthernHemisphere.class);
    	
    	// If this doesn't bomb, this test works
    	sh.check();
    }
}
