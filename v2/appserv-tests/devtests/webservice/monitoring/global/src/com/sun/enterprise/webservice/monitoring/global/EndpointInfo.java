/*
 * Copyright 2004-2005 Sun Microsystems, Inc.  All rights reserved.
 * Use is subject to license terms.
 */

package com.sun.enterprise.webservice.monitoring.global;

/** 
 * Value type for an endpoint information
 *
 * @author Jerome Dochez
 */
public class EndpointInfo {
    
    /** Creates a new instance of Endpoint */
    public EndpointInfo() {
    }

    /**
     * Holds value of property endpointSelector.
     */
    private String endpointSelector;

    /**
     * Getter for property endpointSelector.
     * @return Value of property endpointSelector.
     */
    public String getEndpointSelector() {

        return this.endpointSelector;
    }

    /**
     * Setter for property endpointSelector.
     * @param endpointSelector New value of property endpointSelector.
     */
    public void setEndpointSelector(String endpointSelector) {

        this.endpointSelector = endpointSelector;
    }

    /**
     * Holds value of property endpointType.
     */
    private String endpointType;

    /**
     * Getter for property endpointType.
     * @return Value of property endpointType.
     */
    public String getEndpointType() {

        return this.endpointType;
    }

    /**
     * Setter for property endpointType.
     * @param endpointType New value of property endpointType.
     */
    public void setEndpointType(String endpointType) {

        this.endpointType = endpointType;
    }
    
    
    
}
