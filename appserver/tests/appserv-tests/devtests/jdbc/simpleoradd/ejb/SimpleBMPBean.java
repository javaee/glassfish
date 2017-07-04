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

package com.sun.s1asdev.jdbc.simpleora.ejb;

import javax.ejb.*;
import javax.naming.*;
import javax.sql.*;
import java.rmi.*;
import java.util.*;
import java.sql.*;

public class SimpleBMPBean
    implements EntityBean
{

    protected DataSource ds;
    protected DataSource ds1;

    public void setEntityContext(EntityContext entityContext) {
	Context context = null;
	try {
	    context    = new InitialContext();
	    ds = (DataSource) context.lookup("java:comp/env/DataSource");
	    ds1 = (DataSource) context.lookup("java:comp/env/DataSource1");
	} catch (NamingException e) {
	    throw new EJBException("cant find datasource");
	}
    }

    public Integer ejbCreate() throws CreateException {
	return new Integer(1);
    }

    /* Get 10 connections in a bunch and then close them*/
    public boolean test1( int numRuns ) {
	boolean passed = true;
	Connection[] conns = new Connection[10];
        for( int i = 0; i < numRuns; i++ ) {
	    try {
	        conns[i] = ds.getConnection();
	    } catch (Exception e) {
	        passed = false;
	    } 
        }
        
	for (int i = 0 ; i < numRuns;i++ ) {
	    try {
	        conns[i].close();
	    } catch( Exception e) {
	        passed = false;
	    }
	}
	return passed;
    }

    /* Get a single connection and close it */
    public boolean test2() {
        Connection conn = null;
	Statement stmt = null;
	ResultSet rs = null;
        boolean passed = true;
	try {
	    conn = ds.getConnection();
	    stmt = conn.createStatement();
	    rs = stmt.executeQuery( "SELECT * FROM O_CUSTOMER");
	    while( rs.next() ) {
	        System.out.println(rs.getString(1));
	    }
	    stmt.executeUpdate("INSERT INTO O_CUSTOMER values (100, 'new phone')");
            rs = stmt.executeQuery( "SELECT * FROM O_CUSTOMER");
	    while( rs.next() ) {
	        System.out.println(rs.getString(1));
	    }
            stmt.executeUpdate("DELETE FROM O_CUSTOMER WHERE c_id=100");
	} catch (Exception e) {
	    passed = false;
	    e.printStackTrace();
	} finally {
	    if (rs != null ) { 
	        try { rs.close(); } catch( Exception e1 ) {}
	    }	
	    if (stmt != null ) { 
	        try { stmt.close(); } catch( Exception e1 ) {}
	    }	
	    if ( conn != null ) {
	        try {
	            conn.close();
	        } catch( Exception e1) {}    
	    }
	    
        } 
        
	return passed;
    }

    /** Application auth */
    public boolean test3() {
        Connection conn = null;
	Statement stmt = null;
	ResultSet rs = null;

        boolean passed = true;
	try {
	    conn = ds1.getConnection("scott", "tiger");
	    stmt = conn.createStatement();
	    stmt.executeUpdate("INSERT INTO O_CUSTOMER values (100, 'new phone')");
            rs = stmt.executeQuery( "SELECT * FROM O_CUSTOMER");
	    while( rs.next() ) {
	        System.out.println(rs.getString(1));
	    }
            stmt.executeUpdate("DELETE FROM O_CUSTOMER WHERE c_id=100");
            rs = stmt.executeQuery( "SELECT * FROM O_CUSTOMER");
	    System.out.println( rs );
	} catch (Exception e) {
	    passed = false;
	    e.printStackTrace();
	} finally {
	    if (rs != null ) { 
	        try { rs.close(); } catch( Exception e1 ) {}
	    }	
	    if (stmt != null ) { 
	        try { stmt.close(); } catch( Exception e1 ) {}
	    }	
	    if ( conn != null ) {
	        try {
	            conn.close();
	        } catch( Exception e1) {}    
	    }
        } 
        
	return passed;
    }
  

    public void ejbLoad() {}
    public void ejbStore() {}
    public void ejbRemove() {}
    public void ejbActivate() {}
    public void ejbPassivate() {}
    public void unsetEntityContext() {}
    public void ejbPostCreate() {}
}
