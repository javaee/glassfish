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
package org.glassfish.hk2.xml.test.negative.ea;

import java.net.URI;
import java.net.URL;
import java.util.List;

import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.xml.api.XmlRootHandle;
import org.glassfish.hk2.xml.api.XmlService;
import org.junit.Assert;

/**
 * @author jwells
 *
 */
public class ElementAttributeCommon {
    private final static String GOOD_FILE = "ea/ea1.xml";
    private final static String WHOLLY_MIXED_FILE = "ea/ea2.xml";
    private final static String BOTH_ATTRIBUTES_FILE = "ea/ea3.xml";
    private final static String BOTH_ELEMENTS_FILE = "ea/ea4.xml";
    
    private final static String ATTRIBUTE_VALUE = "attribute";
    private final static String ELEMENT_VALUE = "element";
    
    public static void testReadGoodFile(ServiceLocator locator, ClassLoader cl) throws Exception {
        XmlService xmlService = locator.getService(XmlService.class);
        
        URL url = cl.getResource(GOOD_FILE);
        URI uri = url.toURI();
        
        XmlRootHandle<ElementAttributeRoot> handle = xmlService.unmarshal(uri, ElementAttributeRoot.class, false, false);
        ElementAttributeRoot root = handle.getRoot();
        Assert.assertNotNull(root);
        
        List<ElementAttributeLeaf> leaves = root.getLeaves();
        Assert.assertEquals(1, leaves.size());
        
        ElementAttributeLeaf leaf = leaves.get(0);
        
        Assert.assertEquals(ATTRIBUTE_VALUE, leaf.getAttribute());
        Assert.assertEquals(ELEMENT_VALUE, leaf.getElement());
    }
    
    public static void testReadWhollyMixedFile(ServiceLocator locator, ClassLoader cl) throws Exception {
        XmlService xmlService = locator.getService(XmlService.class);
        
        URL url = cl.getResource(WHOLLY_MIXED_FILE);
        URI uri = url.toURI();
        
        XmlRootHandle<ElementAttributeRoot> handle = xmlService.unmarshal(uri, ElementAttributeRoot.class, false, false);
        ElementAttributeRoot root = handle.getRoot();
        Assert.assertNotNull(root);
        
        List<ElementAttributeLeaf> leaves = root.getLeaves();
        Assert.assertEquals(1, leaves.size());
        
        ElementAttributeLeaf leaf = leaves.get(0);
        
        Assert.assertNull(leaf.getAttribute());
        Assert.assertNull(leaf.getElement());
    }
    
    public static void testReadBothAttributes(ServiceLocator locator, ClassLoader cl) throws Exception {
        XmlService xmlService = locator.getService(XmlService.class);
        
        URL url = cl.getResource(BOTH_ATTRIBUTES_FILE);
        URI uri = url.toURI();
        
        XmlRootHandle<ElementAttributeRoot> handle = xmlService.unmarshal(uri, ElementAttributeRoot.class, false, false);
        ElementAttributeRoot root = handle.getRoot();
        Assert.assertNotNull(root);
        
        List<ElementAttributeLeaf> leaves = root.getLeaves();
        Assert.assertEquals(1, leaves.size());
        
        ElementAttributeLeaf leaf = leaves.get(0);
        
        Assert.assertEquals(ATTRIBUTE_VALUE, leaf.getAttribute());
        Assert.assertNull(leaf.getElement());
    }
    
    public static void testReadBothElements(ServiceLocator locator, ClassLoader cl) throws Exception {
        XmlService xmlService = locator.getService(XmlService.class);
        
        URL url = cl.getResource(BOTH_ELEMENTS_FILE);
        URI uri = url.toURI();
        
        XmlRootHandle<ElementAttributeRoot> handle = xmlService.unmarshal(uri, ElementAttributeRoot.class, false, false);
        ElementAttributeRoot root = handle.getRoot();
        Assert.assertNotNull(root);
        
        List<ElementAttributeLeaf> leaves = root.getLeaves();
        Assert.assertEquals(1, leaves.size());
        
        ElementAttributeLeaf leaf = leaves.get(0);
        
        Assert.assertNull(leaf.getAttribute());
        Assert.assertEquals(ELEMENT_VALUE, leaf.getElement());
    }

}
