/*
 * Copyright 2004-2005 Sun Microsystems, Inc.  All rights reserved.
 * Use is subject to license terms.
 */


package com.sun.enterprise.webservice.monitoring.global;

/**
 *
 * @author dochez
 */
public class InvocationTrace {
    /**
     * Holds value of property request.
     */
    protected String request;

    /**
     * Getter for property request.
     * @return Value of property request.
     */
    public String getRequest() {

        return this.request;
    }

    /**
     * Setter for property request.
     * @param request New value of property request.
     */
    public void setRequest(String request) {

        this.request = request;
    }

    /**
     * Holds value of property response.
     */
    protected String response;

    /**
     * Getter for property response.
     * @return Value of property response.
     */
    public String getResponse() {

        return this.response;
    }

    /**
     * Setter for property response.
     * @param response New value of property response.
     */
    public void setResponse(String response) {

        this.response = response;
    }

    /**
     * Holds value of property endpointInfo.
     */
    protected EndpointInfo endpointInfo;

    /**
     * Getter for property endpointInfo.
     * @return Value of property endpointInfo.
     */
    public EndpointInfo getEndpointInfo() {

        return this.endpointInfo;
    }

    /**
     * Setter for property endpointInfo.
     * @param endpointInfo New value of property endpointInfo.
     */
    public void setEndpointInfo(EndpointInfo endpointInfo) {

        this.endpointInfo = endpointInfo;
    }
    
    
}
