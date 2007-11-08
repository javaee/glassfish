package com.sun.s1asdev.jdbc.onlygetconnection.ejb;


import java.util.*;
import java.io.*;
import java.rmi.*;
import javax.ejb.*;
import javax.transaction.UserTransaction;
import javax.naming.*;
import javax.sql.*;
import java.sql.*;

public class NoTxConnTestBean implements SessionBean {

    private EJBContext ejbcontext;
    private transient javax.ejb.SessionContext m_ctx = null;
    transient javax.sql.DataSource ds;
	

    public void setSessionContext(javax.ejb.SessionContext ctx) {
        m_ctx = ctx;
    }

    public void ejbCreate() {}

    public void ejbRemove() {}

    public void ejbActivate() {}

    public void ejbPassivate() {}

    public boolean test1() {
	Connection conn = null;
	Statement stmt = null;
	ResultSet rs = null;

	Connection conn1 = null;
	Statement stmt1 = null; 
	ResultSet rs1 = null;

        try {

            InitialContext ctx = new InitialContext();
            ds = (javax.sql.DataSource) ctx.lookup("java:comp/env/jdbc/onlygetconnection");
            conn = ds.getConnection();
            stmt = conn.createStatement();
            String query1 = "SELECT * FROM ONLYGETCONNECTION";
            rs = stmt.executeQuery(query1);
	    
            /*
            rs.close();
	    stmt.close();
	    conn.close();
	    conn1 = ds.getConnection();
            stmt1 = conn1.createStatement();
            rs1 = stmt1.executeQuery(query1);

            rs1.close();
	    stmt1.close();
	    conn1.close();
            */

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
            	
       	}
    }



}



