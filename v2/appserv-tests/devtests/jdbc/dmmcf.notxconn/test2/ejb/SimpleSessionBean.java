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
            conn = ds.getConnection("scott", "tiger");
            stmt = conn.createStatement();
	      stmt.executeUpdate("INSERT INTO NOTXCONNTABLE VALUES('method1',3)");
            String query1 = "SELECT * FROM NOTXCONNTABLE";
            rs = stmt.executeQuery(query1);
	      noTxConn = ((com.sun.appserv.jdbc.DataSource)ds).getNonTxConnection("scott", "tiger");

	      stmt2 = noTxConn.createStatement();
            rs2 = stmt2.executeQuery("SELECT * FROM NOTXCONNTABLE");
	      tx.commit();
            if ( rs2.next() ) {
	        return false;
	      }
            
	      
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
