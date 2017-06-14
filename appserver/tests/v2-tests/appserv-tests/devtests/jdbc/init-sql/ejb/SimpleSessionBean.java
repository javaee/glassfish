package com.sun.s1asdev.jdbc.initsql.ejb;

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
    private InitialContext ic;
    private DataSource ds1; 

    public void setSessionContext(SessionContext ctxt) {
        this.context = ctxt;
	try {
	    ic = new InitialContext();
	    ds1 = (com.sun.appserv.jdbc.DataSource)ic.lookup("java:comp/env/DataSource1");
	} catch( Exception ne ) {
	    ne.printStackTrace();
	}
    }

    public void ejbCreate() throws CreateException {
    }

    /**
     * Test to select the names from a table. 
     *
     * The result set would contain different number of rows based on 
     * a session property set during the initialization sql phase. 
     * Based on the property set, the number of rows are compared to 
     * test the feature.
     */
    public boolean test1(boolean caseSensitive) {
        Connection con = null;
	Statement stmt = null;
	ResultSet rs = null;
	String query = "Select name from WORKERS where name='Joy Joy'";
	boolean result = false;
	int size = 0;
	try {
	    con = ds1.getConnection();
	    stmt = con.createStatement();
	    rs = stmt.executeQuery(query);
	    if(rs != null) {
		while(rs.next()) {
		    size++;
		}
	    }
	    if(caseSensitive) {
	        result = size == 1;
	    } else {
		result = size == 3;
	    }
	} catch (SQLException ex) {
	    result = false;
	    ex.printStackTrace();
	} finally {
            if(rs != null) {
		try {
		    rs.close();
		} catch(Exception ex) {}
	    }
	    if(stmt != null) {
		try {
		    stmt.close();
		} catch(Exception ex) {}
	    }
	    if(con != null) {
		try {
		    stmt.close();
		} catch(Exception ex) {}
	    }
	}
	return result;
    }

    public void ejbStore() {}
    public void ejbRemove() {}
    public void ejbActivate() {}
    public void ejbPassivate() {}
    public void unsetEntityContext() {}
    public void ejbPostCreate() {}
}
