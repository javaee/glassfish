/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.glassfish.admin.rest.client;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;

/**
 *
 * @author jasonlee
 */
public class RestClient {
    protected String host;
    protected int port;
    protected static String baseUrl;
    protected Client client;
    protected boolean useSsl = false;

    public RestClient() {
        this("localhost", 4848, false, null, null);
    }

    public RestClient(String host, int port, boolean useSsl) {
        this(host, port, useSsl, null, null);
    }

    public RestClient(String host, int port, boolean useSsl, String user, String password) {
        this.host = host;
        this.port = port;
        this.useSsl = useSsl;
        client = Client.create();
        if (user != null) {
            client.addFilter(new HTTPBasicAuthFilter(user, password));
        }
    }

//    public Domain getDomain() {
//        return new Domain(client, this);
//    }

    public String getRestUrl() {
        return (useSsl ? "https" : "http") + "://" + host + ":" + port + "/management";
    }
}
