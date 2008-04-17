/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 *
 *
 * This file incorporates work covered by the following copyright and
 * permission notice:
 *
 * Copyright 2004 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.taglibs.standard.tag.common.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.jstl.sql.SQLExecutionTag;
import javax.servlet.jsp.tagext.BodyTagSupport;
import javax.servlet.jsp.tagext.TryCatchFinally;
import javax.sql.DataSource;

import org.apache.taglibs.standard.resources.Resources;
import org.apache.taglibs.standard.tag.common.core.Util;

/**
 * <p>Tag handler for &lt;Update&gt; in JSTL.  
 * 
 * @author Hans Bergsten
 * @author Justyna Horwat
 */

public abstract class UpdateTagSupport extends BodyTagSupport 
    implements TryCatchFinally, SQLExecutionTag {

    private String var;
    private int scope;

    /*
     * The following properties take expression values, so the
     * setter methods are implemented by the expression type
     * specific subclasses.
     */
    protected Object rawDataSource;
    protected boolean dataSourceSpecified;
    protected String sql;

    /*
     * Instance variables that are not for attributes
     */
    private Connection conn;
    private List parameters;
    private boolean isPartOfTransaction;


    //*********************************************************************
    // Constructor and initialization

    public UpdateTagSupport() {
	super();
	init();
    }

    private void init() {
	rawDataSource = null;
	sql = null;
	conn = null;
	parameters = null;
	isPartOfTransaction = dataSourceSpecified = false;
        scope = PageContext.PAGE_SCOPE;
	var = null;
    }


    //*********************************************************************
    // Accessor methods

    /**
     * Setter method for the name of the variable to hold the
     * result.
     */
    public void setVar(String var) {
	this.var = var;
    }

    /**
     * Setter method for the scope of the variable to hold the
     * result.
     */
    public void setScope(String scopeName) {
        scope = Util.getScope(scopeName);
    }


    //*********************************************************************
    // Tag logic

    /**
     * Prepares for execution by setting the initial state, such as
     * getting the <code>Connection</code>
     */
    public int doStartTag() throws JspException {

	try {
	    conn = getConnection();
	} catch (SQLException e) {
	    throw new JspException(sql + ": " + e.getMessage(), e);
	}

	return EVAL_BODY_BUFFERED;
    }

    /**
     * <p>Execute the SQL statement, set either through the <code>sql</code>
     * attribute or as the body, and save the result as a variable
     * named by the <code>var</code> attribute in the scope specified
     * by the <code>scope</code> attribute, as an object that implements
     * the Result interface.
     *
     * <p>The connection used to execute the statement comes either
     * from the <code>DataSource</code> specified by the
     * <code>dataSource</code> attribute, provided by a parent action
     * element, or is retrieved from a JSP scope  attribute
     * named <code>javax.servlet.jsp.jstl.sql.dataSource</code>.
     */
    public int doEndTag() throws JspException {
	/*
	 * Use the SQL statement specified by the sql attribute, if any,
	 * otherwise use the body as the statement.
	 */
	String sqlStatement = null;
	if (sql != null) {
	    sqlStatement = sql;
	}
	else if (bodyContent != null) {
	    sqlStatement = bodyContent.getString();
	}
	if (sqlStatement == null || sqlStatement.trim().length() == 0) {
	    throw new JspTagException(
                Resources.getMessage("SQL_NO_STATEMENT"));
	}

	int result = 0;
	try {
	    PreparedStatement ps = conn.prepareStatement(sqlStatement);
	    setParameters(ps, parameters);
	    result = ps.executeUpdate();
	}
	catch (Throwable e) {
	    throw new JspException(sqlStatement + ": " + e.getMessage(), e);
	}
	if (var != null)
            pageContext.setAttribute(var, Integer.valueOf(result), scope);
	return EVAL_PAGE;
    }

    /**
     * Just rethrows the Throwable.
     */
    public void doCatch(Throwable t) throws Throwable {
	throw t;
    }

    /**
     * Close the <code>Connection</code>, unless this action is used
     * as part of a transaction.
     */
    public void doFinally() {
	if (conn != null && !isPartOfTransaction) {
	    try {
		conn.close();
	    } catch (SQLException e) {
		// Not much we can do
	    }
	}

	parameters = null;
	conn = null;
    }


    //*********************************************************************
    // Public utility methods

    /**
     * Called by nested parameter elements to add PreparedStatement
     * parameter values.
     */
    public void addSQLParameter(Object o) {
	if (parameters == null) {
	    parameters = new ArrayList();
	}
	parameters.add(o);
    }


    //*********************************************************************
    // Private utility methods

    private Connection getConnection() throws JspException, SQLException {
	// Fix: Add all other mechanisms
	Connection conn = null;
	isPartOfTransaction = false;

	TransactionTagSupport parent = (TransactionTagSupport) 
	    findAncestorWithClass(this, TransactionTagSupport.class);
	if (parent != null) {
            if (dataSourceSpecified) {
                throw new JspTagException(
                    Resources.getMessage("ERROR_NESTED_DATASOURCE"));
            }
	    conn = parent.getSharedConnection();
            isPartOfTransaction = true;
	} else {
	    if ((rawDataSource == null) && dataSourceSpecified) {
		throw new JspException(
		    Resources.getMessage("SQL_DATASOURCE_NULL"));
	    }
	    DataSource dataSource = DataSourceUtil.getDataSource(rawDataSource,
								 pageContext);
            try {
                conn = dataSource.getConnection();
            } catch (Exception ex) {
                throw new JspException(
                    Resources.getMessage("DATASOURCE_INVALID",
					 ex.toString()));
            }
	}

	return conn;
    }

    private void setParameters(PreparedStatement ps, List parameters) 
        throws SQLException
    {
	if (parameters != null) {
	    for (int i = 0; i < parameters.size(); i++) {
                /* The first parameter has index 1.  If a null
                 * is passed to setObject the parameter will be
                 * set to JDBC null so an explicit call to
                 * ps.setNull is not required.
                 */
                ps.setObject(i + 1, parameters.get(i));
	    }
	}
    }
}
