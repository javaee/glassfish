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
package org.glassfish.hk2.xml.test.elements;

import java.net.URI;
import java.net.URL;
import java.util.List;

import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.xml.api.XmlRootHandle;
import org.glassfish.hk2.xml.api.XmlService;
import org.glassfish.hk2.xml.test.elements.beans.BasicElementalBean;
import org.glassfish.hk2.xml.test.elements.beans.ElementType;
import org.glassfish.hk2.xml.test.elements.beans.ElementalBean;
import org.junit.Assert;

/**
 * @author jwells
 *
 */
public class ElementsCommon {
    private final static String BENDER_EARTH = "Toph";
    private final static String BENDER_WIND = "Ang";
    private final static String BENDER_WATER = "Kitara";
    private final static String BENDER_FIRE = "Zuko";
    
    private final static String ONE_OF_EACH = "elemental/oneofeach.xml";
    
    public static void testReadOneOfEachElement(ServiceLocator locator, ClassLoader cl) throws Exception {
        XmlService xmlService = locator.getService(XmlService.class);
        
        URL url = cl.getResource(ONE_OF_EACH);
        URI uri = url.toURI();
        
        XmlRootHandle<BasicElementalBean> handle = xmlService.unmarshal(uri, BasicElementalBean.class, false, false);
        BasicElementalBean root = handle.getRoot();
        Assert.assertNotNull(root);
        
        List<ElementalBean> elementals = root.getEarthWindAndFire();
        Assert.assertEquals(4, elementals.size());
        
        ElementalBean earth = elementals.get(0);
        ElementalBean wind = elementals.get(1);
        ElementalBean water = elementals.get(2);
        ElementalBean fire = elementals.get(3);
        
        Assert.assertEquals(BENDER_EARTH, earth.getName());
        Assert.assertEquals(BENDER_WIND, wind.getName());
        Assert.assertEquals(BENDER_WATER, water.getName());
        Assert.assertEquals(BENDER_FIRE, fire.getName());
        
        Assert.assertEquals(ElementType.EARTH, earth.getType());
        Assert.assertEquals(ElementType.WIND, wind.getType());
        Assert.assertEquals(ElementType.WATER, water.getType());
        Assert.assertEquals(ElementType.FIRE, fire.getType());
    }

}
