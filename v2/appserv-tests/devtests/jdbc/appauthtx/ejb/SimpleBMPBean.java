package com.sun.s1asdev.jdbc.appauthtx.ejb;

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
    int id;

    public void setEntityContext(EntityContext entityContext) {
	Context context = null;
	try {
	    context    = new InitialContext();
	    ds = (DataSource) context.lookup("java:comp/env/DataSourcetx");
	} catch (NamingException e) {
	    throw new EJBException("cant find datasource");
	}
    System.out.println("[**SimpleBMPBean**] Done with setEntityContext....");
    }

    public Integer ejbCreate() throws CreateException {
	return new Integer(1);
    }

    public boolean test1() {
        //application auth + user/pwd not specified - should fail
	Connection conn = null;
	boolean passed = false;
	try {
	    conn = ds.getConnection();
	} catch (Exception e) {
	    passed = true;
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
        //application auth + user/pwd  specified - should pass
	Connection conn = null;
	boolean passed = true;
	try {
	    conn = ds.getConnection("DBUSER", "DBPASSWORD");
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

    public boolean test3() {
        //application auth + wrong user/pwd 
	Connection conn = null;
	boolean passed = false;
	try {
	    conn = ds.getConnection("xyz", "xyz" );
	} catch (Exception e) {
	    passed = true;
	} finally {
	    if ( conn != null ) {
	        try {
	            conn.close();
		} catch( Exception e1) {}    
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
