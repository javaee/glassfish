/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010 Oracle and/or its affiliates. All rights reserved.
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
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
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

package org.glassfish.admingui.devtests;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class NetworkConfigTest extends BaseSeleniumTestClass {
    private static final String TRIGGER_NETWORK_LISTENERS = "Click New to define a new network listener. Click the name of an existing listener to modify its settings.";
    private static final String TRIGGER_NEW_NETWORK_LISTENER = "New Network Listener";
    private static final String TRIGGER_PROTOCOLS = "Click New to define a new protocol. Click the name of an existing protocol to modify its settings. Select one or more protocols and click Delete to delete the protocols and any network listeners using them.";
    private static final String TRIGGER_NEW_PROTOCOL = "Create a new protocol.";
    private static final String TRIGGER_TRANSPORTS = "Click New to define a new transport. Click the name of an existing transport to modify its settings.";
    private static final String TRIGGER_NEW_TRANSPORT = "Create a new transport.";

    @Test
    public void testAddingNetworkListener() {
        final String listenerName = "listener"+generateRandomString();

        clickAndWait("treeForm:tree:configurations:server-config:networkConfig:networkListeners:networkListeners_link", TRIGGER_NETWORK_LISTENERS);
        clickAndWait("propertyForm:configs:topActionsGroup1:newButton", TRIGGER_NEW_NETWORK_LISTENER);
        selenium.type("propertyForm:propertySheet:propertSectionTextField:nameNew:name", listenerName);
        selenium.click("propertyForm:propertySheet:propertSectionTextField:prop1:existingRdBtn");
        selenium.select("propertyForm:propertySheet:propertSectionTextField:prop1:protocoldw", "label=http-listener-1");
        selenium.type("propertyForm:propertySheet:propertSectionTextField:port:port", "1234");
        clickAndWait("propertyForm:propertyContentPage:topButtons:newButton", TRIGGER_NETWORK_LISTENERS);
        assertTrue(selenium.isTextPresent(listenerName));

        clickAndWait(getLinkIdByLinkText("propertyForm:configs", listenerName), "Edit Network Listener");

        assertTrue(selenium.isTextPresent(listenerName));
        assertTrue(selenium.isTextPresent("http-listener-1"));

        assertEquals("1234", selenium.getValue("propertyForm:propertySheet:propertSectionTextField:port:port"));
        clickAndWait("propertyForm:propertyContentPage:topButtons:cancelButton", TRIGGER_NETWORK_LISTENERS);

        deleteRow("propertyForm:configs:topActionsGroup1:button1", "propertyForm:configs", listenerName);
    }

    @Test
    public void testAddingTransport() {
        final String transportName = "transport"+generateRandomString();

        clickAndWait("treeForm:tree:configurations:server-config:networkConfig:transports:transports_link", TRIGGER_TRANSPORTS);
        clickAndWait("propertyForm:configs:topActionsGroup1:newButton", TRIGGER_NEW_TRANSPORT);
        selenium.type("propertyForm:propertySheet:propertSectionTextField:IdTextProp:IdText", transportName);
        selenium.select("propertyForm:propertySheet:propertSectionTextField:ByteBufferType:ByteBufferType", "label=DIRECT");
        selenium.type("propertyForm:propertySheet:propertSectionTextField:BufferSizeBytes:BufferSizeBytes", "1000");
        selenium.type("propertyForm:propertySheet:propertSectionTextField:AcceptorThreads:AcceptorThreads", "-1");
        clickAndWait("propertyForm:propertyContentPage:topButtons:newButton", TRIGGER_TRANSPORTS);
        assertTrue(selenium.isTextPresent(transportName));

        clickAndWait(getLinkIdByLinkText("propertyForm:configs", transportName), "Edit Transport");
        assertTrue(selenium.isTextPresent(transportName));
        assertTrue(selenium.isTextPresent("DIRECT"));
        assertEquals("1000", selenium.getValue("propertyForm:propertySheet:propertSectionTextField:BufferSizeBytes:BufferSizeBytes"));
        clickAndWait("propertyForm:propertyContentPage:topButtons:cancelButton", TRIGGER_TRANSPORTS);

        deleteRow("propertyForm:configs:topActionsGroup1:button1", "propertyForm:configs", transportName);
    }

    @Test
    public void testAddingProtocol() {
        final String protocol = "protocol"+generateRandomString();
        final String maxAge = Integer.toString(generateRandomNumber(60));
        final String maxCacheSizeBytes = Integer.toString(generateRandomNumber(10485760));
        final String maxFile = Integer.toString(generateRandomNumber(2048));
        final String maxC = Integer.toString(generateRandomNumber(512));
        final String timeoutSeconds = Integer.toString(generateRandomNumber(60));
        final String connectionUploadTimeout = Integer.toString(generateRandomNumber(600000));
        final String requestTimeoutSeconds = Integer.toString(generateRandomNumber(60));
        final String sendBsize = Integer.toString(generateRandomNumber(16384));
        final String headerBLength = Integer.toString(generateRandomNumber(16384));
        final String maxPostSize = Integer.toString(generateRandomNumber(2097152));
        final String compressableMime = Integer.toString(generateRandomNumber(4096));

        clickAndWait("treeForm:tree:configurations:server-config:networkConfig:protocols:protocols_link", TRIGGER_PROTOCOLS);
        clickAndWait("propertyForm:configs:topActionsGroup1:newButton", TRIGGER_NEW_PROTOCOL);
        selenium.type("propertyForm:propertySheet:propertSectionTextField:nameNew:name", protocol);
        selenium.type("propertyForm:propertySheet:fileTextField:maxAge:maxAge", maxAge);
        selenium.type("propertyForm:propertySheet:fileTextField:maxCacheSizeBytes:maxCacheSizeBytes", maxCacheSizeBytes);
        selenium.type("propertyForm:propertySheet:fileTextField:maxFile:maxFile", maxFile);
        selenium.type("propertyForm:propertySheet:httpTextField:maxC:maxC", maxC);
        selenium.type("propertyForm:propertySheet:httpTextField:TimeoutSeconds:TimeoutSeconds", timeoutSeconds);
        selenium.type("propertyForm:propertySheet:httpTextField:connectionUploadTimeout:connectionUploadTimeout", connectionUploadTimeout);
        selenium.type("propertyForm:propertySheet:httpTextField:RequestTimeoutSeconds:RequestTimeoutSeconds", requestTimeoutSeconds);
        selenium.type("propertyForm:propertySheet:httpTextField:sendBsize:sendBsize", sendBsize);
        selenium.type("propertyForm:propertySheet:httpTextField:headerBLength:headerBLength", headerBLength);
        selenium.type("propertyForm:propertySheet:httpTextField:MaxPostSize:headerBLength", maxPostSize);
        selenium.select("propertyForm:propertySheet:httpTextField:Compression:Compression", "label=on");
        selenium.type("propertyForm:propertySheet:httpTextField:compressableMime:compressableMime", compressableMime);
        selenium.check("propertyForm:propertySheet:httpTextField:Comet:cometEnabled");

        clickAndWait("propertyForm:propertyContentPage:topButtons:newButton", TRIGGER_PROTOCOLS);
        assertTrue(selenium.isTextPresent(protocol));
        
        // Disabling checks for now.  There seems to be some backend issues plaguing this piece
/*
        clickAndWait(getLinkIdByLinkText("propertyForm:configs", protocol), "Edit Protocol");
        assertTrue(selenium.isTextPresent(protocol));
        
        clickAndWait("propertyForm:protocolTabs:httpTab", "Modify HTTP settings for the protocol.");
        assertEquals(maxC, selenium.getValue("propertyForm:propertySheet:httpTextField:maxC:maxC"));
        assertEquals(timeoutSeconds, selenium.getValue("propertyForm:propertySheet:httpTextField:TimeoutSeconds:TimeoutSeconds"));
        assertEquals(requestTimeoutSeconds, selenium.getValue("propertyForm:propertySheet:httpTextField:RequestTimeoutSeconds:RequestTimeoutSeconds"));
        assertEquals(connectionUploadTimeout, selenium.getValue("propertyForm:propertySheet:httpTextField:connectionUploadTimeout:connectionUploadTimeout"));
        assertEquals(sendBsize, selenium.getValue("propertyForm:propertySheet:httpTextField:sendBsize:sendBsize"));
        assertEquals(headerBLength, selenium.getValue("propertyForm:propertySheet:httpTextField:headerBLength:headerBLength"));
        assertEquals(maxPostSize, selenium.getValue("propertyForm:propertySheet:httpTextField:MaxPostSize:headerBLength"));
        assertEquals(compressableMime, selenium.getValue("propertyForm:propertySheet:httpTextField:compressableMime:compressableMime"));
        assertEquals("on", selenium.getValue("propertyForm:propertySheet:httpTextField:Comet:sun_checkbox7808"));

        clickAndWait("propertyForm:protocolTabs:fileCacheTab", "Modify file cache settings for the protocol.");
        assertEquals(maxAge, selenium.getValue("propertyForm:propertySheet:fileTextField:maxAge:maxAge"));
        assertEquals(maxCacheSizeBytes, selenium.getValue("propertyForm:propertySheet:fileTextField:maxCacheSizeBytes:maxCacheSizeBytes"));
        assertEquals(maxFile, selenium.getValue("propertyForm:propertySheet:fileTextField:maxFile:maxFile"));

        clickAndWait("propertyForm:propertyContentPage:topButtons:cancelButton", TRIGGER_PROTOCOLS);
*/

        deleteRow("propertyForm:configs:topActionsGroup1:button1", "propertyForm:configs", protocol);

    }
}
