/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved.
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
package org.glassfish.hk2.xml.test.childlocators;

import java.net.URL;
import java.util.List;

import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.configuration.hub.api.Hub;
import org.glassfish.hk2.xml.api.XmlRootHandle;
import org.glassfish.hk2.xml.api.XmlService;
import org.glassfish.hk2.xml.test.beans.DomainBean;
import org.glassfish.hk2.xml.test.dynamic.merge.MergeTest;
import org.glassfish.hk2.xml.test.utilities.Utilities;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author jwells
 *
 */
public class ChildLocatorTest {
    /**
     * Makes sure children do not see parent XmlService and hub
     */
    @Test
    public void testChildrenDoNotSeeParentServices() {
        ServiceLocator parentLocator = Utilities.createDomLocator();
        ServiceLocator childALocator = Utilities.createDomLocator(parentLocator);
        ServiceLocator childBLocator = Utilities.createDomLocator(parentLocator);
        ServiceLocator grandChildALocator = Utilities.createDomLocator(childALocator);
        
        List<XmlService> allXmlServices = grandChildALocator.getAllServices(XmlService.class);
        Assert.assertEquals(1, allXmlServices.size());
        XmlService grandChildAXmlService = allXmlServices.get(0);
        
        allXmlServices = childALocator.getAllServices(XmlService.class);
        Assert.assertEquals(1, allXmlServices.size());
        XmlService childAXmlService = allXmlServices.get(0);
        
        allXmlServices = parentLocator.getAllServices(XmlService.class);
        Assert.assertEquals(1, allXmlServices.size());
        XmlService parentXmlService = allXmlServices.get(0);
        
        allXmlServices = childBLocator.getAllServices(XmlService.class);
        Assert.assertEquals(1, allXmlServices.size());
        XmlService childBXmlService = allXmlServices.get(0);
        
        Assert.assertNotEquals(grandChildAXmlService, childAXmlService);
        Assert.assertNotEquals(grandChildAXmlService, parentXmlService);
        Assert.assertNotEquals(grandChildAXmlService, childBXmlService);
        Assert.assertNotEquals(childAXmlService, parentXmlService);
        Assert.assertNotEquals(childAXmlService, childBXmlService);
        Assert.assertNotEquals(parentXmlService, childBXmlService);
        
        // Now check the Hub
        List<Hub> allHubServices = grandChildALocator.getAllServices(Hub.class);
        Assert.assertEquals(1, allHubServices.size());
        Hub grandChildAHub = allHubServices.get(0);
        
        allHubServices = childALocator.getAllServices(Hub.class);
        Assert.assertEquals(1, allHubServices.size());
        Hub childAHub = allHubServices.get(0);
        
        allHubServices = parentLocator.getAllServices(Hub.class);
        Assert.assertEquals(1, allHubServices.size());
        Hub parentHub = allHubServices.get(0);
        
        allHubServices = childBLocator.getAllServices(Hub.class);
        Assert.assertEquals(1, allHubServices.size());
        Hub childBHub = allHubServices.get(0);
        
        Assert.assertNotEquals(grandChildAHub, childAHub);
        Assert.assertNotEquals(grandChildAHub, parentHub);
        Assert.assertNotEquals(grandChildAHub, childBHub);
        Assert.assertNotEquals(childAHub, parentHub);
        Assert.assertNotEquals(childAHub, childBHub);
        Assert.assertNotEquals(parentHub, childBHub);
    }
    
    /**
     * One parent, one child, both with XmlService started.
     * Only read document in child, ensure it is there in
     * child but not there in parent
     */
    @Test
    public void testReadingInChildOnlyWorks() throws Exception {
        ServiceLocator parentLocator = Utilities.createDomLocator();
        ServiceLocator childLocator = Utilities.createDomLocator(parentLocator);
        
        XmlService childXmlService = childLocator.getService(XmlService.class);
        Hub childHub = childLocator.getService(Hub.class);
        
        URL url = getClass().getClassLoader().getResource(MergeTest.DOMAIN1_FILE);
        
        XmlRootHandle<DomainBean> rootHandle = childXmlService.unmarshall(url.toURI(), DomainBean.class);
        
        Hub parentHub = parentLocator.getService(Hub.class);
        
        MergeTest.verifyDomain1Xml(rootHandle, childHub, childLocator);
        MergeTest.verifyDomain1XmlDomainNotThere(parentHub, parentLocator);
    }
    
    /**
     * One parent, one child, both with XmlService started.
     * Read in both child and parent, verify expected
     * services were found
     */
    @Test
    public void testReadingInChildAndParentWorks() throws Exception {
        ServiceLocator parentLocator = Utilities.createDomLocator();
        ServiceLocator childLocator = Utilities.createDomLocator(parentLocator);
        
        XmlService childXmlService = childLocator.getService(XmlService.class);
        XmlService parentXmlService = parentLocator.getService(XmlService.class);
        
        Hub childHub = childLocator.getService(Hub.class);
        Hub parentHub = parentLocator.getService(Hub.class);
        
        URL url = getClass().getClassLoader().getResource(MergeTest.DOMAIN1_FILE);
        
        XmlRootHandle<DomainBean> childHandle = childXmlService.unmarshall(url.toURI(), DomainBean.class);
        XmlRootHandle<DomainBean> parentHandle = parentXmlService.unmarshall(url.toURI(), DomainBean.class);
        
        MergeTest.verifyDomain1Xml(parentHandle, parentHub, parentLocator);
        MergeTest.verifyDomain1Xml(childHandle, childHub, childLocator);
    }
    
    /**
     * One parent, one child, both with XmlService started.
     * Only read document in parent, ensure the services are
     * in the child, but nothing else
     */
    @Test
    public void testReadingInParentOnlyWorks() throws Exception {
        ServiceLocator parentLocator = Utilities.createDomLocator();
        ServiceLocator childLocator = Utilities.createDomLocator(parentLocator);
        
        XmlService parentXmlService = parentLocator.getService(XmlService.class);
        
        Hub childHub = childLocator.getService(Hub.class);
        Hub parentHub = parentLocator.getService(Hub.class);
        
        URL url = getClass().getClassLoader().getResource(MergeTest.DOMAIN1_FILE);
        
        XmlRootHandle<DomainBean> parentHandle = parentXmlService.unmarshall(url.toURI(), DomainBean.class);
        
        MergeTest.verifyDomain1Xml(parentHandle, parentHub, parentLocator);
        MergeTest.assertDomain1Services(childLocator, parentLocator, false);
        
        MergeTest.verifyDomain1XmlDomainNotThere(childHub, childLocator);
    }

}
