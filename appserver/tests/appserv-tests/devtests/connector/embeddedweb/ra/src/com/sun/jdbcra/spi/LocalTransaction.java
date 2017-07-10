/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2003-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
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
