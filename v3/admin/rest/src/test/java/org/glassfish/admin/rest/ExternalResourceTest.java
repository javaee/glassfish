/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.glassfish.admin.rest;

import com.sun.jersey.api.client.ClientResponse;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;

/**
 *
 * @author jasonlee
 */
public class ExternalResourceTest extends RestTestBase {
    protected static final String URL_EXTERNAL_RESOURCE = "/domain/resources/external-jndi-resource";
    @Test
    public void createAndDeleteExternalResource() {
        final String resourceName = "resource_" + generateRandomString();
        final String jndiName = "jndi/"+resourceName;
        Map<String, String> newResource = new HashMap<String, String>() {{
            put("id", resourceName);
            put("jndilookupname", jndiName);
            put("factoryClass", "org.glassfish.resources.custom.factory.PrimitivesAndStringFactory");
            put("restype", "java.lang.Double");
        }};
        ClientResponse response = post (URL_EXTERNAL_RESOURCE, newResource);
        checkStatusForSuccess(response);

        response = get(URL_EXTERNAL_RESOURCE + "/" + resourceName);
        checkStatusForSuccess(response);

        response = delete(URL_EXTERNAL_RESOURCE + "/" + resourceName);
        checkStatusForSuccess(response);
    }
}
