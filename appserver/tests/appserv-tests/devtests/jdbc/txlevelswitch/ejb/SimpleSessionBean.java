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

package com.sun.s1asdev.jdbc.txlevelswitch.ejb;

import javax.ejb.*;
import javax.naming.*;
import javax.sql.*;
import java.rmi.*;
import java.util.*;
import java.sql.*;

public class SimpleSessionBean implements SessionBean
{

    private SessionContext ctxt_;
    private InitialContext ic_; 
    public void setSessionContext(SessionContext context) {
        ctxt_ = context;
	try {
	    ic_ = new InitialContext();
	} catch( NamingException ne ) {
	    ne.printStackTrace();
	}
    }

    public void ejbCreate() throws CreateException {
    }

    /**
     * Get connection and do some database updates. Then throw
     * an exception before tx commits and ensure that the updates
     * did not happen
     */
    public boolean test1() throws Exception {
	DataSource ds = (DataSource)ic_.lookup("java:comp/env/DataSource");
	Connection conn1 = null;
	Statement stmt1 = null;
	ResultSet rs1 = null;
	int keyToCompare = 10;
	boolean passed = false;
	//clean the database
	try {
	    conn1 = ds.getConnection();
	    stmt1 = conn1.createStatement();
	    stmt1.executeUpdate( "DELETE FROM TXLEVELSWITCH");
	} catch( SQLException e) {
	    e.printStackTrace();
	    return false;
	} finally {
	    if (stmt1 != null) { 
	        try { stmt1.close(); } catch( Exception e1 ) {}
	    }
	    if (conn1 != null) { 
	        try { conn1.close(); } catch( Exception e1 ) {}
	    }
	}
	
	try {
	    //Call a method with tx=RequiresNew and have it throw
	    //an exception
	    updateDb( "TXLEVELSWITCH", keyToCompare );
	} catch( Exception e ) {
	    if (!( e instanceof EJBException )) {
	        e.printStackTrace();
		return false;
	    } else {
	        System.out.println("Caught expected EJBException");
	    }
	}
	try {
	    //since our updateDb threw an exception, we should not
	    //find the inserted value here
	    conn1 = ds.getConnection();
	    stmt1 = conn1.createStatement();
	    rs1 = stmt1.executeQuery("SELECT * FROM TXLEVELSWITCH"); 
	    while( rs1.next() ) {
	        int key = rs1.getInt("c_id");
		if ( key == 10 ) {
		    System.out.println("update happened successfully. It should have failed");
		    return false;
		}
	    }
	} catch (Exception e) {
	   e.printStackTrace(); 
	   return false;
	} finally {
	    if (rs1 != null ) {
	        try { rs1.close(); } catch( Exception e1 ) {}
	    }
	    if ( stmt1 != null ) {
	        try { stmt1.close(); } catch( Exception e1) {}    
	    }
	    if ( conn1 != null ) {
	        try { conn1.close(); } catch( Exception e1) {}    
	    }
	}

	return true;
    }
    
    private void updateDb( String table, int keyToInsert ) throws Exception {
        DataSource ds = (DataSource)ic_.lookup("java:comp/env/DataSource");
	Connection conn1 = null;
	Statement stmt1 = null;
	ResultSet rs1 = null;
	boolean passed = true;
	try {
	    conn1 = ds.getConnection();
	    stmt1 = conn1.createStatement();
	    stmt1.executeUpdate("INSERT INTO " +table+" VALUES ("
	        + keyToInsert+", 'ABCD')");
            throw new Exception("Rollback the tx");
	} catch( SQLException sqle ) {
	    throw sqle;
	} catch (Exception e) {
	   throw new EJBException( e ); 
	} finally {
	    if (rs1 != null ) {
	        try { rs1.close(); } catch( Exception e1 ) {}
	    }
	    if ( stmt1 != null ) {
	        try { stmt1.close(); } catch( Exception e1) {}    
	    }
	    if ( conn1 != null ) {
	        try { conn1.close(); } catch( Exception e1) {}    
	    }
	}


    }
    /**
     * Get connection with two non-xa datasources.
     * Do some work using both. Should throw an
     * exception (that we catch ) since 2 non-xa
     * resources cannot be mixed. This test is run
     * after converting the 2 connection-pools to LocaTransaction
     * so by catching the exception we are asserting taht this
     * changeover is indeed successful
     */
    public boolean test2() throws Exception {
        System.out.println("************IN TEST 2*************");    
	InitialContext ic = new InitialContext();
	DataSource ds1 = (DataSource)ic.lookup("java:comp/env/DataSource1");
	DataSource ds2 = (DataSource)ic.lookup("java:comp/env/DataSource2");
	Connection conn1 = null;
	Connection conn2 = null;
	Statement stmt1 = null;
	Statement stmt2 = null;
	ResultSet rs1 = null;
	ResultSet rs2 = null;
	boolean passed = true;
	try {
            System.out.println("Before getConnection 1");	
	    conn1 = ds1.getConnection();
            System.out.println("After getConnection 1");	
            System.out.println("Before getConnection 2");	
	    conn2 = ds2.getConnection();
            System.out.println("After getConnection 2");	
	    
            System.out.println("Before createStatement 1");	
	    stmt1 = conn1.createStatement();
            System.out.println("After createStatement 1");	
            System.out.println("Before createStatement 2");	
	    stmt2 = conn2.createStatement();
            System.out.println("After createStatement 2");	
            
            System.out.println("executing statement 1"); 

	    try {
	        rs1 = stmt1.executeQuery("SELECT * FROM TXLEVELSWITCH");
            } catch( Exception e2 ) {
	        System.out.println("Exception for first query :" + e2.getMessage() );
	    } finally {
	        passed = false;
            }
           
            System.out.println("executing statement 2"); 
	    try {
	        rs2 = stmt2.executeQuery("SELECT * FROM TXLEVELSWITCH2");
	    } catch( Exception e2) {
	        System.out.println("Exception for second query :" + e2.getMessage() );
	    } finally {
	        passed = false;
	    } 

            System.out.println("finished executing statements"); 
	    passed = !(rs1.next() & rs2.next());
	} catch (Exception e) {
	    passed = true;
	    System.out.println("final exception : " + e.getMessage() );
	    throw new EJBException(e);
	} finally {
	    if (rs1 != null ) {
	        try { rs1.close(); } catch( Exception e1 ) {}
	    }

	    if (rs2 != null ) {
	        try { rs2.close(); } catch( Exception e1 ) {}
	    }

	    if ( stmt1 != null ) {
	        try { stmt1.close(); } catch( Exception e1) {}    
	    }
	    if ( stmt2 != null ) {
	        try { stmt2.close(); } catch( Exception e1) {}    
	    }
	    if ( conn1 != null ) {
	        try { conn1.close(); } catch( Exception e1) {}    
	    }
	    if ( conn2 != null ) {
	        try { conn2.close(); } catch( Exception e1) {}    
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
