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

package com.sun.s1asdev.jdbc.tracingsql.ejb;

import javax.ejb.*;
import javax.naming.*;
import javax.sql.*;
import java.sql.*;


public class SimpleBMPBean
    implements EntityBean{

    protected DataSource ds;
    protected DataSource tracingds;

    public void setEntityContext(EntityContext entityContext) {
	Context context = null;
	try {
	    context    = new InitialContext();
	    ds = (DataSource) context.lookup("java:comp/env/DataSource");
	    tracingds = (DataSource) context.lookup("java:comp/env/TracingDataSource");
	} catch (NamingException e) {
	    throw new EJBException("cant find datasource");
	}
        System.out.println("[**SimpleBMPBean**] Done with setEntityContext....");
    }

    public Integer ejbCreate() throws CreateException {
	    return new Integer(1);
    }

    public boolean statementTest() {
        boolean result = false;
        Connection conFromDS = null;
        Connection conFromStatement = null;
        Statement stmt = null;
        try{
            conFromDS = ds.getConnection();
            stmt = conFromDS.createStatement();
            conFromStatement = stmt.getConnection();

		System.out.println("statement Test : conFromDS : " + conFromDS);
		System.out.println("statement Test : conFromStatement : " + conFromStatement);

            if( conFromDS==conFromStatement || conFromDS.equals(conFromStatement) ){
                result = true;
            }

            System.out.println("Inserting null entry into null_entry_table");
            stmt.executeUpdate("INSERT INTO null_entry_table VALUES(null)");



        }catch(SQLException sqe){}finally{
            try{
                if(stmt != null){
                    stmt.close();
                }
            }catch(SQLException sqe){}

            try{
                if(conFromDS != null){
                    conFromDS.close();
                }
            }catch(SQLException sqe){}
        }
        return result;
   }

    public boolean preparedStatementTest() {
        boolean result = false;
        Connection conFromDS = null;
        Connection conFromStatement = null;
        PreparedStatement stmt = null;
        PreparedStatement stmt2 = null;
        try {
            conFromDS = ds.getConnection();
            stmt = conFromDS.prepareStatement(
                "select * from customer_stmt_wrapper");
            conFromStatement = stmt.getConnection();

            System.out
                .println("Prepared statement Test : conFromDS : " + conFromDS);
            System.out.println("Prepared statement Test : conFromStatement : " +
                conFromStatement);
            if (conFromDS == conFromStatement ||
                conFromDS.equals(conFromStatement)) {
                result = true;
            }

            // Test to ensure that inserting null values does not result in tracing implementations to fail.
            System.out.println("Inserting null entry into null_entry_table");

            stmt2 = conFromDS
                .prepareStatement("INSERT INTO null_entry_table VALUES(?)");
            stmt2.setString(1, null);
            stmt2.executeUpdate();

        } catch (SQLException sqe) {
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException sqe) {
            }

            try {
                if (stmt2 != null) {
                    stmt2.close();
                }
            } catch (SQLException sqe) {
            }

            try {
                if (conFromDS != null) {
                    conFromDS.close();
                }
            } catch (SQLException sqe) {
            }
        }
        return result;
    }
    public boolean callableStatementTest(){
        boolean result = false;
        Connection conFromDS = null;
        Connection conFromStatement = null;
        CallableStatement stmt = null;
        try{
            conFromDS = ds.getConnection();
            stmt = conFromDS.prepareCall("select * from customer_stmt_wrapper");
            conFromStatement = stmt.getConnection();

		System.out.println("Callable statement Test : conFromDS : " + conFromDS);
		System.out.println("Callable statement Test : conFromStatement : " + conFromStatement);
            if( conFromDS==conFromStatement || conFromDS.equals(conFromStatement) ){
                result = true;
            }

        }catch(SQLException sqe){}finally{
            try{
                if(stmt != null){
                    stmt.close();
                }
            }catch(SQLException sqe){}

            try{
                if(conFromDS != null){
                    conFromDS.close();
                }
            }catch(SQLException sqe){}
        }
        return result;
    }
    public boolean metaDataTest(){
     boolean result = false;
        Connection conFromDS = null;
        Connection conFromMetaData = null;
        DatabaseMetaData dbmd = null;
        try{
            conFromDS = ds.getConnection();
            dbmd = conFromDS.getMetaData();
            conFromMetaData = dbmd.getConnection();

		System.out.println("statementTest : conFromDS : " + conFromDS);
		System.out.println("statementTest : conFromDbMetadata : " + conFromMetaData);
            if( conFromDS==conFromMetaData || conFromDS.equals(conFromMetaData) ){
                result = true;
            }

        }catch(SQLException sqe){}finally{
            try{
                if(conFromDS != null){
                    conFromDS.close();
                }
            }catch(SQLException sqe){}
        }
        return result;
    }
    public boolean resultSetTest(){
        boolean result = false;
        Connection conFromDS = null;
        Connection conFromResultSet = null;
        Statement stmt = null;
        ResultSet rs = null;
        try{
            conFromDS = ds.getConnection();
            stmt = conFromDS.createStatement();
            rs = stmt.executeQuery("select * from customer_stmt_wrapper");
            conFromResultSet = rs.getStatement().getConnection();

		System.out.println("ResultSet test : conFromDS : " + conFromDS);
		System.out.println("ResultSet test : conFromResultSet: " + conFromResultSet);
            if( conFromDS==conFromResultSet || conFromDS.equals(conFromResultSet) ){
                result = true;
            }
        }catch(SQLException sqe){}finally{

            try{
                if(rs != null){
                    rs.close();
                }
            }catch(SQLException sqe){}

            try{
                if(stmt != null){
                    stmt.close();
                }
            }catch(SQLException sqe){}

            try{
                if(conFromDS != null){
                    conFromDS.close();
                }
            }catch(SQLException sqe){}
        }
        return result;
    }

    public boolean compareRecords() {
	boolean result = false;
        Connection conFromDS = null;
	Connection con1 = null;
	Statement stmt1 = null;
	Statement stmt = null;
	ResultSet rs = null;
	ResultSet rs1 = null;
        try{
            conFromDS = tracingds.getConnection();
	    con1 = tracingds.getConnection();
            stmt = conFromDS.createStatement();
	    stmt1 = con1.createStatement();
            rs = stmt.executeQuery("select * from expected_sql_trace");
	    rs1 = stmt1.executeQuery("select * from sql_trace");

            System.out.println("@@@@ ------------------------------------------");
	    for(int i=0; i<5; i++) {

	        String className ="";
	        String methodName="";
                String args="";
                String expectedClassName="";
                String expectedMethodName="";
                String expectedArgs="";
	        if(rs1.next()) {	
                className = rs1.getString(1).trim();
		methodName = rs1.getString(2).trim();
		args = rs1.getString(3).trim();
		System.out.println("@@@@@ class=" + className + "---");
		System.out.println("@@@@@ method=" + methodName + "---");
		System.out.println("@@@@@ args=" + args + "---");
		}
		
		if(rs.next()) {
	    	expectedClassName = rs.getString(1).trim();
	        expectedMethodName = rs.getString(2).trim();
		expectedArgs = rs.getString(3).trim();
		System.out.println("@@@@@ expectedClass = " + expectedClassName + "---");
		System.out.println("@@@@@ expectedMethod = " + expectedMethodName + "---");
		System.out.println("@@@@@ expectedArgs = " + expectedArgs + "---");
		}
          System.out.println("@@@@ ------------------------------------------");
		
	    	if(className.equals(expectedClassName) && methodName.equals(expectedMethodName) && args.equals(expectedArgs)) {
		        result = true;
		    } else {
			return false;
		    }

		    /*if(className != null && expectedClassName != null) {
			if(className.equals(expectedClassName))
  		            result = true;
		    }
		    if(methodName != null && expectedMethodName != null) {
			if(methodName.equals(expectedMethodName)) 
			    result = true;
		    }
		    if(args != null && expectedArgs != null) {
			if(args.equals(expectedArgs))
			    result = true;
		    }*/
	    }

        }catch(SQLException sqe){
	}finally{

            try{
                if(stmt != null){
                    stmt.close();
                }
            }catch(SQLException sqe){}
            try{
                if(stmt1 != null){
                    stmt1.close();
                }
            }catch(SQLException sqe){}

            try{
                if(conFromDS != null){
                    conFromDS.close();
                }
            }catch(SQLException sqe){}
            try{
                if(con1 != null){
                    con1.close();
                }
            }catch(SQLException sqe){}

            try{
                if(rs != null){
                    rs.close();
                }
            }catch(SQLException sqe){}
            try{
                if(rs1 != null){
                    rs1.close();
                }
            }catch(SQLException sqe){}
        }
	return result;
    }

    public void ejbLoad() {}
    public void ejbStore() {}
    public void ejbRemove() {}
    public void ejbActivate() {}
    public void ejbPassivate() {}
    public void unsetEntityContext() {}
    public void ejbPostCreate() {}
}
