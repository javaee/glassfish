/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.jdbcra.spi;

import com.sun.jdbcra.spi.ManagedConnection;
import javax.resource.ResourceException;
import javax.resource.spi.LocalTransactionException;

/**
 * <code>LocalTransaction</code> implementation for Generic JDBC Connector.
 *
 * @version	1.0, 02/08/03
 * @author	Evani Sai Surya Kiran
 */
public class LocalTransaction implements javax.resource.spi.LocalTransaction {
    
    private ManagedConnection mc;
    
    /**
     * Constructor for <code>LocalTransaction</code>.
     * @param	mc	<code>ManagedConnection</code> that returns
     *			this <code>LocalTransaction</code> object as
     *			a result of <code>getLocalTransaction</code>
     */
    public LocalTransaction(ManagedConnection mc) {
        this.mc = mc;
    }
    
    /**
     * Begin a local transaction.
     *
     * @throws	LocalTransactionException	if there is an error in changing
     *						the autocommit mode of the physical
     *						connection
     */
    public void begin() throws ResourceException {
        //GJCINT
	mc.transactionStarted();
        try {
            mc.getActualConnection().setAutoCommit(false);
        } catch(java.sql.SQLException sqle) {
            throw new LocalTransactionException(sqle.getMessage());
        }
    }
    
    /**
     * Commit a local transaction.
     * @throws	LocalTransactionException	if there is an error in changing
     *						the autocommit mode of the physical
     *						connection or committing the transaction
     */
    public void commit() throws ResourceException {
        Exception e = null;
        try {
            mc.getActualConnection().commit();
            mc.getActualConnection().setAutoCommit(true);
        } catch(java.sql.SQLException sqle) {
            throw new LocalTransactionException(sqle.getMessage());
        }
        //GJCINT
	mc.transactionCompleted();
    }
    
    /**
     * Rollback a local transaction.
     * 
     * @throws	LocalTransactionException	if there is an error in changing
     *						the autocommit mode of the physical
     *						connection or rolling back the transaction
     */
    public void rollback() throws ResourceException {
        try {
            mc.getActualConnection().rollback();
            mc.getActualConnection().setAutoCommit(true);
        } catch(java.sql.SQLException sqle) {
            throw new LocalTransactionException(sqle.getMessage());
        }
        //GJCINT
	mc.transactionCompleted();
    }
    
}
