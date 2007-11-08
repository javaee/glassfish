package com.sun.s1asdev.jdbc.reconfig.maxpoolsize.ejb;

import javax.ejb.*;
import javax.naming.*;
import javax.sql.*;
import java.rmi.*;
import java.util.*;
import java.sql.*;

public class SimpleBMPBean implements EntityBean {

    protected DataSource ds;
    int id;

    public void setEntityContext(EntityContext entityContext) {
    }

    public Integer ejbCreate() throws CreateException {
	return new Integer(1);
    }

    /** 
     * The basic strategy here is that we try to get 1 more connection
     * than the maxpoolsize. This single extra getConnection should not
     * pass. If this happens, the test has passed.
     */

    public boolean test1( int maxPoolSize, boolean throwException, boolean useXA ) {
        try {
	    InitialContext ic = new InitialContext();
	    if ( useXA ) {
	        ds = (DataSource) ic.lookup("java:comp/env/DataSource_xa");
	    } else {
	        ds = (DataSource) ic.lookup("java:comp/env/DataSource");
	    }
	} catch( Exception e ) {
	    e.printStackTrace();
	    return false;
	}
	boolean passed = true;
	Connection[] conns = new Connection[maxPoolSize];
        for( int i = 0; i < maxPoolSize; i++ ) {
	    System.out.println("throwException is : " + throwException );
	    try {
		System.out.println("########Getting connection : " + i );
	        conns[i] = ds.getConnection();
	    } catch (Exception e) {
		e.printStackTrace();
	        return false;
	    } 

	}    
	//try getting an extra connection
	System.out.println("---Try getting extra connection");
	Connection con = null;
	try {
	    con = ds.getConnection();
	} catch( Exception e) {
	    System.out.print("Caught exception : " ) ;
	    if ( throwException ) {
		System.out.println("Setting passed to true");
		passed = true;
            } else {
	        passed = false;
            }    

        } finally {
            try { con.close(); } catch ( Exception e ) {}
        }
        
	for (int i = 0 ; i < maxPoolSize;i++ ) {
	    try {
	        conns[i].close();
	    } catch( Exception e) {
	        //passed = false;
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
