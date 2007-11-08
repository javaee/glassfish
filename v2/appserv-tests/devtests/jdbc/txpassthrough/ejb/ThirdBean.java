package com.sun.s1asdev.jdbc.txpassthrough.ejb;


import java.util.*;
import java.io.*;
import java.rmi.*;
import javax.ejb.*;
import javax.transaction.UserTransaction;
import javax.naming.*;
import javax.sql.*;
import java.sql.*;

public class ThirdBean implements SessionBean {

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
	System.out.println(" @@@@ Entering Bean 3 @@@@");
	Connection conn = null;
	Statement stmt = null;
	ResultSet rs = null;

        try {

            InitialContext ctx = new InitialContext();
            ds = (javax.sql.DataSource) ctx.lookup("java:comp/env/jdbc/txpassthrough");
            conn = ds.getConnection("dbuser", "dbpassword");
            stmt = conn.createStatement();
            	String query1 = "SELECT * FROM ONLYGETCONNECTION";
            rs = stmt.executeQuery(query1);
	    return true;
        } catch (Exception e) {
	    System.out.println("Caught Exception in 3rd Bean---");
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
            System.out.println(" @@@@ Exiting Bean 3 @@@@");	
       	}
    }



}



