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

package com.sun.s1asdev.jdbc.dmmcfnotxconn.ejb;

import java.util.*;
import java.io.*;
import java.rmi.*;
import javax.ejb.*;
import javax.transaction.UserTransaction;
import javax.naming.*;
import javax.sql.*;
import java.sql.*;

public class SimpleSessionBean implements SessionBean {

    private transient SessionContext ctxt_  = null;
    private InitialContext ic_;
    transient com.sun.appserv.jdbc.DataSource ds;

    public void setSessionContext(SessionContext context) {
        ctxt_ = context;
        try {
            ic_ = new InitialContext();
        } catch (NamingException ne) {
            ne.printStackTrace();
        }
    }

    public void ejbCreate() throws CreateException {
    }

    public boolean test1() throws Exception {
        Connection conn = null;
        Connection noTxConn = null;
        Statement stmt = null;
        Statement stmt2 = null;
        ResultSet rs = null;
        ResultSet rs2 = null;

        try {

            ds = (com.sun.appserv.jdbc.DataSource) ic_.lookup("java:comp/env/DataSource");

            UserTransaction tx =(UserTransaction)ctxt_.getUserTransaction();
	      tx.begin();
            conn = ds.getConnection();
	      noTxConn = ds.getNonTxConnection();
            stmt = conn.createStatement();
	      stmt.executeUpdate("INSERT INTO NOTXCONNTABLE VALUES('method1',3)");
            String query1 = "SELECT * FROM NOTXCONNTABLE";
            rs = stmt.executeQuery(query1);
	      stmt2 = noTxConn.createStatement();
            rs2 = stmt2.executeQuery("SELECT * FROM NOTXCONNTABLE");
	   
            if ( rs2.next() ) {
	        return false;
	      }
            
	      tx.commit();
	      return true;
	    
        } catch (Exception e) {
	    System.out.println("Caught Exception---");
	    e.printStackTrace();
	    return false;
        } finally {
            if (rs != null ) {
                try { rs.close(); } catch( Exception e1) {}
            }
            if (stmt != null ) {
                try {stmt.close(); } catch( Exception e1) {}
            }
            if (conn != null ) {
                try {conn.close();} catch( Exception e1) {}
            }
            	
            if (rs2 != null ) {
                try {rs2.close();} catch( Exception e1 ) {}
            }
            if (stmt2 != null ) {
                try {stmt2.close(); } catch( Exception e1) {}
            }
            		
            if (noTxConn != null ) {
                try { noTxConn.close(); }catch( Exception e1) {}
            }
	}

        
    }

    public boolean test2() throws Exception {
	Connection conn = null;
	Connection noTxConn = null;

        try {

            ds = (com.sun.appserv.jdbc.DataSource)ic_.lookup("java:comp/env/DataSource");

            UserTransaction tx =(UserTransaction)ctxt_.getUserTransaction();
	      tx.begin();
            System.out.println("Getting TRANSACTIONAL connection");
            conn = ds.getConnection();
            System.out.println("Autocommit => " + conn.getAutoCommit());

	      if (conn.getAutoCommit() == true ) {
	          return false;
	      }

	      conn.close();
	      for (int i = 0; i < 20; i++ ) {
		    System.out.println("Getting NonTx connection");
	          noTxConn = ((com.sun.appserv.jdbc.DataSource)ds).getNonTxConnection();
  		    System.out.println("Autocommit => " + noTxConn.getAutoCommit());
		    if (noTxConn.getAutoCommit() == false ) {
	              return false;	
		    }
		    noTxConn.close();
	      }
            System.out.println("Getting TRANSACTIONAL connection");
            conn = ds.getConnection();
            System.out.println("Autocommit => " + conn.getAutoCommit());
	      if (conn.getAutoCommit() == true ) {
	          return false;
	      }
	      conn.close();
	      tx.commit();

	      return true;
	    
        } catch (Exception e) {
	    System.out.println("Caught Exception---");
	    e.printStackTrace();
	    return false;
        } finally {
	    try {
		if (noTxConn != null ) {
		    noTxConn.close();
		}
	    } catch( Exception e1 ) {}
	}


    }

    public void ejbLoad() {
    }

    public void ejbStore() {
    }

    public void ejbRemove() {
    }

    public void ejbActivate() {
    }

    public void ejbPassivate() {
    }

    public void unsetEntityContext() {
    }

    public void ejbPostCreate() {
    }
}
