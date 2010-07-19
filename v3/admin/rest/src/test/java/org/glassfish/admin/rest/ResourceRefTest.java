/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.glassfish.admin.rest;

import com.sun.jersey.api.client.ClientResponse;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jasonlee
 */
public class ResourceRefTest extends RestTestBase {
    public static final String URL_CREATE_INSTANCE = BASE_URL_DOMAIN + "/create-instance";
    public static final String URL_JDBC_RESOURCE = BASE_URL_DOMAIN + "/resources/jdbc-resource";
    public static final String URL_RESOURCE_REF = BASE_URL_DOMAIN + "/servers/server/server/resource-ref";
    public static final String URL_DELETE_INSTANCE = "";

    @Test
    public void testCreatingResourceRef() {
        final String instanceName = "instance_" + generateRandomString();
        final String jdbcResourceName = "jdbc_" + generateRandomString();
        Map<String, String> newInstance = new HashMap<String, String>() {{
            put("id", instanceName);
            put("node", "localhost");
        }};
        Map<String, String> jdbcResource = new HashMap<String, String>() {{
            put("id", jdbcResourceName);
            put("connectionpoolid", "DerbyPool");
            put("target", instanceName);
        }};
        Map<String, String> resourceRef = new HashMap<String, String>() {{
            put("id", jdbcResourceName);
            put("target", "server");
        }};

        try {
            ClientResponse response = post(URL_CREATE_INSTANCE, newInstance);
            assertTrue(isSuccess(response));

            response = post(URL_JDBC_RESOURCE, jdbcResource);
            assertTrue(isSuccess(response));
 
            response = post(URL_RESOURCE_REF, resourceRef);
            assertTrue(isSuccess(response));
        } finally {
            ClientResponse response = delete(BASE_URL_DOMAIN + "/servers/server/" + instanceName + "/resource-ref/" + jdbcResourceName, new HashMap<String, String>() {{ put("target", instanceName); }});
            assertTrue(isSuccess(response));
            response = get(BASE_URL_DOMAIN + "/servers/server/" + instanceName + "/resource-ref/" + jdbcResourceName);
            assertFalse(isSuccess(response));

            response = delete(URL_JDBC_RESOURCE + "/" + jdbcResourceName);
            assertTrue(isSuccess(response));
            response = get(URL_JDBC_RESOURCE + "/" + jdbcResourceName);
            assertFalse(isSuccess(response));
            
            response = delete(BASE_URL_DOMAIN + "/servers/server/" + instanceName + "/delete-instance");
            assertTrue(isSuccess(response));
            response = get(BASE_URL_DOMAIN + "/servers/server/" + instanceName);
            assertFalse(isSuccess(response));
        }
    }
}
