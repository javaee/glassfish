/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.jdbcra.spi;

import javax.resource.spi.endpoint.MessageEndpointFactory;
import javax.resource.spi.ActivationSpec;
import javax.resource.NotSupportedException;
import javax.transaction.xa.XAResource;
import javax.resource.spi.BootstrapContext;
import javax.resource.spi.ResourceAdapterInternalException;

/**
 * <code>ResourceAdapter</code> implementation for Generic JDBC Connector.
 *
 * @version	1.0, 02/08/05
 * @author	Evani Sai Surya Kiran
 */
public class ResourceAdapter implements javax.resource.spi.ResourceAdapter {

    public String raProp = null;

    /**
     * Empty method implementation for endpointActivation
     * which just throws <code>NotSupportedException</code>
     *
     * @param	mef	<code>MessageEndpointFactory</code>
     * @param	as	<code>ActivationSpec</code>
     * @throws	<code>NotSupportedException</code>
     */
    public void endpointActivation(MessageEndpointFactory mef, ActivationSpec as) throws NotSupportedException {
        throw new NotSupportedException("This method is not supported for this JDBC connector");
    }
    
    /**
     * Empty method implementation for endpointDeactivation
     *
     * @param	mef	<code>MessageEndpointFactory</code>
     * @param	as	<code>ActivationSpec</code>
     */
    public void endpointDeactivation(MessageEndpointFactory mef, ActivationSpec as) {
        
    }
    
    /**
     * Empty method implementation for getXAResources
     * which just throws <code>NotSupportedException</code>
     *
     * @param	specs	<code>ActivationSpec</code> array
     * @throws	<code>NotSupportedException</code>
     */
    public XAResource[] getXAResources(ActivationSpec[] specs) throws NotSupportedException {
        throw new NotSupportedException("This method is not supported for this JDBC connector");
    }
    
    /**
     * Empty implementation of start method
     *
     * @param	ctx	<code>BootstrapContext</code>
     */
    public void start(BootstrapContext ctx) throws ResourceAdapterInternalException {
        System.out.println("Resource Adapter is starting with configuration :" + raProp);
        if (raProp == null || !raProp.equals("VALID")) {
	    throw new ResourceAdapterInternalException("Resource adapter cannot start. It is configured as : " + raProp);
	} 
    }
    
    /**
     * Empty implementation of stop method
     */
    public void stop() {
    
    }

    public void setRAProperty(String s) {
        raProp = s;
    }

    public String getRAProperty() {
        return raProp;
    }
    
}
