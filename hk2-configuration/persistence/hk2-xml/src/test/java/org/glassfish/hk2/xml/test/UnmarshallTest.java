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
package org.glassfish.hk2.xml.test;

import java.net.URL;

import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.xml.api.XmlRootHandle;
import org.glassfish.hk2.xml.api.XmlService;
import org.glassfish.hk2.xml.test.utilities.Utilities;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for unmarshalling xml into the hk2 hub
 * 
 * @author jwells
 */
public class UnmarshallTest {
    /**
     * Tests the most basic of xml files can be unmarshalled with an interface
     * annotated with jaxb annotations
     * 
     * @throws Exception
     */
    @Test // @org.junit.Ignore
    public void testInterfaceJaxbUnmarshalling() throws Exception {
        ServiceLocator locator = Utilities.createLocator();
        XmlService xmlService = locator.getService(XmlService.class);
        
        URL url = getClass().getClassLoader().getResource("museum1.xml");
        
        XmlRootHandle<Museum> rootHandle = xmlService.unmarshall(url.toURI(), Museum.class);
        Museum museum = rootHandle.getRoot();
        
        Assert.assertEquals(100, museum.getId());
        Assert.assertEquals("Ben Franklin", museum.getName());
        Assert.assertEquals(110, museum.getAge());
    }
    
    /**
     * Tests the most basic of xml files can be unmarshalled with an class
     * annotated with jaxb annotations
     * 
     * @throws Exception
     */
    @Test // @org.junit.Ignore
    public void testClassJaxbUnmarshalling() throws Exception {
        ServiceLocator locator = Utilities.createLocator();
        XmlService xmlService = locator.getService(XmlService.class);
        
        URL url = getClass().getClassLoader().getResource("museum1.xml");
        
        XmlRootHandle<MuseumBean> rootHandle = xmlService.unmarshall(url.toURI(), MuseumBean.class);
        Museum museum = rootHandle.getRoot();
        
        Assert.assertEquals(100, museum.getId());
        Assert.assertEquals("Ben Franklin", museum.getName());
        Assert.assertEquals(110, museum.getAge());
    }
    
    /**
     * Tests the most basic of xml files can be unmarshalled with an class
     * annotated with jaxb annotations
     * 
     * @throws Exception
     */
    @Test // @org.junit.Ignore
    public void testClassJaxbUnmarshallingWithChildren() throws Exception {
        ServiceLocator locator = Utilities.createLocator();
        XmlService xmlService = locator.getService(XmlService.class);
        
        URL url = getClass().getClassLoader().getResource("Acme1.xml");
        
        XmlRootHandle<EmployeesBean> rootHandle = xmlService.unmarshall(url.toURI(), EmployeesBean.class);
        Employees employees = rootHandle.getRoot();
        
        Assert.assertEquals("Acme", employees.getCompanyName());
        
        Assert.assertEquals(2, employees.getEmployees().size());
        
        boolean first = true;
        for (Employee employee : employees.getEmployees()) {
            if (first) {
                first = false;
                Assert.assertEquals(100L, employee.getId());
                Assert.assertEquals("Bob", employee.getName());
            }
            else {
                Assert.assertEquals(101L, employee.getId());
                Assert.assertEquals("Carol", employee.getName());
                
            }
        }
    }
    
    /**
     * Tests the most basic of xml files can be unmarshalled with an interface
     * annotated with jaxb annotations
     * 
     * @throws Exception
     */
    @Test // @org.junit.Ignore
    public void testInterfaceJaxbUnmarshallingWithChildren() throws Exception {
        ServiceLocator locator = Utilities.createLocator();
        XmlService xmlService = locator.getService(XmlService.class);
        
        URL url = getClass().getClassLoader().getResource("Acme1.xml");
        
        XmlRootHandle<Employees> rootHandle = xmlService.unmarshall(url.toURI(), Employees.class);
        Employees employees = rootHandle.getRoot();
        
        Assert.assertEquals("Acme", employees.getCompanyName());
        
        Assert.assertEquals(2, employees.getEmployees().size());
        
        boolean first = true;
        for (Employee employee : employees.getEmployees()) {
            if (first) {
                first = false;
                Assert.assertEquals(100L, employee.getId());
                Assert.assertEquals("Bob", employee.getName());
            }
            else {
                Assert.assertEquals(101L, employee.getId());
                Assert.assertEquals("Carol", employee.getName());
                
            }
        }
    }
}
