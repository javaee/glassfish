/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.glassfish.admin.rest;

import com.sun.jersey.api.client.ClientResponse;
import java.util.HashMap;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jasonlee
 */
public class NetworkListenerTest extends RestTestBase {
    protected static final String URL_PROTOCOL = "/domain/configs/config/server-config/network-config/protocols/protocol";
    @Test
    public void createHttpListener() {
        final String redirectProtocolName = "protocol_" + generateRandomString();
        final String redirectFilterName = "filter_" + generateRandomString();
        final String portUniProtocolName = "protocol_" + generateRandomString();
        final String finderName1 = "finder" + generateRandomString();
        final String finderName2 = "finder" + generateRandomString();

        try {
// asadmin commands taken from: http://www.antwerkz.com/port-unification-in-glassfish-3-part-1/
//        asadmin create-protocol --securityenabled=false http-redirect
//        asadmin create-protocol --securityenabled=false pu-protocol
            ClientResponse response = post(URL_PROTOCOL, new HashMap<String, String>() {{ put ("securityenabled", "false"); put("id", redirectProtocolName); }});
            checkStatusForSuccess(response);
            response = post(URL_PROTOCOL, new HashMap<String, String>() {{ put ("securityenabled", "false"); put("id", portUniProtocolName); }});
            checkStatusForSuccess(response);

//        asadmin create-protocol-filter --protocol http-redirect --classname com.sun.grizzly.config.HttpRedirectFilter redirect-filter
            response = post (URL_PROTOCOL + "/" + redirectProtocolName + "/create-protocol-filter",
                new HashMap<String, String>() {{
                    put ("id", redirectFilterName);
                    put ("protocol", redirectProtocolName);
                    put ("classname", "com.sun.grizzly.config.HttpRedirectFilter");
                }});
            checkStatusForSuccess(response);

//        asadmin create-protocol-finder --protocol pu-protocol --target-protocol http-listener-2 --classname com.sun.grizzly.config.HttpProtocolFinder http-finder
//        asadmin create-protocol-finder --protocol pu-protocol --target-protocol http-redirect --classname com.sun.grizzly.config.HttpProtocolFinder http-redirect
            response = post (URL_PROTOCOL + "/" + portUniProtocolName + "/create-protocol-finder",
                new HashMap<String, String>() {{
                    put ("id", finderName1);
                    put ("protocol", portUniProtocolName);
                    put ("target-protocol", "http-listener-2");
                    put ("classname", "com.sun.grizzly.config.HttpProtocolFinder");
                }});
            checkStatusForSuccess(response);
            response = post (URL_PROTOCOL + "/" + portUniProtocolName + "/create-protocol-finder",
                new HashMap<String, String>() {{
                    put ("id", finderName2);
                    put ("protocol", portUniProtocolName);
                    put ("target-protocol", redirectProtocolName);
                    put ("classname", "com.sun.grizzly.config.HttpProtocolFinder");
                }});
            checkStatusForSuccess(response);


//        asadmin set configs.config.server-config.network-config.network-listeners.network-listener.http-listener-1.protocol=pu-protocol
            response = post("/domain", new HashMap<String, String>() {{
                put("configs.config.server-config.network-config.network-listeners.network-listener.http-listener-1.protocol", "pu-protocol");
            }});

            response = get("/domain/configs/config/server-config/network-config/network-listeners/network-listener/http-listener-1/find-http-protocol");
            checkStatusForSuccess(response);
            String retvalue = response.getEntity(String.class);
            assertTrue(retvalue.contains("property name=\"protocol\" value=\"http-listener-2\""));
            System.out.println(retvalue);
        } finally {
            ClientResponse response = delete(URL_PROTOCOL + "/" + portUniProtocolName + "/delete-protocol-finder",
                new HashMap<String, String>() {{
                    put("protocol", portUniProtocolName);
                    put("id", finderName1);
                }} );
            checkStatusForSuccess(response);
            response = delete(URL_PROTOCOL + "/" + portUniProtocolName + "/delete-protocol-finder",
                new HashMap<String, String>() {{
                    put("protocol", portUniProtocolName);
                    put("id", finderName2);
                }} );
            checkStatusForSuccess(response);
            response = delete(URL_PROTOCOL + "/" + redirectProtocolName + "/protocol-chain-instance-handler/protocol-chain/protocol-filter/" + redirectFilterName,
                    new HashMap<String, String>() {{ put("protocol", redirectProtocolName); }} );
            checkStatusForSuccess(response);
            response = delete(URL_PROTOCOL + "/" + portUniProtocolName);
            checkStatusForSuccess(response);
            response = delete(URL_PROTOCOL + "/" + redirectProtocolName);
            checkStatusForSuccess(response);
        }
        
    }
}
