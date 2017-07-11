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

/*
 * UnpooledTest.java
 *
 * Created on October 31, 2006, 11:44 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.sun.s1asdev.jdbc.switchoffACCConnPooling.client;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;
import java.util.HashSet;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

/**
 *
 * @author kshitiz
 */
public class Client {
    
    private static int count = 10;
    private static boolean rollback;
    private static String tableName = "COFFEE";
    
    //static SimpleReporterAdapter stat = new SimpleReporterAdapter("appserv-tests");
    private static SimpleReporterAdapter stat = new SimpleReporterAdapter();
    private static final String testSuite = "switchoffACCconnpooling";
    private static int testCount = 0;
    
    private static boolean isXA=false;

    /** Creates a new instance of UnpooledTest */
    public Client() {
    }
    
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        stat.addDescription("Switch-Off connection pooling in ACC");
        System.out.println("verifying uniqueness of all connection");
        openAndCloseConnection("jdbc/nonxaresource", 40);
        System.out.println("creating connection upto max-pool-size of 32");
        openMaxConnections("jdbc/nonxaresource", 32);
        rollback = false;
        System.out.println("rollback set to " + rollback);
        runTest();
        rollback = true;
        System.out.println("rollback set to " + rollback);
        runTest();
        stat.printSummary();
    }
    
    private static void runTest() {
        
        //Connection opened and closed within transaction
        //non-xa resource
        isXA = false;
        test1("jdbc/nonxaresource");
        //xa resource
        isXA = true;
        test1("jdbc/xaresource");
        
        //Connection opened within transaction 
        //but closed after transaction
        isXA = false;
        //non-xa resource
        test2("jdbc/nonxaresource");
        //xa resource
        isXA = true;
        test2("jdbc/xaresource");
                
        //XA and Non-XA resource within same transaction
        //non-xa resource and xa  resource together
        test3("jdbc/nonxaresource", "jdbc/xaresource");
       
        test4("jdbc/nonxaresource", "jdbc/xaresource");	
        //openAndCloseConnection("jdbc/oraclexa", 40);
    }
    
    public static void test1(String dsName) {
        UserTransaction ut = null;
        try {
            InitialContext ic = new InitialContext();
            DataSource ds = (DataSource) ic.lookup(dsName);
            printConnection(ds);
            
            createTable(ds);
            
            int count1 = getCount(ds);
            System.out.println("count1 : " + count1);
            
            ut = (UserTransaction) ic.lookup("java:comp/UserTransaction");
            ut.begin();
            
            for(int i=0; i< count; i++)
                insertRow(ds);
            if(rollback)
                ut.rollback();
            else
                ut.commit();
            
            int count2=getCount(ds);
            
            System.out.println("count2 : " + count2);
            
            int diff = count2 - count1;
            if(( diff == count && !rollback) || (diff == 0 && rollback))
                printStatus(true);
            else
                printStatus(false);
            
        } catch(Exception e){
            printStatus(false);
            e.printStackTrace();
            if(ut != null){
                try {
                    ut.rollback();
                } catch (IllegalStateException ex) {
                    ex.printStackTrace();
                } catch (SecurityException ex) {
                    ex.printStackTrace();
                } catch (SystemException ex) {
                    ex.printStackTrace();
                }
            }
        };
        
    }
    
    public static void test2(String dsName) {
        UserTransaction ut = null;
        try {
            InitialContext ic = new InitialContext();
            DataSource ds = (DataSource) ic.lookup(dsName);
            printConnection(ds);
            Connection[] con;

            createTable(ds);
            
            int count1 = getCount(ds);
            System.out.println("count1 : " + count1);
            
            ut = (UserTransaction) ic.lookup("java:comp/UserTransaction");
            ut.begin();
            
            con = openConnections(dsName, count);
            insertRow(con);
            closeConnections(con, count);
            
            if(rollback)
                ut.rollback();
            else
                ut.commit();
            
            int count2=getCount(ds);
            
            System.out.println("count2 : " + count2);
            
            int diff = count2 - count1;
            if(( diff == count && !rollback) || (diff == 0 && rollback))
                printStatus(true);
            else
                printStatus(false);
            
        } catch(Exception e){
            printStatus(false);
            e.printStackTrace();
            if(ut != null){
                try {
                    ut.rollback();
                } catch (IllegalStateException ex) {
                    ex.printStackTrace();
                } catch (SecurityException ex) {
                    ex.printStackTrace();
                } catch (SystemException ex) {
                    ex.printStackTrace();
                }
            }
        };
        
    }
    
    
    public static void test4(String dsName, String xaDsName) {
        UserTransaction ut = null;
        try {
            InitialContext ic = new InitialContext();
            DataSource ds = (DataSource) ic.lookup(dsName);
	    DataSource xads = (DataSource) ic.lookup(xaDsName);
            printConnection(ds);
            Connection[] con;
	    Connection[] xaCon;

	    isXA = false;
            createTable(ds);
            isXA = true;
	    createTable(xads);

	    isXA = false;
            int count1 = getCount(ds);
	    isXA = true;
	    int xacount1 = getCount(xads);

            System.out.println("count1 : " + count1 + " xacount1 : " + xacount1);
            
            ut = (UserTransaction) ic.lookup("java:comp/UserTransaction");
            ut.begin();
            
            con = openConnections(dsName, count);
	    xaCon = openConnections(xaDsName, 1);
	    isXA = false;
            insertRow(con);
	    isXA = true;
	    insertRow(xaCon);

	    isXA = false;
            closeConnections(con, count);

	    isXA = true;
	    closeConnections(xaCon, 1);
            
            if(rollback)
                ut.rollback();
            else
                ut.commit();
            
	    isXA = false;
            int count2=getCount(ds);
	    isXA = true;
	    int xacount2 = getCount(xads);
            
            System.out.println("count2 : " + count2 + " xacount2 : " + xacount2);
            
            int diff = count2 - count1;
	    int xadiff = xacount2 - xacount1;

            if((( diff == count && !rollback) || (diff == 0 && rollback)) && 
			((xadiff == 1 && !rollback) || (xadiff == 0 && rollback)))
                printStatus(true);
            else
                printStatus(false);
            
        } catch(Exception e){
            printStatus(false);
            e.printStackTrace();
            if(ut != null){
                try {
                    ut.rollback();
                } catch (IllegalStateException ex) {
                    ex.printStackTrace();
                } catch (SecurityException ex) {
                    ex.printStackTrace();
                } catch (SystemException ex) {
                    ex.printStackTrace();
                }
            }
        };
        
    }
    
    
    public static void test3(String ds1Name, String ds2Name) {
        UserTransaction ut = null;
        try {
            InitialContext ic = new InitialContext();
            DataSource ds1 = (DataSource) ic.lookup(ds1Name);
            printConnection(ds1);
            DataSource ds2 = (DataSource) ic.lookup(ds2Name);
            printConnection(ds2);
            
            isXA = false;
            createTable(ds1);
            isXA = true;
            createTable(ds2);
            
            isXA = false;
            int count1 = getCount(ds1);
            isXA = true;
            int count3 = getCount(ds2);
            System.out.println("count1 : " + count1);
            System.out.println("count3 : " + count3);
            
            ut = (UserTransaction) ic.lookup("java:comp/UserTransaction");
            ut.begin();
            
            isXA = false;
            for(int i=0; i< count; i++)
                insertRow(ds1);
            isXA = true;
            for(int i=0; i< count; i++)
                insertRow(ds2);
            
            if(rollback)
                ut.rollback();
            else
                ut.commit();
            
            isXA = false;
            int count2=getCount(ds1);
            isXA = true;
            int count4=getCount(ds2);
            
            System.out.println("count2 : " + count2);
            System.out.println("count4 : " + count4);
            
            int diff1 = count2 - count1;
            int diff2 = count4 - count3;
            if((( diff1 == count && !rollback) || (diff1 == 0 && rollback))
            && (( diff2 == count && !rollback) || (diff2 == 0 && rollback)))
                printStatus(true);
            else
                printStatus(false);
            
        } catch(Exception e){
            printStatus(false);
            e.printStackTrace();
            if(ut != null){
                try {
                    ut.rollback();
                } catch (IllegalStateException ex) {
                    ex.printStackTrace();
                } catch (SecurityException ex) {
                    ex.printStackTrace();
                } catch (SystemException ex) {
                    ex.printStackTrace();
                }
            }
        };
        
    }
    
    private static void createTable(final DataSource ds) throws SQLException {
        String tableName;
        if(isXA)
            tableName = "COFFEE_XA";
        else
            tableName = "COFFEE";
        
        Connection con;
        Statement stmt;
        con = ds.getConnection();
        stmt = con.createStatement();
        
        try{
            stmt.executeUpdate("drop table " + tableName);
        }catch(Exception ex){
            ex.printStackTrace();
        }
        stmt.executeUpdate("create table " + tableName + " (name varchar(32), qty integer)");
        stmt.close();
        con.close();
    }
    
    private static int getCount(final DataSource ds) throws SQLException {
        String tableName;
        if(isXA)
            tableName = "COFFEE_XA";
        else
            tableName = "COFFEE";
        Statement stmt;
        Connection con;
        ResultSet rs;
        con = ds.getConnection();
        stmt = con.createStatement();
        rs = stmt.executeQuery( "SELECT count(*) FROM " + tableName);
        rs.next();
        int count = rs.getInt(1);
        rs.close();
        stmt.close();
        con.close();
        return count;
    }
    
    private static void insertRow(final DataSource ds) throws SQLException {
        String tableName;
        if(isXA)
            tableName = "COFFEE_XA";
        else
            tableName = "COFFEE";
        Statement stmt;
        Connection con;
        con = ds.getConnection();
        stmt = con.createStatement();
        stmt.executeUpdate("INSERT INTO " + tableName + " values ('COFFEE', 100)");
        stmt.close();
        con.close();
    }
    
    private static void insertRow(final Connection[] con) throws SQLException {
        String tableName;
        if(isXA)
            tableName = "COFFEE_XA";
        else
            tableName = "COFFEE";
        Statement stmt;
        for(int i=0; i < con.length; i++){
            stmt = con[i].createStatement();
            stmt.executeUpdate("INSERT INTO " + tableName + " values ('COFFEE', 100)");
	    stmt.close();
        }
    }
    
   
    private static void printConnection(final DataSource ds) throws SQLException{
        com.sun.appserv.jdbc.DataSource dsTyped = (com.sun.appserv.jdbc.DataSource) ds;
        Connection wrapper = dsTyped.getConnection();
        System.out.println("Connection type : " + dsTyped.getConnection(wrapper));
        wrapper.close();
    }
    
    private static void openAndCloseConnection(String dsName, int count) {
        try {
            InitialContext ic = new InitialContext();
            DataSource ds = (DataSource) ic.lookup(dsName);
            com.sun.appserv.jdbc.DataSource dsTyped = (com.sun.appserv.jdbc.DataSource) ds;
	    HashSet<String> connections = new HashSet<String>();
            Connection con;
	    String conType;
	    boolean status = true;
	    int i = 0;
            for(; i < count; i++){
                con = ds.getConnection();
		conType = dsTyped.getConnection(con).toString();
        	System.out.println("Connection type : " + conType);
                con.close();
		if(!connections.add(conType)){
			status = false;
			break;
		}
            }
       	    System.out.println("Total connection requested :  " + count);
       	    System.out.println("Total connection created :  " + i);
       	    System.out.println("Total number of unique connection :  " + connections.size());
	    printStatus(status);
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }
    
    private static void openMaxConnections(String dsName, int count) {
        DataSource ds = null;
	try{
        	InitialContext ic = new InitialContext();
		ds = (DataSource) ic.lookup(dsName);
	}catch(NamingException ex){
		System.out.println("Unable to lookup datasource");
		ex.printStackTrace();
		return;
	}
        
        Connection[] con = new Connection[count];
	int i = 0;
        for(; i < count; i++){
	   try{
           	con[i] = ds.getConnection();
	   }catch(SQLException ex){
		System.out.println("Unable to create max connections");
		printStatus(false);	
	   }
	}

	if(i ==  count){
		System.out.println("Able to create max connections");
		printStatus(true);	
		try{
			ds.getConnection();
                	System.out.println("Able to create beyond max connections");
                	printStatus(false);
		}catch(SQLException ex){
                	System.out.println("Unable to create beyond max connections");
                	printStatus(true);
           	}
	}
        
        for(; i > 0; i--){
	   try{
            	con[i - 1].close();
	   }catch(SQLException ex){
		System.out.println("Unable to close connection");
	   }
	}
    }

    private static Connection[] openConnections(String dsName, int count) throws NamingException, SQLException {
        InitialContext ic = new InitialContext();
        DataSource ds = (DataSource) ic.lookup(dsName);
        
        Connection[] con = new Connection[count];
        for(int i=0; i < count; i++) {
            con[i] = ds.getConnection();
	    System.out.println("con[" + i+ "]=" + con[i]);
	}
        return con;
    }
    
    private static void closeConnections(Connection[] con, int count) throws SQLException {
        for(int i=0; i < count; i++)
            con[i].close();
    }
    
    private static void printStatus(boolean status){
        String testcaseID = testSuite + "-test" + (++testCount) ;
        if(status){
            stat.addStatus(testcaseID, stat.PASS);
            System.out.println(testcaseID + ": passed");
        }
        else{
            stat.addStatus(testcaseID, stat.FAIL);
            System.out.println(testcaseID + ": failed");
        }
    }
}
