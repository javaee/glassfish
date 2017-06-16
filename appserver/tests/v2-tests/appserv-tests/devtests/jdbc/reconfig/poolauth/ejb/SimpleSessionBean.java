package com.sun.s1asdev.jdbc.reconfig.poolauth.ejb;

import javax.ejb.*;
import javax.naming.*;
import javax.sql.*;
import java.rmi.*;
import java.util.*;
import java.sql.*;

public class SimpleSessionBean implements SessionBean
{

    private SessionContext ctxt_;
    private InitialContext ic_; 
    public void setSessionContext(SessionContext context) {
        ctxt_ = context;
	try {
	    ic_ = new InitialContext();
	} catch( NamingException ne ) {
	    ne.printStackTrace();
	}
    }

    public void ejbCreate() throws CreateException {
    }

    public boolean test1() throws Exception {
	DataSource ds = (DataSource)ic_.lookup("java:comp/env/DataSource");
	Connection conn1 = null;
	Statement stmt1 = null;
	boolean passed = false;
	//Get a connection with user,password = dbuser,dbpassword
	//and access a table that only this principal can
	//access
	try {
	    conn1 = ds.getConnection();
	    stmt1 = conn1.createStatement();
	    stmt1.executeQuery("SELECT * FROM DBUSERTABLE");
	} catch( Exception e) {
	    e.printStackTrace();
	    return false;
	} finally {
	    if (stmt1 != null ) { 
	        try { stmt1.close(); } catch(Exception e) {}
	    }
	    if (conn1 != null) { 
	        try { conn1.close(); } catch( Exception e1 ) {}
	    }
	}

	//Now try querying a table owned by another principal and
	//expect an exception
	try {
	    conn1 = ds.getConnection();
	    stmt1 = conn1.createStatement();
	    stmt1.executeQuery("SELECT * FROM PBPUBLICTABLE");
	} catch( Exception e) {
	    e.printStackTrace();
	    return true;
	} finally {
	    if (stmt1 != null ) { 
	        try { stmt1.close(); } catch(Exception e) {}
	    }
	    if (conn1 != null) { 
	        try { conn1.close(); } catch( Exception e1 ) {}
	    }
	}

	return false;
    }

    public boolean test2() throws Exception {
	DataSource ds = (DataSource)ic_.lookup("java:comp/env/DataSource");
	Connection conn1 = null;
	Statement stmt1 = null;
	boolean passed = false;
	//Get a connection with user,password = pbpublic,pbpublic
	//and access a table that only this principal can
	//access
	try {
	    conn1 = ds.getConnection();
	    stmt1 = conn1.createStatement();
	    stmt1.executeQuery("SELECT * FROM PBPUBLICTABLE");
	} catch( Exception e) {
	    e.printStackTrace();
	    return false;
	} finally {
	    if (stmt1 != null ) { 
	        try { stmt1.close(); } catch(Exception e) {}
	    }
	    if (conn1 != null) { 
	        try { conn1.close(); } catch( Exception e1 ) {}
	    }
	}

	//Now try querying a table owned by another principal and
	//expect an exception
	try {
	    conn1 = ds.getConnection();
	    stmt1 = conn1.createStatement();
	    stmt1.executeQuery("SELECT * FROM DBUSERTABLE");
	} catch( Exception e) {
	    e.printStackTrace();
	    return true;
	} finally {
	    if (stmt1 != null ) { 
	        try { stmt1.close(); } catch(Exception e) {}
	    }
	    if (conn1 != null) { 
	        try { conn1.close(); } catch( Exception e1 ) {}
	    }
	}

	return false;
    }

    public void ejbLoad() {}
    public void ejbStore() {}
    public void ejbRemove() {}

    public void ejbActivate() {}
    public void ejbPassivate() {}
    public void unsetEntityContext() {}
    public void ejbPostCreate() {}
}
