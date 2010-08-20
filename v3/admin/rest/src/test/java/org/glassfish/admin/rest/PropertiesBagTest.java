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

package org.glassfish.admin.rest;

import java.util.ArrayList;
import org.glassfish.admin.rest.clientutils.MarshallingUtils;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.sun.jersey.api.client.ClientResponse;
import org.junit.Test;

import javax.ws.rs.core.MediaType;

import static org.junit.Assert.*;

/**
 *
 * @author jasonlee
 */
public class PropertiesBagTest extends RestTestBase {

    protected static final String PROP_DOMAIN_NAME = "administrative.domain.name";
    protected static final String URL_DOMAIN_PROPERTIES = "/domain/property";
    protected static final String URL_JAVA_CONFIG_PROPERTIES = "/domain/configs/config/default-config/java-config/property";
    protected static final String URL_SERVER_PROPERTIES = "/domain/servers/server/server/property";
    private static final String REQUEST_FORMAT = MediaType.APPLICATION_JSON;

    @Test
    public void propertyRetrieval() {
        ClientResponse response = get(URL_DOMAIN_PROPERTIES);
        checkStatusForSuccess(response);
        List<Map<String, String>> properties = getProperties(response);
        assertTrue(isPropertyFound(properties, PROP_DOMAIN_NAME));
    }

    @Test
    public void addDomainProperties() {
        final String propName = "property_" + generateRandomString();
        final String propValue = generateRandomString();
        final Map<String, String> domainProps = new HashMap<String, String>() {{
            put("name", propName);
            put("value", propValue);
        }};

        try {
            createProperties(URL_DOMAIN_PROPERTIES, new ArrayList<Map<String, String>>() {{ add(domainProps); }});
        } finally {
            restoreDomainProperties();
        }
    }

    @Test
    public void deleteDomainProperties() {
        try {
            ClientResponse response = delete(URL_DOMAIN_PROPERTIES);
            checkStatusForSuccess(response);
            response = get(URL_DOMAIN_PROPERTIES);
            checkStatusForSuccess(response);
            List<Map<String, String>> properties = getProperties(response);
            assertEquals(0, properties.size());
        } finally {
            restoreDomainProperties();
        }

    }

    @Test
    public void javaConfigProperties() {
        createAndDeleteProperties(URL_JAVA_CONFIG_PROPERTIES);
    }

    @Test
    public void serverProperties() {
        createAndDeleteProperties(URL_SERVER_PROPERTIES);
    }

    protected void createAndDeleteProperties(String endpoint) {
        ClientResponse response = get(endpoint);
        checkStatusForSuccess(response);
        assertNotNull(getProperties(response));

        List<Map<String, String>> properties = new ArrayList<Map<String, String>>();

        for(int i = 0, max = generateRandomNumber(16); i < max; i++) {
            properties.add(new HashMap<String, String>() {{
                put ("name", "property_" + generateRandomString());
                put ("value", generateRandomString());
                put ("description", generateRandomString());
            }});
        }

        createProperties(endpoint, properties);
        response = delete(endpoint);
        checkStatusForSuccess(response);
    }

    protected void createProperties(String endpoint, List<Map<String, String>> properties) {
        final String payload = buildPayload(properties);
        ClientResponse response = client.resource(getAddress(endpoint))
            .header("Content-Type", REQUEST_FORMAT)
            .accept(RESPONSE_TYPE)
            .post(ClientResponse.class, payload);
        checkStatusForSuccess(response);
        response = get(endpoint);
        checkStatusForSuccess(response);

        // Retrieve the properties and make sure they were created.
        List<Map<String, String>> newProperties = getProperties(response);

        for (Map<String, String> property : properties) {
            assertTrue(isPropertyFound(newProperties, property.get("name")));
        }
    }

    // Restore and verify the default domain properties
    protected void restoreDomainProperties() {
        final HashMap<String, String> domainProps = new HashMap<String, String>() {{
            put("name", PROP_DOMAIN_NAME);
            put("value", "domain1");
        }};
        ClientResponse response = client.resource(getAddress(URL_DOMAIN_PROPERTIES))
                .header("Content-Type", REQUEST_FORMAT)
                .accept(RESPONSE_TYPE)
                .put(ClientResponse.class, buildPayload(new ArrayList<Map<String, String>>() {{ add(domainProps); }}));
        checkStatusForSuccess(response);
        response = get(URL_DOMAIN_PROPERTIES);
        checkStatusForSuccess(response);
        assertTrue(isPropertyFound(getProperties(response), PROP_DOMAIN_NAME));
    }

    protected String buildPayload(List<Map<String, String>> properties) {
        if (RESPONSE_TYPE.equals(MediaType.APPLICATION_XML)) {
            return MarshallingUtils.getXmlForProperties(properties);
        } else {
            return MarshallingUtils.getJsonForProperties(properties);
        }
    }

    protected boolean isPropertyFound(List<Map<String, String>> properties, String name) {
        boolean propertyFound = false;
        for (Map property : properties) {
            if (name.equals(property.get("name"))) {
                propertyFound = true;
            }
        }

        return propertyFound;
    }
}
