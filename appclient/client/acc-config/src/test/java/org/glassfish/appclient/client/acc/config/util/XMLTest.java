/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 * 
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
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

package org.glassfish.appclient.client.acc.config.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import org.glassfish.appclient.client.acc.config.ClientContainer;
import org.glassfish.appclient.client.acc.config.TargetServer;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author tjquinn
 */
public class XMLTest {

    private static final String SAMPLE_XML_PATH = "/sun-acc.xml";
    private static final String FIRST_HOST = "glassfish.dev.java.net";
    private static final int FIRST_PORT = 3701;
    private static final String SECOND_HOST = "other.dev.java.net";
    private static final int SECOND_PORT = 4701;

    private static final String FIRST_PROP_NAME = "firstProp";
    private static final String FIRST_PROP_VALUE = "firstValue";

    private static final String SECOND_PROP_NAME = "secondProp";
    private static final String SECOND_PROP_VALUE = "secondValue";

    public XMLTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Test
    public void testProps() throws Exception {
        System.out.println("testProps");
        ClientContainer cc = readConfig(SAMPLE_XML_PATH);
        Properties props = XML.toProperties(cc.getProperty());
        assertEquals("property value mismatch for first property", FIRST_PROP_VALUE, props.getProperty(FIRST_PROP_NAME));
        assertEquals("property value mismatch for second property", SECOND_PROP_VALUE, props.getProperty(SECOND_PROP_NAME));
    }

    @Test
    public void testReadSampleXML() throws Exception {
        System.out.println("testReadSampleXML");
        ClientContainer cc = readConfig(SAMPLE_XML_PATH);
        List<TargetServer> servers = cc.getTargetServer();


        assertTrue("target servers did not read correctly",
                servers.get(0).getAddress().equals(FIRST_HOST) &&
                servers.get(0).getPort().equals(FIRST_PORT) &&
                servers.get(1).getAddress().equals(SECOND_HOST) &&
                servers.get(1).getPort() == SECOND_PORT
            );

    }

    private static ClientContainer readConfig(final String configPath) throws JAXBException, FileNotFoundException {
        ClientContainer result = null;
        InputStream is = XMLTest.class.getResourceAsStream(SAMPLE_XML_PATH);
        if (is == null) {
            fail("cannot locate test file " + SAMPLE_XML_PATH);
        }
        JAXBContext jc = JAXBContext.newInstance(ClientContainer.class );

        Unmarshaller u = jc.createUnmarshaller();
        result = (ClientContainer) u.unmarshal(is);

        return result;
    }
}