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
import java.util.Map;

import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.configuration.hub.api.BeanDatabase;
import org.glassfish.hk2.configuration.hub.api.Hub;
import org.glassfish.hk2.configuration.hub.api.Instance;
import org.glassfish.hk2.configuration.hub.api.Type;
import org.glassfish.hk2.xml.api.XmlHk2ConfigurationBean;
import org.glassfish.hk2.xml.api.XmlRootHandle;
import org.glassfish.hk2.xml.api.XmlService;
import org.glassfish.hk2.xml.test.elements.beans.BasicElementalBean;
import org.glassfish.hk2.xml.test.elements.beans.EarthBean;
import org.glassfish.hk2.xml.test.elements.beans.ElementType;
import org.glassfish.hk2.xml.test.elements.beans.ElementalBean;
import org.glassfish.hk2.xml.test.elements.beans.FireBean;
import org.glassfish.hk2.xml.test.elements.beans.WaterBean;
import org.glassfish.hk2.xml.test.elements.beans.WindBean;
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
    private final static String BENDER_NONE = "CabbageVendor";
    
    private final static String ONE_OF_EACH = "elemental/oneofeach.xml";
    
    private final static String EARTH_TYPE = "/basic-elements/earth";
    private final static String WIND_TYPE = "/basic-elements/wind";
    private final static String WATER_TYPE = "/basic-elements/water";
    private final static String FIRE_TYPE = "/basic-elements/fire";
    private final static String NONE_TYPE = "/basic-elements/none";
    
    private final static String EARTH_INSTANCE = "basic-elements.Toph";
    private final static String WIND_INSTANCE = "basic-elements.Ang";
    private final static String WATER_INSTANCE = "basic-elements.Kitara";
    private final static String FIRE_INSTANCE = "basic-elements.Zuko";
    private final static String NONE_INSTANCE = "basic-elements.CabbageVendor";
    
    public static void testReadOneOfEachElement(ServiceLocator locator, ClassLoader cl) throws Exception {
        XmlService xmlService = locator.getService(XmlService.class);
        
        URL url = cl.getResource(ONE_OF_EACH);
        URI uri = url.toURI();
        
        XmlRootHandle<BasicElementalBean> handle = xmlService.unmarshal(uri, BasicElementalBean.class, true, true);
        BasicElementalBean root = handle.getRoot();
        Assert.assertNotNull(root);
        
        List<ElementalBean> elementals = root.getEarthWindAndFire();
        Assert.assertEquals("elements found =" + elementals, 5, elementals.size());
        
        EarthBean earth = (EarthBean) elementals.get(0);
        WindBean wind = (WindBean) elementals.get(1);
        WaterBean water = (WaterBean) elementals.get(2);
        FireBean fire = (FireBean) elementals.get(3);
        ElementalBean none = (ElementalBean) elementals.get(4);
        
        Assert.assertEquals(BENDER_EARTH, earth.getName());
        Assert.assertEquals(BENDER_WIND, wind.getName());
        Assert.assertEquals(BENDER_WATER, water.getName());
        Assert.assertEquals(BENDER_FIRE, fire.getName());
        Assert.assertEquals(BENDER_NONE, none.getName());
        
        Assert.assertEquals(ElementType.EARTH, earth.getType());
        Assert.assertEquals(ElementType.WIND, wind.getType());
        Assert.assertEquals(ElementType.WATER, water.getType());
        Assert.assertEquals(ElementType.FIRE, fire.getType());
        Assert.assertEquals(ElementType.NONE, none.getType());
        
        Assert.assertEquals(EARTH_TYPE, ((XmlHk2ConfigurationBean) earth)._getXmlPath());
        Assert.assertEquals(WIND_TYPE, ((XmlHk2ConfigurationBean) wind)._getXmlPath());
        Assert.assertEquals(WATER_TYPE, ((XmlHk2ConfigurationBean) water)._getXmlPath());
        Assert.assertEquals(FIRE_TYPE, ((XmlHk2ConfigurationBean) fire)._getXmlPath());
        Assert.assertEquals(NONE_TYPE, ((XmlHk2ConfigurationBean) none)._getXmlPath());
        
        Hub hub = locator.getService(Hub.class);
        
        checkNamedInHub(hub, EARTH_TYPE, EARTH_INSTANCE, BENDER_EARTH);
        checkNamedInHub(hub, WIND_TYPE, WIND_INSTANCE, BENDER_WIND);
        checkNamedInHub(hub, WATER_TYPE, WATER_INSTANCE, BENDER_WATER);
        checkNamedInHub(hub, FIRE_TYPE, FIRE_INSTANCE, BENDER_FIRE);
        checkNamedInHub(hub, NONE_TYPE, NONE_INSTANCE, BENDER_NONE);
        
        hub.getCurrentDatabase().dumpDatabase(System.out);
    }
    
    @SuppressWarnings("unchecked")
    private static void checkNamedInHub(Hub hub, String type, String instance, String name) {
        BeanDatabase bd = hub.getCurrentDatabase();
        
        Type hubType = bd.getType(type);
        Assert.assertNotNull(hubType);
        
        Instance i = null;
        if (instance.contains(".*")) {
            Map<String, Instance> instances = hubType.getInstances();
            Assert.assertEquals(1, instances.size());
            
            for (Instance found : instances.values()) {
                i = found;
            }
        }
        else {
            i = hubType.getInstance(instance);
        }
        
        Assert.assertNotNull("Could not find instance " + instance + " in type " + type, i);
        
        Object bean = i.getBean();
        Assert.assertNotNull(bean);
        
        String beanName = ((Map<String, String>) bean).get("name");
        Assert.assertEquals(name, beanName);
    }

}
