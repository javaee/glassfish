package com.sun.s1asdev.jdbc.lazyassoc.ejb;

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
    private DataSource ds;
    public void setSessionContext(SessionContext context) {
        ctxt_ = context;
	try {
	    ic_ = new InitialContext();
	    ds = (DataSource)ic_.lookup("java:comp/env/DataSource");
	} catch( NamingException ne ) {
	    ne.printStackTrace();
	}
    }

    public void ejbCreate() throws CreateException {
    }

    public boolean test1() throws Exception {
	Connection conn1 = null;
	boolean passed = false;
	//clean the database
	try {
	    conn1 = ds.getConnection();
	} catch( Exception e) {
	    e.printStackTrace();
	    return false;
        }
	
	return true;
    }

    public boolean test2() throws Exception {
	Connection conn1 = null;
        Statement stmt = null;
	boolean passed = false;
	//clean the database
	try {
	    conn1 = ds.getConnection();
            stmt = conn1.createStatement();
            stmt.executeQuery( "SELECT * FROM TXLEVELSWITCH");
	} catch( Exception e) {
	    e.printStackTrace();
	    return false;
	} finally {
            if (stmt != null ) {
                try { stmt.close(); }catch( Exception e ) {}
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
