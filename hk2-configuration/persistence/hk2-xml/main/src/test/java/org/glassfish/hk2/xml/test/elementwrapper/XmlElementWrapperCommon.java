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
package org.glassfish.hk2.xml.test.elementwrapper;

import java.net.URI;
import java.net.URL;
import java.util.List;

import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.xml.api.XmlHk2ConfigurationBean;
import org.glassfish.hk2.xml.api.XmlRootHandle;
import org.glassfish.hk2.xml.api.XmlService;
import org.glassfish.hk2.xml.test.elementwrapper.beans.WrapperLeafBean;
import org.glassfish.hk2.xml.test.elementwrapper.beans.WrapperMiddleBean;
import org.glassfish.hk2.xml.test.elementwrapper.beans.WrapperRootBean;
import org.junit.Assert;

/**
 * @author jwells
 *
 */
public class XmlElementWrapperCommon {
    private final static String WRAPPER_FILE1 = "elementwrapper/ElementWrapper1.xml";
    
    private final static String ALICE = "Alice";
    private final static String BOB = "Bob";
    
    public static void testReadOneOfEachElement(ServiceLocator locator, ClassLoader cl) throws Exception {
        XmlService xmlService = locator.getService(XmlService.class);
        
        URL url = cl.getResource(WRAPPER_FILE1);
        URI uri = url.toURI();
        
        XmlRootHandle<WrapperRootBean> handle = xmlService.unmarshal(uri, WrapperRootBean.class, true, true);
        WrapperRootBean root = handle.getRoot();
        Assert.assertNotNull(root);
        
        List<WrapperMiddleBean> middles = root.getMiddle();
        Assert.assertEquals(1, middles.size());
        
        WrapperMiddleBean middle = middles.get(0);
        
        Assert.assertEquals(ALICE, middle.getName());
        XmlHk2ConfigurationBean middleConfigBean = (XmlHk2ConfigurationBean) middle;
        
        Assert.assertEquals("/wrapper-root/middles/middle", middleConfigBean._getXmlPath());
        Assert.assertEquals("wrapper-root.middles.Alice", middleConfigBean._getInstanceName());
        Assert.assertEquals(ALICE, middleConfigBean._getKeyValue());
        
        List<WrapperLeafBean> leaves = middle.getLeaves();
        Assert.assertEquals(1, leaves.size());
        
        WrapperLeafBean leaf = leaves.get(0);
        
        Assert.assertEquals(BOB, leaf.getData());
        XmlHk2ConfigurationBean leafConfigBean = (XmlHk2ConfigurationBean) leaf;
        
        Assert.assertEquals("/wrapper-root/middles/middle/leaves/leaf", leafConfigBean._getXmlPath());
        Assert.assertTrue(leafConfigBean._getInstanceName().startsWith("wrapper-root.middles.Alice.leaves."));
        Assert.assertNull(leafConfigBean._getKeyValue());
    }
}
