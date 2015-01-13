/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2015 Oracle and/or its affiliates. All rights reserved.
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
package org.glassfish.hk2.xml.test.dynamic.adds;

import java.net.URL;

import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.configuration.hub.api.Hub;
import org.glassfish.hk2.configuration.hub.api.Instance;
import org.glassfish.hk2.configuration.hub.api.Type;
import org.glassfish.hk2.xml.api.XmlRootHandle;
import org.glassfish.hk2.xml.api.XmlService;
import org.glassfish.hk2.xml.test.Employee;
import org.glassfish.hk2.xml.test.Employees;
import org.glassfish.hk2.xml.test.Financials;
import org.glassfish.hk2.xml.test.OtherData;
import org.glassfish.hk2.xml.test.UnmarshallTest;
import org.glassfish.hk2.xml.test.utilities.Utilities;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests adding the root and children
 * 
 * @author jwells
 *
 */
public class AddsTest {
    private final static String DAVE = "Dave";
    private final static String EMPLOYEE_TYPE = "/employees/employee";
    private final static String OTHER_DATA_TYPE = "/employees/other-data";
    private final static String DAVE_INSTANCE = "employees.Dave";
    
    /**
     * Tests that we can call createAndAdd successfully on a root with no required elements
     */
    @Test @org.junit.Ignore
    public void testCreateAndAdd() {
        ServiceLocator locator = Utilities.createLocator();
        XmlService xmlService = locator.getService(XmlService.class);
        
        XmlRootHandle<Employees> rootHandle = xmlService.createEmptyHandle(Employees.class);
        Assert.assertNull(rootHandle.getRoot());
        
        rootHandle.addRoot();
        Employees root = rootHandle.getRoot();
        
        Assert.assertNotNull(root);
        Assert.assertNull(root.getFinancials());
        Assert.assertNull(root.getEmployees());
        Assert.assertNull(root.getCompanyName());
    }
    
    /**
     * Tests that we can add to an existing tree with just a basic add (no copy or overlay)
     */
    @Test // @org.junit.Ignore
    public void testAddToExistingTree() throws Exception {
        ServiceLocator locator = Utilities.createLocator();
        XmlService xmlService = locator.getService(XmlService.class);
        Hub hub = locator.getService(Hub.class);
        
        URL url = getClass().getClassLoader().getResource(UnmarshallTest.ACME1_FILE);
        
        XmlRootHandle<Employees> rootHandle = xmlService.unmarshall(url.toURI(), Employees.class);
        Employees employees = rootHandle.getRoot();
        
        employees.addEmployees(DAVE);
        
        Employee daveDirect = employees.lookupEmployees(DAVE);
        Assert.assertNotNull(daveDirect);
        
        Employee daveService = locator.getService(Employee.class, DAVE);
        Assert.assertNotNull(daveService);
        
        Assert.assertNotNull(hub.getCurrentDatabase().getInstance(EMPLOYEE_TYPE, DAVE_INSTANCE));
    }
    
    /**
     * Tests that we can add to an existing tree with just a basic add
     * with an unkeyed field
     */
    @Test // @org.junit.Ignore
    public void testAddToExistingTreeUnKeyed() throws Exception {
        ServiceLocator locator = Utilities.createLocator();
        XmlService xmlService = locator.getService(XmlService.class);
        Hub hub = locator.getService(Hub.class);
        
        URL url = getClass().getClassLoader().getResource(UnmarshallTest.ACME1_FILE);
        
        XmlRootHandle<Employees> rootHandle = xmlService.unmarshall(url.toURI(), Employees.class);
        Employees employees = rootHandle.getRoot();
        
        employees.addOtherData(0);
        
        OtherData found = null;
        for (OtherData other : employees.getOtherData()) {
            Assert.assertNull(found);
            found = other;
        }
        
        Assert.assertNotNull(found);
        
        OtherData otherService = locator.getService(OtherData.class);
        Assert.assertNotNull(otherService);
        
        Assert.assertEquals(found, otherService);
        
        Type type = hub.getCurrentDatabase().getType(OTHER_DATA_TYPE);
        
        Instance foundInstance = null;
        for (Instance i : type.getInstances().values()) {
            Assert.assertNull(foundInstance);
            foundInstance = i;
        }
        
        Assert.assertNotNull(foundInstance);
    }
    
    /**
     * Tests that we can add to an existing tree with just a basic add
     * with an direct stanza
     */
    @Test // @org.junit.Ignore
    public void testAddToExistingTreeDirect() throws Exception {
        ServiceLocator locator = Utilities.createLocator();
        XmlService xmlService = locator.getService(XmlService.class);
        Hub hub = locator.getService(Hub.class);
        
        URL url = getClass().getClassLoader().getResource(UnmarshallTest.ACME2_FILE);
        
        XmlRootHandle<Employees> rootHandle = xmlService.unmarshall(url.toURI(), Employees.class);
        Employees employees = rootHandle.getRoot();
        
        Assert.assertNull(employees.getFinancials());
        
        employees.addFinancials();
        
        Financials financials = employees.getFinancials();
        
        Assert.assertNotNull(financials);
        Assert.assertNull(financials.getExchange());
        Assert.assertNull(financials.getSymbol());
        
        Assert.assertNotNull(hub.getCurrentDatabase().getInstance(UnmarshallTest.FINANCIALS_TYPE, UnmarshallTest.FINANCIALS_INSTANCE));
    }

}
