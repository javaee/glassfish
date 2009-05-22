/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
package com.sun.jdbcra.spi;

import javax.resource.spi.ManagedConnectionFactory;
import javax.resource.spi.ManagedConnection;
import javax.resource.ResourceException;
import javax.resource.spi.ConnectionRequestInfo;

/**
 * ConnectionManager implementation for Generic JDBC Connector.
 *
 * @version	1.0, 02/07/31
 * @author	Binod P.G
 */
public class ConnectionManager implements javax.resource.spi.ConnectionManager{ 

    /**
     * Returns a <code>Connection </code> object to the <code>ConnectionFactory</code>
     *
     * @param	mcf	<code>ManagedConnectionFactory</code> object.
     * @param	info	<code>ConnectionRequestInfo</code> object.
     * @return	A <code>Connection</code> Object.
     * @throws	ResourceException In case of an error in getting the <code>Connection</code>.
     */
    public Object allocateConnection(ManagedConnectionFactory mcf,
    				     ConnectionRequestInfo info) 
    				     throws ResourceException {
	ManagedConnection mc = mcf.createManagedConnection(null, info);
	return mc.getConnection(null, info);    				     
    }
    
    /*
     * This class could effectively implement Connection pooling also.
     * Could be done for FCS.
     */
}
