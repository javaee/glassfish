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
package org.glassfish.hk2.xml.test.dynamic.removes;

import java.net.URL;
import java.util.List;

import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.configuration.hub.api.Hub;
import org.glassfish.hk2.xml.api.XmlRootHandle;
import org.glassfish.hk2.xml.api.XmlService;
import org.glassfish.hk2.xml.test.basic.Employee;
import org.glassfish.hk2.xml.test.basic.Employees;
import org.glassfish.hk2.xml.test.basic.OtherData;
import org.glassfish.hk2.xml.test.basic.UnmarshallTest;
import org.glassfish.hk2.xml.test.utilities.Utilities;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests removal of stuff
 * 
 * @author jwells
 *
 */
public class RemovesTest {
    public final static String EMPLOYEE_TYPE = "/employees/employee";
    public final static String BOB_EMPLOYEE_INSTANCE = "employees.Bob";
    
    public final static String ACME3_FILE = "Acme3.xml";
    
    public final static String INDEX0 = "Index0";
    public final static String INDEX1 = "Index1";
    public final static String INDEX2 = "Index2";
    public final static String INDEX3 = "Index3";
    
    /**
     * Tests remove of a keyed child with no sub-children
     * 
     * @throws Exception
     */
    @Test // @org.junit.Ignore
    public void testRemoveOfNamedChild() throws Exception {
        ServiceLocator locator = Utilities.createLocator();
        XmlService xmlService = locator.getService(XmlService.class);
        Hub hub = locator.getService(Hub.class);
        
        URL url = getClass().getClassLoader().getResource(UnmarshallTest.ACME1_FILE);
        
        XmlRootHandle<Employees> rootHandle = xmlService.unmarshall(url.toURI(), Employees.class);
        Employees employees = rootHandle.getRoot();
        
        Employee bob = employees.lookupEmployee(UnmarshallTest.BOB);
        
        // Make sure it is truly there
        Assert.assertNotNull(bob);  
        Assert.assertNotNull(locator.getService(Employee.class, UnmarshallTest.BOB));
        Assert.assertNotNull(hub.getCurrentDatabase().getInstance(EMPLOYEE_TYPE, BOB_EMPLOYEE_INSTANCE));
        
        employees.removeEmployee(UnmarshallTest.BOB);
        
        bob = employees.lookupEmployee(UnmarshallTest.BOB);
        
        Assert.assertNull(bob);
        Assert.assertNull(locator.getService(Employee.class, UnmarshallTest.BOB));
        Assert.assertNull(hub.getCurrentDatabase().getInstance(EMPLOYEE_TYPE, BOB_EMPLOYEE_INSTANCE));
    }
    
    /**
     * Tests remove of an un-keyed child with no sub-children
     * 
     * @throws Exception
     */
    @Test
    @org.junit.Ignore
    public void testRemoveOfIndexedChild() throws Exception {
        ServiceLocator locator = Utilities.createLocator();
        XmlService xmlService = locator.getService(XmlService.class);
        Hub hub = locator.getService(Hub.class);
        
        URL url = getClass().getClassLoader().getResource(ACME3_FILE);
        
        XmlRootHandle<Employees> rootHandle = xmlService.unmarshall(url.toURI(), Employees.class);
        Employees employees = rootHandle.getRoot();
        
        validateAcme3InitialState(employees, hub);
        
        employees.removeOtherData(2);
        
        List<OtherData> otherDatum = employees.getOtherData();
        Assert.assertEquals(3, otherDatum.size());
        
        Assert.assertEquals(INDEX0, otherDatum.get(0).getData());
        Assert.assertEquals(INDEX1, otherDatum.get(1).getData());
        // Index 2 was removed!
        Assert.assertEquals(INDEX2, otherDatum.get(2).getData());
    }
    
    private static void validateAcme3InitialState(Employees employees, Hub hub) {
        Assert.assertEquals(UnmarshallTest.ACME, employees.getCompanyName());
        
        List<OtherData> otherDatum = employees.getOtherData();
        Assert.assertEquals(4, otherDatum.size());
        
        Assert.assertEquals(INDEX0, otherDatum.get(0).getData());
        Assert.assertEquals(INDEX1, otherDatum.get(1).getData());
        Assert.assertEquals(INDEX2, otherDatum.get(2).getData());
        Assert.assertEquals(INDEX3, otherDatum.get(3).getData());
        
        
        
    }

}
