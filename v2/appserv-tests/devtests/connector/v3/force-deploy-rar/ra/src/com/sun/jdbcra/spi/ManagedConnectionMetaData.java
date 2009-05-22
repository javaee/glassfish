/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.jdbcra.spi;

import com.sun.jdbcra.spi.ManagedConnection;
import java.sql.SQLException;
import javax.resource.ResourceException;

import java.util.logging.Logger;
import java.util.logging.Level;
/**
 * <code>ManagedConnectionMetaData</code> implementation for Generic JDBC Connector.
 *
 * @version	1.0, 02/08/03
 * @author	Evani Sai Surya Kiran
 */
public class ManagedConnectionMetaData implements javax.resource.spi.ManagedConnectionMetaData {

    private java.sql.DatabaseMetaData dmd = null;
    private ManagedConnection mc;

    private static Logger _logger;
    static {
        _logger = Logger.getAnonymousLogger();
    }
    private boolean debug = false;
    /**
     * Constructor for <code>ManagedConnectionMetaData</code>
     *
     * @param	mc	<code>ManagedConnection</code>
     * @throws	<code>ResourceException</code>	if getting the DatabaseMetaData object fails
     */
    public ManagedConnectionMetaData(ManagedConnection mc) throws ResourceException {
        try {
            this.mc = mc;
            dmd = mc.getActualConnection().getMetaData();
        } catch(SQLException sqle) {
	    _logger.log(Level.SEVERE, "jdbc.exc_md");
            throw new ResourceException(sqle.getMessage());
        }
    }
    
    /**
     * Returns product name of the underlying EIS instance connected
     * through the ManagedConnection.
     *
     * @return	Product name of the EIS instance
     * @throws	<code>ResourceException</code>
     */
    public String getEISProductName() throws ResourceException {
        try {
            return dmd.getDatabaseProductName();
        } catch(SQLException sqle) {
	    _logger.log(Level.SEVERE, "jdbc.exc_eis_prodname", sqle);
            throw new ResourceException(sqle.getMessage());
        }
    }
    
    /**
     * Returns product version of the underlying EIS instance connected
     * through the ManagedConnection.
     *
     * @return	Product version of the EIS instance
     * @throws	<code>ResourceException</code>
     */
    public String getEISProductVersion() throws ResourceException {
        try {
            return dmd.getDatabaseProductVersion();
        } catch(SQLException sqle) {
	    _logger.log(Level.SEVERE, "jdbc.exc_eis_prodvers", sqle);
            throw new ResourceException(sqle.getMessage(), sqle.getMessage());
        }
    }
    
    /**
     * Returns maximum limit on number of active concurrent connections
     * that an EIS instance can support across client processes.
     *
     * @return	Maximum limit for number of active concurrent connections
     * @throws	<code>ResourceException</code>
     */
    public int getMaxConnections() throws ResourceException {
        try {
            return dmd.getMaxConnections();
        } catch(SQLException sqle) {
	    _logger.log(Level.SEVERE, "jdbc.exc_eis_maxconn");
            throw new ResourceException(sqle.getMessage());
        }
    }
    
    /**
     * Returns name of the user associated with the ManagedConnection instance. The name
     * corresponds to the resource principal under whose whose security context, a connection
     * to the EIS instance has been established.
     *
     * @return	name of the user
     * @throws	<code>ResourceException</code>
     */
    public String getUserName() throws ResourceException {
        javax.resource.spi.security.PasswordCredential pc = mc.getPasswordCredential();
        if(pc != null) {
            return pc.getUserName();
        }
        
        return mc.getManagedConnectionFactory().getUser();
    }
}
