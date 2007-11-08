package com.sun.s1asdev.jdbc.autocommit.ejb;

import javax.ejb.*;
import javax.naming.*;
import javax.sql.*;
import java.rmi.*;
import java.util.*;
import java.sql.*;

public class SimpleBMPBean
    implements EntityBean
{

    protected DataSource ds;

    public void setEntityContext(EntityContext entityContext) {
	Context context = null;
	try {
	    context    = new InitialContext();
	    ds = (DataSource) context.lookup("java:comp/env/DataSource");
	} catch (NamingException e) {
	    throw new EJBException("cant find datasource");
	}
    }

    public Integer ejbCreate() throws CreateException {
	return new Integer(1);
    }

    /* Get a single connection and close it */
    public boolean test1() {
        Connection conn = null;
        boolean passed = true;
	try {
	    conn = ds.getConnection();
	    passed = !conn.getAutoCommit();
	} catch (Exception e) {
	    passed = false;
	} finally {
	    if ( conn != null ) {
	        try {
	            conn.close();
	        } catch( Exception e1) {}    
	    }
        } 
        
	return passed;
    }

    public boolean test2() {
        Connection conn1  = null;
        Connection conn2  = null;
	boolean passed = true;

	try {
	    conn1 = ds.getConnection();
	    conn2 = ds.getConnection();

	    passed = conn1.getAutoCommit() & conn2.getAutoCommit();
	} catch( Exception e ) {
	    passed = false;
	} finally {
	    if (conn1 != null ) {
	        try {
		    conn1.close();
		} catch( Exception ei) {}
	    }
	    if (conn2 != null ) {
	        try {
		    conn2.close();
		} catch( Exception ei) {}
	    }
	}

	return passed;
    }
   

    public void ejbLoad() {}
    public void ejbStore() {}
    public void ejbRemove() {}
    public void ejbActivate() {}
    public void ejbPassivate() {}
    public void unsetEntityContext() {}
    public void ejbPostCreate() {}
}
