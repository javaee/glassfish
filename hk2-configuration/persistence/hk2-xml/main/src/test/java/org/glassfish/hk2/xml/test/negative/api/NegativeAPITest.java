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
package org.glassfish.hk2.xml.test.negative.api;

import java.io.InputStream;
import java.net.URI;
import java.net.URL;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.xml.api.XmlService;
import org.glassfish.hk2.xml.test.beans.DomainBean;
import org.glassfish.hk2.xml.test.dynamic.merge.MergeTest;
import org.glassfish.hk2.xml.test.utilities.Utilities;
import org.junit.Test;

/**
 * Tests for bad input and the like
 * @author jwells
 *
 */
public class NegativeAPITest {
    private final URL DOMAIN_URL = getClass().getClassLoader().getResource(MergeTest.DOMAIN1_FILE);
    private final XMLInputFactory xif = XMLInputFactory.newInstance();
    
    private XMLStreamReader openDomainReader() throws Exception {
        InputStream is = DOMAIN_URL.openStream();
        return xif.createXMLStreamReader(is);
    }
    
    /**
     * XmlService.unmarshal with null URI
     */
    @Test(expected=IllegalArgumentException.class)
    public void testNullURIXmlServiceUnmarshal() {
        ServiceLocator locator = Utilities.createDomLocator();
        XmlService xmlService = locator.getService(XmlService.class);
        
        xmlService.unmarshal((URI) null, DomainBean.class);
    }
    
    /**
     * XmlService.unmarshal with null bean
     */
    @Test(expected=IllegalArgumentException.class)
    public void testNullBeanXmlServiceUnmarshal() throws Exception {
        ServiceLocator locator = Utilities.createDomLocator();
        XmlService xmlService = locator.getService(XmlService.class);
        
        URI uri = DOMAIN_URL.toURI();
        
        xmlService.unmarshal(uri, null);
    }
    
    /**
     * XmlService.unmarshal with null bean
     */
    @Test(expected=IllegalArgumentException.class)
    public void testBeanIsClassXmlServiceUnmarshal() throws Exception {
        ServiceLocator locator = Utilities.createDomLocator();
        XmlService xmlService = locator.getService(XmlService.class);
        
        URI uri = DOMAIN_URL.toURI();
        
        xmlService.unmarshal(uri, NegativeAPITest.class);
    }
    
    /**
     * XmlService.unmarshal with null URI
     */
    @Test(expected=IllegalArgumentException.class)
    public void testNullReaderXmlServiceUnmarshal() {
        ServiceLocator locator = Utilities.createDomLocator();
        XmlService xmlService = locator.getService(XmlService.class);
        
        xmlService.unmarshal((XMLStreamReader) null, DomainBean.class, false, true);
    }
    
    /**
     * XmlService.unmarshal with null bean
     */
    @Test(expected=IllegalArgumentException.class)
    public void testNullBeanXmlServiceUnmarshalReader() throws Exception {
        ServiceLocator locator = Utilities.createDomLocator();
        XmlService xmlService = locator.getService(XmlService.class);
        
        XMLStreamReader reader = openDomainReader();
        try {
            xmlService.unmarshal(reader, null, false, false);
        }
        finally {
            reader.close();
        }
    }
    
    /**
     * XmlService.unmarshal with null bean
     */
    @Test(expected=IllegalArgumentException.class)
    public void testBeanIsClassXmlServiceUnmarshalReader() throws Exception {
        ServiceLocator locator = Utilities.createDomLocator();
        XmlService xmlService = locator.getService(XmlService.class);
        
        XMLStreamReader reader = openDomainReader();
        try {
            xmlService.unmarshal(reader, NegativeAPITest.class, true, false);
        }
        finally {
            reader.close();
        }
    }

}
