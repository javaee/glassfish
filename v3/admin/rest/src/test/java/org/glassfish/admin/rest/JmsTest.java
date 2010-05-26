package org.glassfish.admin.rest;

import com.sun.jersey.api.client.ClientResponse;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by IntelliJ IDEA.
 * User: jasonlee
 * Date: May 26, 2010
 * Time: 2:28:27 PM
 * To change this template use File | Settings | File Templates.
 */
public class JmsTest extends RestTestBase {
    static final String URL_CONNECTOR_CONNECTION_POOL = BASE_URL + "/domain/resources/connector-connection-pool";
    static final String URL_CONNECTOR_RESOURCE = BASE_URL + "/domain/resources/connector-resource";

    @Test
    public void testJmsConnectionFactories() {
        Map<String, String> ccp_attrs = new HashMap<String, String>();
        Map<String, String> cr_attrs = new HashMap<String, String>();
        final String poolName = "JmsConnectionFactory" + generateRandomString();

        ccp_attrs.put("name", poolName);
        ccp_attrs.put("connectiondefinition", "javax.jms.ConnectionFactory");
        ccp_attrs.put("raname", "jmsra");

        cr_attrs.put("id", poolName);
        cr_attrs.put("poolname", poolName);

        // Create connection pool
        ClientResponse response = this.post(URL_CONNECTOR_CONNECTION_POOL, ccp_attrs);
        assertEquals(201, response.getStatus());

        // Check connection pool creation
        Map<String, String> pool = getEntityValues(get(URL_CONNECTOR_CONNECTION_POOL + "/" + poolName));
        assertFalse(pool.size() == 0);

        // Create connector resource
        response = this.post(URL_CONNECTOR_RESOURCE, cr_attrs);
        assertEquals(201, response.getStatus());

        // Check connector resource
        Map<String, String> resource = getEntityValues(get(URL_CONNECTOR_RESOURCE + "/" + poolName));
        assertFalse(resource.size() == 0);

        // Edit and check ccp
        response = this.post(URL_CONNECTOR_CONNECTION_POOL + "/" + poolName, new HashMap<String,String>() {{ put("description", poolName); }});
        assertEquals(200, response.getStatus());

        pool = getEntityValues(get(URL_CONNECTOR_CONNECTION_POOL + "/" + poolName));
        assertTrue(pool.get("description").equals(poolName));

        // Edit and check cr
        response = this.post(URL_CONNECTOR_RESOURCE + "/" + poolName, new HashMap<String,String>() {{ put("description", poolName); }});
        assertEquals(200, response.getStatus());

        resource = getEntityValues(get(URL_CONNECTOR_RESOURCE + "/" + poolName));
        assertTrue(pool.get("description").equals(poolName));

        // Delete objects
        response = delete(URL_CONNECTOR_CONNECTION_POOL + "/" + poolName, new HashMap<String,String>() {{ put("cascade", "true"); }});
    }
}
