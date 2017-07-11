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

package com.sun.s1asdev.jdbc.pooling.ejb;

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
    private static int count = 10;
    private static boolean rollback;
    private static String tableName = "COFFEE";
    private InitialContext ic;
    private DataSource ds;
    private DataSource xads; 
    private DataSource contds;
    private static boolean isXA=false;
    public void setSessionContext(SessionContext ctxt) {
        this.context = ctxt;
	try {
	    ic = new InitialContext();
	    ds = (DataSource)ic.lookup("java:comp/env/DataSource");
	    xads = (DataSource)ic.lookup("java:comp/env/XADataSource");
	    contds = (DataSource) ic.lookup("java:comp/env/ContainerDataSource");
	} catch( Exception ne ) {
	    ne.printStackTrace();
	}
    }

    public void ejbCreate() throws CreateException {
    }

    public boolean test1(boolean isXa, boolean rollback) {
	boolean passed = false;
	this.isXA = isXa;
	this.rollback = rollback;
	DataSource ds = null;
	if(isXA) {
	    ds = this.xads;
	} else {
	    ds = this.ds;
	}
        UserTransaction ut = null;
        try {
            
            createTable(ds);
            
            int count1 = getCount(ds);
            System.out.println("count1 : " + count1);
            
            ut = (UserTransaction) context.getUserTransaction();
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
                passed = true;
            else
                passed = false;
            
        } catch(Exception e){
            passed = false;
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
	return passed;
        
    }
    
    public boolean test2(boolean isXa, boolean rollback) {
	boolean passed = false;
	this.isXA = isXa;
	this.rollback = rollback;
	DataSource ds = null;
	if(isXA) {
	    ds = this.xads;
	} else {
	    ds = this.ds;
	}
        UserTransaction ut = null;
        try {
            Connection[] con;

            createTable(ds);
            
            int count1 = getCount(ds);
            System.out.println("count1 : " + count1);
            
            ut = (UserTransaction) context.getUserTransaction();
            ut.begin();
            
            con = openConnections(ds, count);
            insertRow(con);
            closeConnections(con);
            
            if(rollback)
                ut.rollback();
            else
                ut.commit();
            
            int count2=getCount(ds);
            
            System.out.println("count2 : " + count2);
            
            int diff = count2 - count1;
            if(( diff == count && !rollback) || (diff == 0 && rollback))
                passed = true;
            else
                passed = false;
            
        } catch(Exception e){
            passed = false;
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
        }
	return passed;
    }

    public boolean test3(boolean rollback) {
	boolean passed = false;
	this.rollback = rollback;
        UserTransaction ut = null;
	DataSource ds1 = this.ds;
	DataSource ds2 = this.xads;
        try {
            
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
            
            ut = (UserTransaction) context.getUserTransaction();
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
                passed=true;
            else
                passed=false;
            
        } catch(Exception e){
            passed=false;
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
       return passed; 
    }
    
    private void createTable(final DataSource ds) throws SQLException {
        String tableName;
        if(isXA)
            tableName = "COFFEE_XA";
        else
            tableName = "COFFEE";
        
        Connection con;
        Statement stmt;
        con = ds.getConnection("scott", "tiger");
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
    
    private int getCount(final DataSource ds) throws SQLException {
        String tableName;
        if(isXA)
            tableName = "COFFEE_XA";
        else
            tableName = "COFFEE";
        Statement stmt;
        Connection con;
        ResultSet rs;
        con = ds.getConnection("scott", "tiger");
        stmt = con.createStatement();
        rs = stmt.executeQuery( "SELECT count(*) FROM " + tableName);
        rs.next();
        int count = rs.getInt(1);
        rs.close();
        stmt.close();
        con.close();
        return count;
    }
    
    private void insertRow(final DataSource ds) throws SQLException {
        String tableName;
        if(isXA)
            tableName = "COFFEE_XA";
        else
            tableName = "COFFEE";
        Statement stmt;
        Connection con;
        con = ds.getConnection("scott", "tiger");
        stmt = con.createStatement();
        stmt.executeUpdate("INSERT INTO " + tableName + " values ('COFFEE', 100)");
        stmt.close();
        con.close();
    }
    
    private void insertRow(final Connection[] con) throws SQLException {
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
    
   
    private void printConnection(final DataSource ds) throws SQLException{
        com.sun.appserv.jdbc.DataSource dsTyped = (com.sun.appserv.jdbc.DataSource) ds;
        Connection wrapper = dsTyped.getConnection("scott", "tiger");
        System.out.println("Connection type : " + dsTyped.getConnection(wrapper));
        wrapper.close();
    }
    
    public boolean openAndCloseConnection(int count) {
	boolean status = true;
	DataSource ds = contds;

        try {
            com.sun.appserv.jdbc.DataSource dsTyped = (com.sun.appserv.jdbc.DataSource) ds;
	    HashSet<String> connections = new HashSet<String>();
            Connection con;
	    String conType;
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
        }catch(Exception ex){
            ex.printStackTrace();
        }
	return status;
    }
    
    public boolean openMaxConnections(int count) {
	boolean status = false;
	DataSource ds = contds;
        Connection[] con = new Connection[count];
	int i = 0;
        for(; i < count; i++){
	   try{
           	con[i] = ds.getConnection();
	   }catch(SQLException ex){
		System.out.println("Unable to create max connections");
		status = false;
	   }
	}

	if(i ==  count){
		System.out.println("Able to create max connections");
		status=true;
		try{
			ds.getConnection();
                	System.out.println("Able to create beyond max connections");
                	status=false;
		}catch(SQLException ex){
                	System.out.println("Unable to create beyond max connections");
		        status=true;
           	}
	}
        
        for(; i > 0; i--){
	   try{
            	con[i - 1].close();
	   }catch(SQLException ex){
		System.out.println("Unable to close connection");
	   }
	}
	return status;
    }

    private Connection[] openConnections(DataSource ds, int count) throws NamingException, SQLException {
        
        Connection[] con = new Connection[count];
        for(int i=0; i < count; i++)
            con[i] = ds.getConnection("scott", "tiger");
        
        return con;
    }
    
    private void closeConnections(Connection[] con) throws SQLException {
        for(int i=0; i < count; i++)
            con[i].close();
    }
    
    public void ejbLoad() {}
    public void ejbStore() {}
    public void ejbRemove() {}
    public void ejbActivate() {}
    public void ejbPassivate() {}
    public void unsetEntityContext() {}
    public void ejbPostCreate() {}
}
