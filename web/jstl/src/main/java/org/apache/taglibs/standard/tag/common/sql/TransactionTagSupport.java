/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * Portions Copyright Apache Software Foundation.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
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

package org.apache.taglibs.standard.tag.common.sql;

import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.TagSupport;
import javax.servlet.jsp.tagext.TryCatchFinally;
import javax.sql.DataSource;

import org.apache.taglibs.standard.resources.Resources;


/**
 * <p>Tag handler for &lt;Transaction&gt; in JSTL.  
 * 
 * @author Hans Bergsten
 */

public abstract class TransactionTagSupport extends TagSupport 
    implements TryCatchFinally {

    //*********************************************************************
    // Private constants

    private static final String TRANSACTION_READ_COMMITTED
	= "read_committed";
    private static final String TRANSACTION_READ_UNCOMMITTED
	= "read_uncommitted";
    private static final String TRANSACTION_REPEATABLE_READ
	= "repeatable_read";
    private static final String TRANSACTION_SERIALIZABLE
	= "serializable";


    //*********************************************************************
    // Protected state

    protected Object rawDataSource;
    protected boolean dataSourceSpecified;


    //*********************************************************************
    // Private state

    private Connection conn;
    private int isolation;
    private int origIsolation;


    //*********************************************************************
    // Constructor and initialization

    public TransactionTagSupport() {
	super();
	init();
    }

    private void init() {
	conn = null;
	dataSourceSpecified = false;
	rawDataSource = null;
	isolation = Connection.TRANSACTION_NONE;
    }


    //*********************************************************************
    // Tag logic

    /**
     * Prepares for execution by setting the initial state, such as
     * getting the <code>Connection</code> and preparing it for
     * the transaction.
     */
    public int doStartTag() throws JspException {

	if ((rawDataSource == null) && dataSourceSpecified) {
	    throw new JspException(
                Resources.getMessage("SQL_DATASOURCE_NULL"));
	}

        DataSource dataSource = DataSourceUtil.getDataSource(rawDataSource,
							     pageContext);

	try {
	    conn = dataSource.getConnection();
	    origIsolation = conn.getTransactionIsolation();
	    if (origIsolation == Connection.TRANSACTION_NONE) {
		throw new JspTagException(
                    Resources.getMessage("TRANSACTION_NO_SUPPORT"));
	    }
	    if ((isolation != Connection.TRANSACTION_NONE)
		    && (isolation != origIsolation)) {
		conn.setTransactionIsolation(isolation);
	    }
	    conn.setAutoCommit(false);
	} catch (SQLException e) {
	    throw new JspTagException(
                Resources.getMessage("ERROR_GET_CONNECTION",
				     e.toString()), e);
	} 

	return EVAL_BODY_INCLUDE;
    }

    /**
     * Commits the transaction.
     */
    public int doEndTag() throws JspException {
	try {
	    conn.commit();
	} catch (SQLException e) {
	    throw new JspTagException(
                Resources.getMessage("TRANSACTION_COMMIT_ERROR",
				     e.toString()), e);
	}
	return EVAL_PAGE;
    }

    /**
     * Rollbacks the transaction and rethrows the Throwable.
     */
    public void doCatch(Throwable t) throws Throwable {
	if (conn != null) {
	    try {
		conn.rollback();
	    } catch (SQLException e) {
		// Ignore to not hide orignal exception
	    }
	}
	throw t;
    }

    /**
     * Restores the <code>Connection</code> to its initial state and
     * closes it.
     */
    public void doFinally() {
	if (conn != null) {
	    try {
		if ((isolation != Connection.TRANSACTION_NONE)
		        && (isolation != origIsolation)) {
		    conn.setTransactionIsolation(origIsolation);
		}
		conn.setAutoCommit(true);
		conn.close();
	    } catch (SQLException e) {
		// Not much we can do
	    }
	}
	conn = null;
    }

    // Releases any resources we may have (or inherit)
    public void release() {
	init();
    }


    //*********************************************************************
    // Public utility methods

    /**
     * Setter method for the transaction isolation level.
     */
    public void setIsolation(String iso) throws JspTagException {

	if (TRANSACTION_READ_COMMITTED.equals(iso)) {
	    isolation = Connection.TRANSACTION_READ_COMMITTED;
	} else if (TRANSACTION_READ_UNCOMMITTED.equals(iso)) {
	    isolation = Connection.TRANSACTION_READ_UNCOMMITTED;
	} else if (TRANSACTION_REPEATABLE_READ.equals(iso)) {
	    isolation = Connection.TRANSACTION_REPEATABLE_READ;
	} else if (TRANSACTION_SERIALIZABLE.equals(iso)) {
	    isolation = Connection.TRANSACTION_SERIALIZABLE;
	} else {
	    throw new JspTagException(
                Resources.getMessage("TRANSACTION_INVALID_ISOLATION"));
	}
    }

    /**
     * Called by nested parameter elements to get a reference to
     * the Connection.
     */
    public Connection getSharedConnection() {
	return conn;
    }
}
