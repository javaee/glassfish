/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.glassfish.admin.rest;

/**
 *
 * @author jasonlee
 */
public class CliFailureException extends RuntimeException {

    public CliFailureException(String message) {
        super(message);
//        super(Response.status(Status.INTERNAL_SERVER_ERROR).
//                entity(message).type("text/plain").build());
    }
}
