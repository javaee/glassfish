/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
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
package org.glassfish.hk2.xml.integration.test.tests;

import java.util.List;
import java.util.Map;

import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.xml.api.XmlRootHandle;
import org.glassfish.hk2.xml.api.XmlService;
import org.glassfish.hk2.xml.integration.test.LeafBean;
import org.glassfish.hk2.xml.integration.test.RootBean;
import org.glassfish.hk2.xml.integration.test.utilities.IntegrationTestUtilities;
import org.junit.Assert;
import org.junit.Test;

public class XmlIntegrationTest {
    private static final String NAME_KEY = "name";
    
    private static final String ALICE = "Alice";
    private static final String HATTER = "Hatter";
    
    /**
     * Tests integrating a child using a bean like map
     */
    @Test
    public void testHk2IntegrationMap() {
        ServiceLocator locator = IntegrationTestUtilities.createDomLocator(LeafMapService.class);
        
        createBenchmarkRoot(locator);
        
        List<LeafMapService> leaves = locator.getAllServices(LeafMapService.class);
        Assert.assertEquals(2, leaves.size());
        
        {
            LeafMapService aliceLeaf = leaves.get(0);
            Map<String, Object> aliceBean = aliceLeaf.getLeafAsMap();
            Assert.assertNotNull(aliceBean);
            Assert.assertEquals(ALICE, aliceBean.get(NAME_KEY));
        }
        
        {
            LeafMapService hatterLeaf = leaves.get(1);
            Map<String, Object> hatterBean = hatterLeaf.getLeafAsMap();
            Assert.assertNotNull(hatterBean);
            Assert.assertEquals(HATTER, hatterBean.get(NAME_KEY));
        }
        
    }
    
    /**
     * Tests integrating a child using a bean
     */
    @Test
    // @org.junit.Ignore
    public void testHk2IntegrationBean() {
        ServiceLocator locator = IntegrationTestUtilities.createLocator(LeafBeanService.class);
        
        createBenchmarkRoot(locator);
        
        List<LeafBeanService> leaves = locator.getAllServices(LeafBeanService.class);
        Assert.assertEquals(2, leaves.size());
        
        {
            LeafBeanService aliceLeaf = leaves.get(0);
            LeafBean aliceBean = aliceLeaf.getBeanAsBean();
            Assert.assertNotNull(aliceBean);
            Assert.assertEquals(ALICE, aliceBean.getName());
        }
        
        {
            LeafBeanService hatterLeaf = leaves.get(1);
            LeafBean hatterBean = hatterLeaf.getBeanAsBean();
            Assert.assertNotNull(hatterBean);
            Assert.assertEquals(HATTER, hatterBean.getName());
        }
        
    }
    
    private XmlRootHandle<RootBean> createBenchmarkRoot(ServiceLocator locator) {
        XmlService xmlService = locator.getService(XmlService.class);
        XmlRootHandle<RootBean> retVal = xmlService.createEmptyHandle(RootBean.class);
        
        retVal.addRoot();
        RootBean root = retVal.getRoot();
        
        root.addLeave(ALICE);
        root.addLeave(HATTER);
        
        return retVal;
    }
}
