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
public abstract class RestLeaf extends RestClientBase {

    public RestLeaf(Client c, RestClientBase p) {
        super(c, p);
    }

}
