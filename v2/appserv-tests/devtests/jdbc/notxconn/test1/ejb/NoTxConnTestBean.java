package com.sun.s1asdev.jdbc.notxconn.test1.ejb;


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
    transient com.sun.appserv.jdbc.DataSource ds;
	

    public void setSessionContext(javax.ejb.SessionContext ctx) {
        m_ctx = ctx;
    }

    public void ejbCreate() {}

    public void ejbRemove() {}

    public void ejbActivate() {}

    public void ejbPassivate() {}

    public boolean test1() {
	Connection conn = null;
	Connection noTxConn = null;
	Statement stmt = null;
	Statement stmt2 = null;
	ResultSet rs = null;
	ResultSet rs2 = null;

        try {

            InitialContext ctx = new InitialContext();
            ds = (com.sun.appserv.jdbc.DataSource) ctx.lookup("java:comp/env/jdbc/notxconn");

	   
            UserTransaction tx =(UserTransaction)m_ctx.getUserTransaction();
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

    public boolean test2() {
	Connection conn = null;
	Connection noTxConn = null;

        try {

            InitialContext ctx = new InitialContext();
            ds = (com.sun.appserv.jdbc.DataSource) ctx.lookup("java:comp/env/jdbc/notxconn");

            UserTransaction tx =(UserTransaction)m_ctx.getUserTransaction();
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


}



