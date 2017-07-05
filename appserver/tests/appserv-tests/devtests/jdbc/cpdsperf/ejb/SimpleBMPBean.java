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

package com.sun.s1asdev.jdbc.cpdsperf.ejb;

import javax.ejb.*;
import javax.naming.*;
import javax.sql.*;
import java.rmi.*;
import java.util.*;
import java.sql.*;

public class SimpleBMPBean implements EntityBean {

    //protected ConnectionPoolDataSource ds;
    protected DataSource dsCP;
    protected DataSource dsNormal;
    int id;
    protected int numRuns;

    public void setEntityContext(EntityContext entityContext) {
	Context context = null;
	try {
	    context    = new InitialContext();
	    dsCP = (DataSource) context.lookup("java:comp/env/DataSource-cp");
	    dsNormal = (DataSource) context.lookup("java:comp/env/DataSource-normal");
	} catch (NamingException e) {
	    throw new EJBException("cant find datasource");
	}
    System.out.println("[**SimpleBMPBean**] Done with setEntityContext....");
    }

    public Integer ejbCreate(int _numRuns) throws CreateException {
        numRuns = _numRuns;
	return new Integer(1);
    }
    
    /**
     * Do an SQL insert into the database and time this
     * @return time taken. -1 If test fails
     */
    public long test1() {
        //ConnectionPoolDataSource
	System.out.println("-----------------Start test1--------------");
	Connection conn = null;
	boolean passed = true;
	long startTime = 0 ;
	long endTime = 0;
	try {
	    startTime = System.currentTimeMillis();
	    for ( int i = 0; i < numRuns; i++ ) {
	        conn = dsCP.getConnection("system", "manager");
		insertEntry( i, "1234567890", conn);
		if (i / 10 == 0 ) {
		    queryTable( conn );
		}
		conn.close();
	    }    
	    endTime = System.currentTimeMillis(); 
	} catch (Exception e) {
	    e.printStackTrace();
	    passed = false;
	} finally {
	    if ( conn != null ) {
	        try { conn.close(); } catch( Exception e1) {}    
	    }
	}
	System.out.println("-----------------End test1--------------");
	
	try { 
	    emptyTable( conn );
	} catch (Exception e) {
	    e.printStackTrace();
	} finally {
	    if (conn != null) {
	        try { conn.close(); } catch( Exception e1 ) {}
	    }
	}
        if (passed) {
	    return (endTime - startTime)/1000;
	}
	return -1;
    }
    
    public long test2() {
        //Normal DataSource
	System.out.println("-----------------Start test2--------------");
	Connection conn = null;
	boolean passed = true;
	long startTime = 0 ;
	long endTime = 0;
	try {
	    startTime = System.currentTimeMillis();
	    for ( int i = 0; i < numRuns; i++ ) {
	        conn = dsNormal.getConnection("system", "manager");
		insertEntry( i, "1234567890", conn);
		if (i / 10 == 0 ) {
		    queryTable( conn );
		}
		conn.close();
	    }    
	    endTime = System.currentTimeMillis(); 
	} catch (Exception e) {
	    e.printStackTrace();
	    passed = false;
	} finally {
	    if ( conn != null ) {
	        try {
	            conn.close();
		} catch( Exception e1) {}    
	    }
	}
	System.out.println("-----------------End test2--------------");
	
	try {
	    conn = dsNormal.getConnection("system","manager");
	    emptyTable(conn);
	    conn.close();
	} catch( Exception e) {
	    e.printStackTrace();
	} finally {
	    if (conn != null) {
	        try { conn.close(); } catch( Exception e1) {}
	    }
	}
        if (passed) {
	    return (endTime - startTime)/1000;
	}
	return -1;
    }

    private void insertEntry( int id, String phone, Connection con )
        throws SQLException {
	
        PreparedStatement stmt = con.prepareStatement(
	    "insert into O_Customer values (?, ?)" );

	stmt.setInt(1, id);
	stmt.setString(2, phone);

	stmt.executeUpdate();
	stmt.close();
        /*	
        PreparedStatement stmt = con.prepareStatement(
	    "select * from O_Customer" );
	stmt.executeUpdate();    
	stmt.close();
	*/
    }

    private void emptyTable( Connection con ) {
        try {
            Statement stmt = con.createStatement();
            
	    stmt.execute("delete * from O_Customer");
	    stmt.close();
        } catch( Exception e) {
	}
	    
    }

    private void queryTable( Connection con ) {
        try {
	    Statement stmt = con.createStatement();
	    ResultSet rs = stmt.executeQuery("select * from O_Customer");
	    while( rs.next() ) ;
	    rs.close();
	} catch( Exception e) {
	}
    }

    public void ejbLoad() {}
    public void ejbStore() {}
    public void ejbRemove() {}
    public void ejbActivate() {}
    public void ejbPassivate() {}
    public void unsetEntityContext() {}
    public void ejbPostCreate( int numTimes ) {}
}
