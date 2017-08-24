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
package org.glassfish.hk2.pbuf.test.basic;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.net.URL;
import java.util.List;

import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.pbuf.api.PBufUtilities;
import org.glassfish.hk2.pbuf.test.beans.CustomerBean;
import org.glassfish.hk2.pbuf.test.beans.RootOnlyBean;
import org.glassfish.hk2.pbuf.test.beans.ServiceRecordBean;
import org.glassfish.hk2.pbuf.test.beans.ServiceRecordBlockBean;
import org.glassfish.hk2.pbuf.test.beans.TypeBean;
import org.glassfish.hk2.pbuf.test.utilities.Utilities;
import org.glassfish.hk2.xml.api.XmlRootHandle;
import org.glassfish.hk2.xml.api.XmlService;
import org.junit.Assert;
import org.junit.Test;

import com.google.protobuf.DescriptorProtos;

/**
 * @author jwells
 *
 */
public class PBufParserTest {
    private final static String ALICE = "Alice";
    private final static String ALICE_ADDRESS = "Milky Way";
    
    private final static String ACME = "Acme";
    private final static long ACME_ID = 3000;
    private final static String ACME_HASH = "aaabbbccc";
    
    private final static String BJS = "BJs";
    private final static long BJS_ID = 4000;
    private final static String BJS_HASH = "dddeeefff";
    
    private final static String COSTCO = "Costco";
    private final static long COSTCO_ID = 5000;
    private final static String COSTCO_HASH = "ggghhhiii";
    
    /**
     * Tests very basic marshaling
     */
    @Test
    // @org.junit.Ignore
    public void testMarshalFlatBean() throws Exception {
        ClassLoader cl = getClass().getClassLoader();
        URL protoURL = cl.getResource("proto/RootOnlyBean.proto");
        
        ServiceLocator locator = Utilities.enableLocator();
        
        XmlService xmlService = locator.getService(XmlService.class, PBufUtilities.PBUF_SERVICE_NAME);
        Assert.assertNotNull(xmlService);
        
        XmlRootHandle<RootOnlyBean> handle = xmlService.createEmptyHandle(RootOnlyBean.class);
        handle.addRoot();
        
        RootOnlyBean rootOnlyBean = handle.getRoot();
        
        rootOnlyBean.setAddress(ALICE_ADDRESS);
        rootOnlyBean.setName(ALICE);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
          handle.marshal(baos);
        }
        finally {
            baos.close();
        }
        
        byte[] asBytes = baos.toByteArray();
        Assert.assertTrue(asBytes.length > 0);
    }
    
    /**
     * marshals a complex data structure
     * 
     * @throws Exception
     */
    @Test
    // @org.junit.Ignore
    public void testMarshalStructuredBean() throws Exception {
        ServiceLocator locator = Utilities.enableLocator();
        
        XmlService xmlService = locator.getService(XmlService.class, PBufUtilities.PBUF_SERVICE_NAME);
        Assert.assertNotNull(xmlService);
        
        XmlRootHandle<ServiceRecordBlockBean> handle = getStandardTestBlock(xmlService);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
          handle.marshal(baos);
        }
        finally {
            baos.close();
        }
        
        byte[] asBytes = baos.toByteArray();
        Assert.assertTrue(asBytes.length > 0);
    }
    
    /**
     * Reads in a pre-generated binary protobuf
     * 
     * @throws Exception
     */
    @Test
    // @org.junit.Ignore
    public void testUnmarshalStructuredBean() throws Exception {
        ClassLoader cl = getClass().getClassLoader();
        URI standardPbufURI = cl.getResource("standard.pbuf").toURI();
        
        ServiceLocator locator = Utilities.enableLocator();
        
        XmlService xmlService = locator.getService(XmlService.class, PBufUtilities.PBUF_SERVICE_NAME);
        Assert.assertNotNull(xmlService);
        
        XmlRootHandle<ServiceRecordBlockBean> handle = xmlService.unmarshal(standardPbufURI, ServiceRecordBlockBean.class);
        
        validateStandardBean(handle);
    }
    
    /**
     * Tests all the native types of pbuf
     * 
     * @throws Exception
     */
    @Test
    @org.junit.Ignore
    public void testTypes() throws Exception {
        ServiceLocator locator = Utilities.enableLocator();
        
        XmlService xmlService = locator.getService(XmlService.class, PBufUtilities.PBUF_SERVICE_NAME);
        Assert.assertNotNull(xmlService);
        
        XmlRootHandle<TypeBean> handle = xmlService.createEmptyHandle(TypeBean.class);
        handle.addRoot();
        
        TypeBean root = handle.getRoot();
        
        root.setIType(13);
        root.setJType(14L);
        root.setZType(true);
        root.setBType((byte) 15);
        root.setCType('E');
        root.setSType((short) 16);
        root.setFType((float) 17.0);
        root.setDType(18.0);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
          handle.marshal(baos);
        }
        finally {
            baos.close();
        }
        
        byte[] asBytes = baos.toByteArray();
        Assert.assertTrue(asBytes.length > 0);
        
        XmlRootHandle<TypeBean> readHandle;
        
        ByteArrayInputStream bais = new ByteArrayInputStream(asBytes);
        try {
            readHandle = xmlService.unmarshal(bais, TypeBean.class);
        }
        finally {
            bais.close();
        }
        
        TypeBean typeRoot = readHandle.getRoot();
        
        Assert.assertEquals(13, typeRoot.getIType());
        Assert.assertEquals(14L, typeRoot.getJType());
        Assert.assertEquals(true, typeRoot.getZType());
        Assert.assertEquals((byte) 15, typeRoot.getBType());
        Assert.assertEquals('E', typeRoot.getCType());
        Assert.assertEquals((short) 16, typeRoot.getSType());
        Assert.assertTrue(new Float(17.0).equals(typeRoot.getFType()));
        Assert.assertTrue(new Double(18.0).equals(typeRoot.getDType()));
        
    }
    
    private static void validateStandardBean(XmlRootHandle<ServiceRecordBlockBean> handle) {
        ServiceRecordBlockBean root = handle.getRoot();
        Assert.assertNotNull(root);
        
        List<ServiceRecordBean> records = root.getServiceRecords();
        Assert.assertEquals(3, records.size());
        
        validateServiceRecordBean(records.get(0), ACME, ACME_ID, ACME_HASH);
        validateServiceRecordBean(records.get(1), BJS, BJS_ID, BJS_HASH);
        validateServiceRecordBean(records.get(2), COSTCO, COSTCO_ID, COSTCO_HASH);
    }
    
    private static void validateServiceRecordBean(ServiceRecordBean record, String name, long id, String hash) {
        Assert.assertNotNull(record);
        
        Assert.assertEquals(hash, record.getServiceRecordID());
        
        CustomerBean customer = record.getCustomer();
        Assert.assertNotNull(customer);
        
        Assert.assertEquals(name, customer.getCustomerName());
        Assert.assertEquals(id, customer.getCustomerID());
    }
    
    private static CustomerBean createCustomerBean(XmlService xmlService, String companyName, long id) {
        CustomerBean retVal = xmlService.createBean(CustomerBean.class);
        
        retVal.setCustomerName(companyName);
        retVal.setCustomerID(id);
        
        return retVal;
    }
    
    private static ServiceRecordBean createServiceRecordBean(XmlService xmlService, CustomerBean customer, String hash) {
        ServiceRecordBean retVal = xmlService.createBean(ServiceRecordBean.class);
        
        retVal.setServiceRecordID(hash);
        retVal.setCustomer(customer);
        
        return retVal;
    }
    
    private static XmlRootHandle<ServiceRecordBlockBean> getStandardTestBlock(XmlService xmlService) {
        XmlRootHandle<ServiceRecordBlockBean> handle = xmlService.createEmptyHandle(ServiceRecordBlockBean.class);
        handle.addRoot();
        
        ServiceRecordBlockBean blockBean = handle.getRoot();
        
        CustomerBean acmeBean = createCustomerBean(xmlService, ACME, ACME_ID);
        CustomerBean bjsBean = createCustomerBean(xmlService, BJS, BJS_ID);
        CustomerBean costcoBean = createCustomerBean(xmlService, COSTCO, COSTCO_ID);
        
        ServiceRecordBean acmeRecord = createServiceRecordBean(xmlService, acmeBean, ACME_HASH);
        ServiceRecordBean bjsRecord = createServiceRecordBean(xmlService, bjsBean, BJS_HASH);
        ServiceRecordBean costcoRecord = createServiceRecordBean(xmlService, costcoBean, COSTCO_HASH);
        
        blockBean.addServiceRecord(acmeRecord);
        blockBean.addServiceRecord(bjsRecord);
        blockBean.addServiceRecord(costcoRecord);
        
        return handle;
    }

}
