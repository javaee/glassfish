/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.glassfish.admin.rest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.sun.jersey.api.client.ClientResponse;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jasonlee
 */
public class PropertiesBagTest extends RestTestBase {

    protected static final String PROP_DOMAIN_NAME = "administrative.domain.name";
    protected static final String URL_DOMAIN_PROPERTIES = BASE_URL + "/property";

    @Test
    public void propertyRetrieval() {
        ClientResponse response = get(URL_DOMAIN_PROPERTIES);
        assertTrue(isSuccess(response));
        List<Map> properties = getProperties(response);
        assertTrue(isPropertyFound(properties, PROP_DOMAIN_NAME));
    }

    @Test
    public void addProperties() {
        final String propName = "property_" + generateRandomString();
        final String propValue = generateRandomString();
        HashMap<String, String> domainProps = new HashMap<String, String>() {{
            put(propName, propValue);
        }};

        try {
            ClientResponse response = post(URL_DOMAIN_PROPERTIES, domainProps);
            assertTrue(isSuccess(response));
            response = get(URL_DOMAIN_PROPERTIES);
            assertTrue(isSuccess(response));
            List<Map> properties = getProperties(response);
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
            assertTrue(isSuccess(response));
            response = get(URL_DOMAIN_PROPERTIES);
            assertTrue(isSuccess(response));
            List<Map> properties = getProperties(response);
            assertEquals(0, properties.size());
        } finally {
            restoreDomainProperties();
        }

    }

    // Restore and verify the default domain properties
    protected void restoreDomainProperties() {
        HashMap<String, String> domainProps = new HashMap<String, String>() {{
            put(PROP_DOMAIN_NAME, "domain1");
        }};
        ClientResponse response = put(URL_DOMAIN_PROPERTIES, domainProps);
        assertTrue(isSuccess(response));
        response = get(URL_DOMAIN_PROPERTIES);
        assertTrue(isSuccess(response));
        assertTrue(isPropertyFound(getProperties(response), PROP_DOMAIN_NAME));
    }

    protected boolean isPropertyFound(List<Map> properties, String name) {
        boolean propertyFound = false;
        for (Map property : properties) {
            if (name.equals(property.get("name"))) {
                propertyFound = true;
            }
        }

        return propertyFound;
    }
}
