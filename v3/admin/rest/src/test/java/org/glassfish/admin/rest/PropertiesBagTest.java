/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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
package org.glassfish.admin.rest;

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

    @Test
    public void propertyRetrieval() {
        ClientResponse response = get(URL_DOMAIN_PROPERTIES);
        checkStatusForSuccess(response);
        List<Map<String, String>> properties = getProperties(response);
        assertTrue(isPropertyFound(properties, PROP_DOMAIN_NAME));
    }

    @Test
    public void addProperties() {
        final String propName = "property_" + generateRandomString();
        final String propValue = generateRandomString();
        Map<String, String> domainProps = new HashMap<String, String>() {{
            put("name", propName);
            put("value", propValue);
        }};

        try {
            ClientResponse response = client.resource(getAddress(URL_DOMAIN_PROPERTIES))
                .header("Content-Type", MediaType.APPLICATION_XML)
                .accept(RESPONSE_TYPE)
                .post(ClientResponse.class, MarshallingUtils.getXmlForProperties(domainProps));
            checkStatusForSuccess(response);
            response = get(URL_DOMAIN_PROPERTIES);
            checkStatusForSuccess(response);
            List<Map<String, String>> properties = getProperties(response);
            assertTrue(isPropertyFound(properties, propName));
            assertFalse(isPropertyFound(properties, PROP_DOMAIN_NAME));
        } finally {
            restoreDomainProperties();
        }
    }

    @Test
    public void deleteProperties() {
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

    // Restore and verify the default domain properties
    protected void restoreDomainProperties() {
        HashMap<String, String> domainProps = new HashMap<String, String>() {{
            put("name", PROP_DOMAIN_NAME);
            put("value", "domain1");
        }};
        ClientResponse response = client.resource(getAddress(URL_DOMAIN_PROPERTIES))
                .header("Content-Type", MediaType.APPLICATION_XML)
                .accept(RESPONSE_TYPE)
                .put(ClientResponse.class, MarshallingUtils.getXmlForProperties(domainProps));
        checkStatusForSuccess(response);
        response = get(URL_DOMAIN_PROPERTIES);
        checkStatusForSuccess(response);
        assertTrue(isPropertyFound(getProperties(response), PROP_DOMAIN_NAME));
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
