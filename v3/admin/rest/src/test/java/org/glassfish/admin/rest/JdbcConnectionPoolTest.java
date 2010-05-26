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
public class JdbcConnectionPoolTest extends RestTestBase {
    public static final String BASE_JDBC_CP_URL = BASE_URL+"/domain/resources/jdbc-connection-pool";
    @Test
    public void testReading() {
        Map<String, String> entity = getEntityValues(get(BASE_JDBC_CP_URL + "/__TimerPool"));
        assertEquals("__TimerPool", entity.get("name"));
    }

    @Test
    public void testCreateAndDelete() {
        String poolName = "TestPool" + generateRandomString();
        Map<String, String> params = new HashMap<String, String>();
        params.put("name", poolName);
        params.put("datasourceClassname","org.apache.derby.jdbc.ClientDataSource");
        ClientResponse response = post(BASE_JDBC_CP_URL, params);
        assertEquals(201, response.getStatus());

        Map<String, String> entity = getEntityValues(get(BASE_JDBC_CP_URL + "/"+poolName));
        assertFalse(entity.size() == 0);

        response = delete(BASE_JDBC_CP_URL+"/"+poolName, new HashMap<String, String>());
        assertEquals(response.getStatus(), 200);

        entity = getEntityValues(get(BASE_JDBC_CP_URL + "/"+poolName));
        assertEquals(entity.size(), 0);
    }
}
