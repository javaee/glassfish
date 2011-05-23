/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.glassfish.admin.rest.client;

import com.sun.jersey.api.client.Client;

/**
 *
 * @author jasonlee
 */
public abstract class RestLeafCollection extends RestClientBase {
    public RestLeafCollection(Client c, RestClientBase p) {
        super(c, p);
    }
}
