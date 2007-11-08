package com.sun.s1asdev.jdbc.stress.ejb;

import javax.ejb.*;
import javax.naming.*;
import javax.sql.*;
import java.rmi.*;
import java.util.*;
import java.sql.*;

public class SimpleBMPBean implements EntityBean {

    //protected ConnectionPoolDataSource ds;
    protected DataSource ds;
    int id;

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
    
    public boolean test1(int testId) {
	Connection conn = null;
	boolean passed = true;
	try {
	    conn = ds.getConnection();
            insertEntry( testId, "1234567890", conn);
            //queryTable( conn );
	    emptyTable( conn, testId );
	} catch (Exception e) {
	    e.printStackTrace();
	    passed = false;
	} finally {
	    if ( conn != null ) {
	        try { conn.close(); } catch( Exception e1) {}    
	    }
	}

	return passed;
	
    }
    

    private void insertEntry( int id, String phone, Connection con )
        throws SQLException {
	
        PreparedStatement stmt = con.prepareStatement(
	    "insert into O_Customer values (?, ?)" );

	stmt.setInt(1, id);
	stmt.setString(2, phone);

	stmt.executeUpdate();
	stmt.close();
    }

    private void emptyTable( Connection con, int testId ) {
        try {
            Statement stmt = con.createStatement();
            
	    stmt.execute("delete * from O_Customer WHERE c_id="+testId);
	    stmt.close();
        } catch( Exception e) {
	}
	    
    }

    private void queryTable( Connection con ) {
        try {
	    Statement stmt = con.createStatement();
	    ResultSet rs = stmt.executeQuery("select * from O_Customer");
	    while( rs.next() ) ;
	    rs.close();
	    stmt.close();
	} catch( Exception e) {
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
