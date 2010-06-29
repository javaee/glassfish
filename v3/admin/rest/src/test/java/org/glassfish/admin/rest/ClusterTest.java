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
public class ClusterTest extends RestTestBase {
    public static final String URL_CLUSTER = BASE_URL + "/clusters/cluster";
    @Test
    public void testDomainCreationAndDeletion() {
        final String clusterName = "cluster_" + generateRandomString();
        Map<String, String> newCluster = new HashMap<String, String>() {{
           put ("id", clusterName);
        }};

        ClientResponse response = create(URL_CLUSTER, newCluster);
        assertTrue(isSuccess(response));

        Map<String, String> entity = getEntityValues(get(URL_CLUSTER + "/" + clusterName));
        assertEquals(clusterName+"-config", entity.get("configRef"));

        response = post(URL_CLUSTER + "/" + clusterName + "/delete-cluster", null);
        assertTrue(isSuccess(response));

        response = get(URL_CLUSTER + "/" + clusterName);
        assertFalse(isSuccess(response));
    }
}
