package com.sun.s1asdev.jdbc.notxconn.test2.ejb;


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
    private transient javax.ejb.SessionContext ctx_;
    transient com.sun.appserv.jdbc.DataSource ds;
	

    public void setSessionContext(javax.ejb.SessionContext ctx) {
        ctx_ = ctx;
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
        
           InitialContext context = new InitialContext();
           ds = (com.sun.appserv.jdbc.DataSource) context.lookup("java:comp/env/jdbc/notxconn");
           
           
           UserTransaction tx = (UserTransaction)ctx_.getUserTransaction();
           tx.begin();
           conn = ds.getConnection("aditya", "aditya");
           stmt = conn.createStatement();
           stmt.executeUpdate("INSERT INTO NOTXCONNTABLE VALUES('method1',3)");
           String query1 = "SELECT * FROM NOTXCONNTABLE";
           rs = stmt.executeQuery(query1);
           noTxConn = ((com.sun.appserv.jdbc.DataSource)ds).
               getNonTxConnection("aditya", "aditya");
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
            try {
                if (rs != null ) {
                    rs.close();
                }
                if (stmt != null ) {
                    stmt.close();
                }
                if (conn != null ) {
                    conn.close();
                }
                
                if (rs2 != null ) {
                    rs2.close();
                }
                if (stmt2 != null ) {
                    stmt2.close();
                }
                if (noTxConn != null ) {
                    noTxConn.close();
                }
            } catch( Exception e1 ) {}
        }
    }
}



