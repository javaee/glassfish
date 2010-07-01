/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.glassfish.admin.rest;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import javax.ws.rs.core.MultivaluedMap;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jasonlee
 */
public class ConfigTest extends RestTestBase {

    public static final String BASE_CONFIGS_URL = BASE_URL + "/configs";

    @Test
    public void testConfigCopy() {
        String configName = "config-" + generateRandomString();
        MultivaluedMap formData = new MultivaluedMapImpl();
        formData.add("id", "default-config");
        formData.add("id", configName);
        ClientResponse response = post(BASE_CONFIGS_URL + "/copy-config", formData);
        assertTrue(isSuccess(response));

        response = get(BASE_CONFIGS_URL + "/config/" + configName);
        assertTrue(isSuccess(response));

        response = post(BASE_CONFIGS_URL + "/config/" + configName + "/delete-config", null);
        assertTrue(isSuccess(response));

        response = get(BASE_CONFIGS_URL + "/config/" + configName);
        assertFalse(isSuccess(response));
    }
}
