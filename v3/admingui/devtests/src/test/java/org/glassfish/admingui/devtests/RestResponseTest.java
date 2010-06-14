/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.glassfish.admingui.devtests;

import java.util.HashMap;
import org.glassfish.admingui.common.handlers.RestApiHandlers;
import org.glassfish.admingui.common.util.RestResponse;
import org.junit.Test;

import static org.junit.Assert.*;


/**
 *
 * @author jasonlee
 */
public class RestResponseTest {
    public static final String BASE_URL = "http://localhost:4848/management/domain";
    static final String URL_UPTIME = BASE_URL + "/uptime";
    static final String URL_GENERATE_JVM_REPORT = BASE_URL + "/configs/config/server-config/java-config/generate-jvm-report";

    @Test
    public void testPostCommand() {
        RestResponse response = RestApiHandlers.post(URL_GENERATE_JVM_REPORT, new HashMap<String, Object>(){{
            put ("type", "summary");
        }});
        assertTrue(response.getResponseBody().contains("GlassFish REST Interface"));
    }

    @Test
    public void testGetCommand() {
        RestResponse response = RestApiHandlers.get(URL_UPTIME);
        assertTrue(response.getResponseBody().contains("Uptime:"));
    }
}