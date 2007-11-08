package com.sun.s1asdev.jdbc.multiplecloseconnection.ejb;

import javax.ejb.CreateException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;

public class SimpleSessionBean implements SessionBean {

    private SessionContext ctxt_;
    private InitialContext ic_;
    private DataSource ds;

    public void setSessionContext(SessionContext context) {
        ctxt_ = context;
        try {
            ic_ = new InitialContext();
            ds = (DataSource) ic_.lookup("java:comp/env/DataSource");
        } catch (NamingException ne) {
            ne.printStackTrace();
        }
    }

    public void ejbCreate() throws CreateException {
    }

    public boolean test1() throws Exception {
        Connection conn1 = null;
        boolean passed = true;
        //clean the database
        try {
            conn1 = ds.getConnection();
        } catch (Exception e) {
            e.printStackTrace();
            passed = false;
        } finally {
            try {
                if (conn1 != null) {
                    conn1.close();
                    conn1.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
                passed = false;
            }
        }
        return passed;
    }

    public boolean test2() throws Exception {
        Connection conn1 = null;
        Statement stmt = null;
        boolean passed = true;
        //clean the database
        try {
            conn1 = ds.getConnection();
            stmt = conn1.createStatement();
            stmt.executeQuery("SELECT * FROM TXLEVELSWITCH");
        } catch (Exception e) {
            e.printStackTrace();
            passed = false;
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (Exception e) {
                    e.printStackTrace();
                    passed = false;
                }
            }
            try {
                if (conn1 != null) {
                    conn1.close();
                    conn1.createStatement();
                    // trying to create statement on a closed connection, must throw exception.
                }
            } catch (Exception e) {
                try {
                    conn1.close();
                } catch (Exception e1) {
                    e.printStackTrace();
                    //closing a connection multiple times is a no-op.
                    //If exception is thrown, its a failure.
                    passed = false;
                }
            }
        }
        return passed;
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
