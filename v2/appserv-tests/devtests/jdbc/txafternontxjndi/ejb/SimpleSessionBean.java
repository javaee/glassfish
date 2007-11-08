package com.sun.s1asdev.jdbc.txafternontx.ejb;

import javax.ejb.*;
import javax.naming.*;
import javax.sql.*;
import java.rmi.*;
import java.util.*;
import java.sql.*;
import javax.transaction.UserTransaction;

public class SimpleSessionBean implements SessionBean
{

    private SessionContext ctxt_;
    private InitialContext ic_; 
    private InitialContext ic1_; 
    public void setSessionContext(SessionContext context) {
        ctxt_ = context;
	try {
	    ic_ = new InitialContext();
            Hashtable ht = new Hashtable();
            ht.put("com.sun.enterprise.connectors.jndisuffix", "__nontx");
	    ic1_ = new InitialContext(ht);
	} catch( NamingException ne ) {
	    ne.printStackTrace();
	}
    }

    public void ejbCreate() throws CreateException {
    }

    /**
     */
    public boolean test1() throws Exception {
	com.sun.appserv.jdbc.DataSource ds = 
	    (com.sun.appserv.jdbc.DataSource)ic_.lookup("java:comp/env/DataSource");
	com.sun.appserv.jdbc.DataSource ds1 = 
	    (com.sun.appserv.jdbc.DataSource)ic1_.lookup("jdbc/txafternontx");
	Connection conn1 = null;
	Statement stmt1 = null;
	ResultSet rs1 = null;
	boolean passed = false;
        System.out.println("getting first tx connection");
	try {
	    conn1 = ds.getConnection();
	    if (conn1.getAutoCommit() == true ) {
	        throw new SQLException("Before nontx: Connection with wrong autocommit value");
	    }
	} catch( SQLException e) {
	    e.printStackTrace();
	    return false;
	} finally {
	    if (conn1 != null) { 
	        try { conn1.close(); } catch( Exception e1 ) {}
	    }
	}
	
        System.out.println("getting nontx connection");
        try {
	    conn1 = ds1.getConnection();
	    if (conn1.getAutoCommit() == false ) {
	        throw new SQLException("NonTX Connection with wrong autocommit value");
	    }
	} catch( Exception e ) {
	    e.printStackTrace();
	    return false;
	} finally {
	    try {conn1.close();} catch(Exception e) {}
	}

        System.out.println("getting second tx connection");
	try {
	    conn1 = ds.getConnection();
	    if (conn1.getAutoCommit() == true ) {
	        throw new SQLException("After nontx: Connection with wrong autocommit value");
	    }
	} catch( SQLException e) {
	    e.printStackTrace();
	    return false;
	} finally {
	    if (conn1 != null) { 
	        try { conn1.close(); } catch( Exception e1 ) {}
	    }
	}

	return true;
    }
    
    public void ejbLoad() {}
    public void ejbStore() {}
    public void ejbRemove() {}
    public void ejbActivate() {}
    public void ejbPassivate() {}
    public void unsetEntityContext() {}
    public void ejbPostCreate() {}
}
