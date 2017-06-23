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

package com.sun.s1asdev.jdbc41.ejb;

import javax.ejb.*;
import javax.naming.*;
import javax.sql.*;
import java.rmi.*;
import java.util.*;
import java.sql.*;
import javax.transaction.UserTransaction;
import java.util.concurrent.*;
import java.lang.reflect.Method;

public class SimpleSessionBean implements SessionBean
{

    private SessionContext ctxt_;
    private InitialContext ic_; 
    private DataSource ds;
    PrintTask task1 = new PrintTask("thread1");
    PrintTask task2 = new PrintTask("thread2");
    ExecutorService threadExecutor = Executors.newCachedThreadPool(new ThreadFactory() {
	public Thread newThread(Runnable r) {
	    Thread th = new Thread(r);
	    th.setDaemon(true);
	    return th;
	}
    });

    class PrintTask implements Runnable {
        private String threadName;

        public PrintTask(String name) {
            threadName = name;
        }

        public void run() {
            System.out.println("Thread : " + threadName + " executed");
        }
    }

    public void setSessionContext(SessionContext context) {
        ctxt_ = context;
	try {
	    ic_ = new InitialContext();
	    ds = (DataSource) ic_.lookup("java:comp/env/DataSource");
	} catch( Exception ne ) {
	    ne.printStackTrace();
	}
    }

    public void ejbCreate() throws CreateException {
    }

    /**
     * Testing connection object 
     */
    public boolean test1() throws Exception {
	Connection con = null;
	Connection[] conns = new Connection[32];
	Statement stmt = null;
	boolean passed = true;
	try {
	    con = ds.getConnection("scott", "tiger");
	    stmt = con.createStatement();

	    //Try getting schema from con object
	    System.out.println(">>> con.getSchema()=" + con.getSchema());

	    //try getting network timeout from con object
	    try {
	        System.out.println(">>> con.getNetworkTimeout=" + con.getNetworkTimeout());
	    } catch(Exception ex) {
		passed = true;
		//Expected as this is not implemented in derby 10.8 jdbc41 driver
	    }

	    //Invoke abort on con object
	    threadExecutor.submit(task1);
	    threadExecutor.submit(task2);
	    con.abort(threadExecutor);
	    System.out.println("Marked connection for aborting...");
	    
	    //Try to use the aborted connection to create a statement. should fail
	    try {
	        con.createStatement();
	    } catch(SQLException ex) {
		passed = true;
		System.out.println("Creating statement from con after tx abort : Failed : Expected");
	    }
	} catch(Exception ex) {
	    passed = false;
	} finally {
	    if ( stmt != null ) {
	        try { stmt.close(); } catch( Exception e1) {}    
	    }
	    if ( con != null ) {
	        try { con.close(); } catch( Exception e1) {}    
	    }
	    for(int i=0; i<32; i++) {
		if(conns[i] != null) {
		    try { conns[i].close(); } catch(Exception ex) {}
		}
	    }
	}
	return passed;
    }

    /**
     * Testing connection object in a User transaction 
     */
    public boolean test2() throws Exception {
	Connection con = null;
	Connection[] conns = new Connection[32];
	Connection[] conns1 = new Connection[32];
	Statement[] stmts1 = new Statement[32];
	Statement stmt = null;
	boolean passed = true;
	try {
	    UserTransaction usertx = (UserTransaction) ctxt_.getUserTransaction();
            usertx.begin();
	    con = ds.getConnection("scott", "tiger");
	    //System.out.println("!!!! TEST2 ConHashcode=" + (((com.sun.appserv.jdbc.DataSource)ds).getConnection(con)).hashCode());
	    stmt = con.createStatement();

	    //Invoke abort on con object
	    threadExecutor.submit(task1);
	    threadExecutor.submit(task2);
	    con.abort(threadExecutor);
	    System.out.println("Marked connection for aborting...");
	    
	    //Operations on con object after marking for abort in a tx
	    //before tx commit, all operations on con pass
            //Try getting schema from con object
	    System.out.println(">>> con.getSchema()=" + con.getSchema());

	    //try getting network timeout from con object
	    try {
	        System.out.println(">>> con.getNetworkTimeout=" + con.getNetworkTimeout());
	    } catch(Exception ex) {
		passed = true;
		//Expected as this is not implemented in derby 10.8 jdbc41 driver
	    }

	    usertx.commit();

	    //Try to use the aborted connection to create a statement. should fail
	    try {
	        con.createStatement();
	    } catch(SQLException ex) {
		passed = true;
		System.out.println("Creating statement from con after tx abort : Failed : Expected");
	    }
	} catch(Exception ex) {
	    passed = false;
	} finally {
	    if ( stmt != null ) {
	        try { stmt.close(); } catch( Exception e1) {}    
	    }
	    if ( con != null ) {
	        try { con.close(); } catch( Exception e1) {}    
	    }
	    //Test to get (max pool size-1) no. of connections after a previous abort.
	    //should pass as the aborted connection is destroyed
	    for(int i=0; i<32; i++) {
		if(i == 31) {
		    //Last connection should not be got
		    try {
			conns1[i] = ds.getConnection("scott", "tiger");
		    } catch(Exception ex) {
			passed = true;
		    }
		} else {
                    conns1[i] = ds.getConnection("scott", "tiger");
		    stmts1[i] = conns1[i].createStatement();
		}
	    }
	    for(int i=0; i<32; i++) {
		if(stmts1[i] != null) {
		    try { stmts1[i].close(); } catch(Exception ex) {}
		}
	    }
	    for(int i=0; i<32; i++) {
		if(conns1[i] != null) {
		    try { conns1[i].close(); } catch(Exception ex) {}
		}
	    }
	}
	return passed;
    }

    //Tests DatabaseMetaData
    public boolean test3() throws Exception {
	Connection con = null;
	DatabaseMetaData dmd = null;
	boolean passed = true;
	try {
	    con = ds.getConnection("scott", "tiger");
	    dmd = con.getMetaData();
	    //Testing DatabaseMetaData
	    System.out.println(">>> dmd.generatedKeyAlwaysReturned()=" + dmd.generatedKeyAlwaysReturned());
	} catch(Exception ex) {
	    passed = false;
	} finally {
	    if(con!= null) {
		try { con.close(); } catch(Exception ex) {}
	    }
	}
	return passed;
    }

    /**
     * Testing Statement object within a transaction
     */
    public boolean test4() throws Exception {
	Connection conn1 = null;
	Statement stmt1 = null;
	ResultSet rs1 = null;
	boolean passed = true;

	//Now try getting a connection again, but within a transaction
	int size = 0;
	try {
	    createTable();
	    UserTransaction tx = (UserTransaction) ctxt_.getUserTransaction();
	    tx.begin();
	    conn1 = ds.getConnection("scott", "tiger");
	    System.out.println("!!!! TEST4 ConHashcode=" + (((com.sun.appserv.jdbc.DataSource)ds).getConnection(conn1)).hashCode());
	    stmt1 = conn1.createStatement();
	    insertRow(stmt1);
            
	    threadExecutor.submit(task1);
            threadExecutor.submit(task2);

	    //Testing Statement object
	    stmt1.closeOnCompletion();
	    System.out.println(">>> stmt1.isCloseOnCompletion()=" + stmt1.isCloseOnCompletion());
	    
            conn1.abort(threadExecutor);

	    System.out.println("Marked connection for aborting...");
	    insertRow(stmt1);

	    //After con abort (within the tx) 
	    size = getCount(stmt1);

	    if(size != 2) {
		passed = false;
		System.out.println("FAILED as size!= 2");
	    }

	    tx.commit();

	    try {
	        //After tx commit, insert data. Should fail because of no current connection
		insertRow(stmt1);
	    } catch(SQLException ex) {
		passed = true;
		System.out.println("Trying to insert data after a tx commit (prev. conn abort): Failed with : " + ex + " Expected.");
	    }
	} catch (Exception e) {
	   passed = false;
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
	return passed;
    }

    private void createTable() throws SQLException {
        String tableName = "JDBC41";
        
        Connection con = null;
        Statement stmt = null;
        con = ds.getConnection("scott", "tiger");
        stmt = con.createStatement();
        
        try{
            stmt.executeUpdate("drop table " + tableName);
        }catch(Exception ex){
        }
        stmt.executeUpdate("create table " + tableName + " (qty integer, name varchar(32))");
        stmt.close();
        con.close();
    }
    
    private void insertRow(Statement stmt) throws SQLException {
        String tableName = "JDBC41";
        stmt.executeUpdate("INSERT INTO " + tableName + " values (100, 'COFFEE')");
    }
    
    private int getCount(Statement stmt) throws SQLException {
        String tableName = "JDBC41";
        ResultSet rs = null;
        rs = stmt.executeQuery( "SELECT count(*) FROM " + tableName);
        rs.next();
        int count = rs.getInt(1);
        rs.close();
        return count;
    }
    
    public void ejbLoad() {}
    public void ejbStore() {}
    public void ejbRemove() {}
    public void ejbActivate() {}
    public void ejbPassivate() {}
    public void unsetEntityContext() {}
    public void ejbPostCreate() {}
}
