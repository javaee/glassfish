package com.sun.s1asdev.jdbc.connsharing.xa.ejb;

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

    /**
     * Get connection and do some database inserts. Then call another
     * EJB's method in the same transaction and change the inserted value.
     * Since all this is in the same tx, the other bean's method should 
     * get the same connection (physical) and hence be able to see the
     * inserted value even though the tx has not committed yet.
     * The idea is to test connection sharing
     */
    public boolean test1() throws Exception {
	DataSource ds = (DataSource)ic_.lookup("java:comp/env/DataSource");
	Connection conn1 = null;
	Statement stmt1 = null;
	ResultSet rs1 = null;
	boolean passed = false;

	try {
	    conn1 = ds.getConnection();
	    stmt1 = conn1.createStatement();
	    stmt1.executeUpdate( "INSERT INTO CONNSHARING values (100, 'ADITYA')");
            
	    Object o = ic_.lookup("java:comp/env/ejb/SimpleSession2EJB");
	    SimpleSession2Home home = (SimpleSession2Home) 
	        javax.rmi.PortableRemoteObject.narrow( o, SimpleSession2Home.class);
	    SimpleSession2 bean = home.create();

	    return bean.test1("ADITYA_BEAN_2", 100);

	} catch( SQLException e) {
	    e.printStackTrace();
	    return false;
	} finally {
	    if (stmt1 != null) { 
	        try { stmt1.close(); } catch( Exception e1 ) {}
	    }
	    if (conn1 != null) { 
	        try { conn1.close(); } catch( Exception e1 ) {}
	    }
	}
    }
    
    /**
     * Get connection and do some database inserts. Then call another
     * EJB's method in the same transaction and change the inserted value.
     * Since all this is in the same tx, the other bean's method should 
     * get the same connection (physical) and hence be able to see the
     * inserted value even though the tx has not committed yet.
     * This test does the same thing as test1 except that it closes the
     * connection it obtains and then opens a new connection in bean2's method
     * The idea is to test connection sharing
     */
    public boolean test2() throws Exception {
	DataSource ds = (DataSource)ic_.lookup("java:comp/env/DataSource");
	Connection conn1 = null;
	Statement stmt1 = null;
	ResultSet rs1 = null;
	boolean passed = false;

	try {
	    conn1 = ds.getConnection();
	    stmt1 = conn1.createStatement();
	    stmt1.executeUpdate( "INSERT INTO CONNSHARING values (100, 'ADITYA')");
            
	    stmt1.close();
	    conn1.close();

	    Object o = ic_.lookup("java:comp/env/ejb/SimpleSession2EJB");
	    SimpleSession2Home home = (SimpleSession2Home) 
	        javax.rmi.PortableRemoteObject.narrow( o, SimpleSession2Home.class);
	    SimpleSession2 bean = home.create();

	    return bean.test1("ADITYA_BEAN_2", 100);

	} catch( SQLException e) {
	    e.printStackTrace();
	    return false;
	} finally {
	    if (stmt1 != null) { 
	        try { stmt1.close(); } catch( Exception e1 ) {}
	    }
	    if (conn1 != null) { 
	        try { conn1.close(); } catch( Exception e1 ) {}
	    }
	}
    }

    public boolean test3() throws Exception {
	DataSource ds = (DataSource)ic_.lookup("java:comp/env/DataSource");
	Connection conn1 = null;
	Connection conn2 = null;
	Statement stmt1 = null;
	Statement stmt2 = null;
	ResultSet rs1 = null;
	boolean passed = false;

	try {
	    conn1 = ds.getConnection();
	    stmt1 = conn1.createStatement();
	    stmt1.executeUpdate( "INSERT INTO CONNSHARING values (100, 'ADITYA')");
            
	    conn2 = ds.getConnection();
	    stmt2 = conn2.createStatement();
	    stmt2.executeUpdate( "INSERT INTO CONNSHARING values (200, 'ADITYA_200')");

	    Object o = ic_.lookup("java:comp/env/ejb/SimpleSession2EJB");
	    SimpleSession2Home home = (SimpleSession2Home) 
	        javax.rmi.PortableRemoteObject.narrow( o, SimpleSession2Home.class);
	    SimpleSession2 bean = home.create();

	    return bean.test1("ADITYA_BEAN_2_2", 200);

	} catch( SQLException e) {
	    e.printStackTrace();
	    return false;
	} finally {
	    if (stmt1 != null) { 
	        try { stmt1.close(); } catch( Exception e1 ) {}
	    }
	    if (conn1 != null) { 
	        try { conn1.close(); } catch( Exception e1 ) {}
	    }
	}
    }

    public boolean test4() throws Exception {
	DataSource ds = (DataSource)ic_.lookup("java:comp/env/DataSource");
	Connection conn1 = null;
	Connection conn2 = null;
	Statement stmt1 = null;
	Statement stmt2 = null;
	boolean passed = false;

	try {
	    conn1 = ds.getConnection();
	    stmt1 = conn1.createStatement();
	    stmt1.executeUpdate( "INSERT INTO CONNSHARING values (100, 'ADITYA')");
            
	    stmt1.close();
            conn1.close();

	    conn2 = ds.getConnection();
	    stmt2 = conn2.createStatement();
	    stmt2.executeUpdate( "INSERT INTO CONNSHARING values (200, 'ADITYA_200')");

	    stmt2.close();
	    conn2.close();

	    Object o = ic_.lookup("java:comp/env/ejb/SimpleSession2EJB");
	    SimpleSession2Home home = (SimpleSession2Home) 
	        javax.rmi.PortableRemoteObject.narrow( o, SimpleSession2Home.class);
	    SimpleSession2 bean = home.create();

	    return bean.test1("ADITYA_BEAN_2_2", 200);

	} catch( SQLException e) {
	    e.printStackTrace();
	    return false;
	} finally {
	    if (stmt1 != null) { 
	        try { stmt1.close(); } catch( Exception e1 ) {}
	    }
	    if (conn1 != null) { 
	        try { conn1.close(); } catch( Exception e1 ) {}
	    }
	}
    }

    /**
     * Query the value modified in the second bean and ensure that it
     * is correct.
     */
    public boolean query() throws Exception {
        DataSource ds = (DataSource) ic_.lookup("java:comp/env/DataSource");
	Connection conn = null;
	Statement stmt = null;
	ResultSet rs = null;

	try {
	    conn = ds.getConnection();
	    stmt = conn.createStatement();
	    rs = stmt.executeQuery("SELECT * FROM CONNSHARING WHERE c_id=100");
	    if ( rs.next() ) {
                String str = rs.getString( 2 );
		System.out.println(" str => " + str );
		if( "ADITYA_BEAN_2".equals(str.trim()) ) {
		    return true;
		} 
	    }



	    return false;
	} catch( SQLException e ) {
	    e.printStackTrace();
	    return false;
	} finally {
	    if( rs != null ) {
	        try { rs.close() ; }catch( Exception e ) {}
	    }

	    if( stmt != null) {
	        try { stmt.close(); } catch(Exception e) { }
	    }
            
	    //cleanup table
	    try {
	        stmt = conn.createStatement();
		stmt.executeUpdate( "DELETE FROM CONNSHARING WHERE c_id=100" );
	        stmt.close();	
	    } catch( Exception e ) { 
	        e.printStackTrace();
	    }

	    if( conn != null ) { try { conn.close(); } catch(Exception e) { }
	    }
	}
	
    }

    /**
     * Query the value modified in the second bean and ensure that it
     * is correct.
     */
    public boolean query2() throws Exception {
        DataSource ds = (DataSource) ic_.lookup("java:comp/env/DataSource");
	Connection conn = null;
	Statement stmt = null;
	ResultSet rs = null;
	ResultSet rs1 = null;
	String str1 = null;
	String str2 = null;
	

	try {
	    conn = ds.getConnection();
	    stmt = conn.createStatement();
	    rs = stmt.executeQuery("SELECT * FROM CONNSHARING WHERE c_id=100");
	    if ( rs.next() ) {
                str1 = rs.getString(2);
                System.out.println(" str1 => " + str1 );
	    }

	    rs1 = stmt.executeQuery("SELECT * FROM CONNSHARING WHERE c_id=200");
	    if ( rs1.next() ) {
	        str2 = rs1.getString(2);
                System.out.println(" str2 => " + str2 );
	    }
            if( "ADITYA".equals(str1.trim()) && 
	        "ADITYA_BEAN_2_2".equals(str2.trim())) {
                return true;
            } 

	    return false;
	} catch( SQLException e ) {
	    e.printStackTrace();
	    return false;
	} finally {
	    if( rs != null ) {
	        try { rs.close() ; }catch( Exception e ) {}
	    }

            if( rs1 != null) {
	        try { rs1.close(); } catch(Exception e) { }
	    }

	    if( stmt != null) {
	        try { stmt.close(); } catch(Exception e) { }
	    }

   
	    //cleanup table
	    try {
	        stmt = conn.createStatement();
		stmt.executeUpdate( "DELETE FROM CONNSHARING WHERE c_id=100" );
	        stmt.close();	
	    } catch( Exception e ) { 
	        e.printStackTrace();
	    }

	    try {
	        stmt = conn.createStatement();
		stmt.executeUpdate( "DELETE FROM CONNSHARING WHERE c_id=200" );
	        stmt.close();	
	    } catch( Exception e ) { 
	        e.printStackTrace();
	    }

	    if( conn != null ) { try { conn.close(); } catch(Exception e) { }
	    }
	}
	
    }

    public void ejbLoad() {}
    public void ejbStore() {}
    public void ejbRemove() {}
    public void ejbActivate() {}
    public void ejbPassivate() {}
    public void unsetEntityContext() {}
    public void ejbPostCreate() {}
}
