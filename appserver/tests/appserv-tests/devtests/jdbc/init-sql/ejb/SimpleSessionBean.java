/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.s1asdev.jdbc.initsql.ejb;

import javax.ejb.*;
import javax.naming.*;
import javax.sql.*;
import java.rmi.*;
import java.util.*;
import java.sql.*;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

public class SimpleSessionBean implements SessionBean
{

    private SessionContext context;
    private InitialContext ic;
    private DataSource ds1; 

    public void setSessionContext(SessionContext ctxt) {
        this.context = ctxt;
	try {
	    ic = new InitialContext();
	    ds1 = (com.sun.appserv.jdbc.DataSource)ic.lookup("java:comp/env/DataSource1");
	} catch( Exception ne ) {
	    ne.printStackTrace();
	}
    }

    public void ejbCreate() throws CreateException {
    }

    /**
     * Test to select the names from a table. 
     *
     * The result set would contain different number of rows based on 
     * a session property set during the initialization sql phase. 
     * Based on the property set, the number of rows are compared to 
     * test the feature.
     */
    public boolean test1(boolean caseSensitive) {
        Connection con = null;
	Statement stmt = null;
	ResultSet rs = null;
	String query = "Select name from WORKERS where name='Joy Joy'";
	boolean result = false;
	int size = 0;
	try {
	    con = ds1.getConnection();
	    stmt = con.createStatement();
	    rs = stmt.executeQuery(query);
	    if(rs != null) {
		while(rs.next()) {
		    size++;
		}
	    }
	    if(caseSensitive) {
	        result = size == 1;
	    } else {
		result = size == 3;
	    }
	} catch (SQLException ex) {
	    result = false;
	    ex.printStackTrace();
	} finally {
            if(rs != null) {
		try {
		    rs.close();
		} catch(Exception ex) {}
	    }
	    if(stmt != null) {
		try {
		    stmt.close();
		} catch(Exception ex) {}
	    }
	    if(con != null) {
		try {
		    stmt.close();
		} catch(Exception ex) {}
	    }
	}
	return result;
    }

    public void ejbStore() {}
    public void ejbRemove() {}
    public void ejbActivate() {}
    public void ejbPassivate() {}
    public void unsetEntityContext() {}
    public void ejbPostCreate() {}
}
