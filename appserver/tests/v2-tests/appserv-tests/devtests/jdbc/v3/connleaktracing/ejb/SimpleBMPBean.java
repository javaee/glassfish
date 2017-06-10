package com.sun.s1asdev.jdbc.connectionleaktracing.ejb;

import javax.ejb.*;
import javax.naming.*;
import javax.sql.*;
import java.sql.*;

public class SimpleBMPBean
        implements SessionBean {

    protected DataSource ds;
    private transient javax.ejb.SessionContext m_ctx = null;

    public void setSessionContext(SessionContext context) {
	m_ctx = context;
    }

    public Integer ejbCreate() throws CreateException {
        return new Integer(1);
    }


   
/* Get a single connection and close it */
    public boolean test1() {
        Connection conn = null;
        boolean passed = true;
        Context context = null;
        try {
            context = new InitialContext();
            ds = (DataSource) context.lookup("java:comp/env/DataSource");
        } catch (NamingException e) {
            throw new EJBException("cant find datasource");
        }
        try {
            conn = ds.getConnection();
            insertEntry(conn);
            emptyTable(conn);
        } catch (Exception e) {
            //System.out.println("Exception caught : " + e.getMessage() );
            e.printStackTrace();
            passed = false;
        }
        return passed;
    }

    private void insertEntry(Connection conn) throws SQLException {
        Statement stmt = conn.createStatement();
        stmt.executeUpdate("INSERT into O_CUSTOMER" +
                "  values (1, 'abcd')");
        stmt.close();
    }

    private void emptyTable(Connection conn) throws SQLException {
        Statement stmt = conn.createStatement();
        stmt.executeUpdate("DELETE FROM O_CUSTOMER");
        stmt.close();
    }

    public void ejbLoad() {
    }

    public void ejbStore() {
    }

    public void ejbRemove() {
    }

    public void ejbActivate() {
    }

    public void ejbPassivate() {
    }

    public void unsetEntityContext() {
    }

    public void ejbPostCreate() {
    }
}
