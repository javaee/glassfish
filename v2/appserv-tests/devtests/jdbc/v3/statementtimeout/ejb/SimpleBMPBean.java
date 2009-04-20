package com.sun.s1asdev.jdbc.statementtimeout.ejb;

import javax.ejb.*;
import javax.naming.*;
import javax.sql.*;
import java.sql.*;


public class SimpleBMPBean
        implements EntityBean {

    protected DataSource ds;

    public void setEntityContext(EntityContext entityContext) {
        Context context = null;
        try {
            context = new InitialContext();
            ds = (DataSource) context.lookup("java:comp/env/DataSource");
        } catch (NamingException e) {
            throw new EJBException("cant find datasource");
        }
        System.out.println("[**SimpleBMPBean**] Done with setEntityContext....");
    }

    public Integer ejbCreate() throws CreateException {
        return new Integer(1);
    }

    public boolean statementTest() {
        boolean result = false;
        Connection conFromDS = null;
        Statement stmt = null;
        try {
            conFromDS = ds.getConnection();
            stmt = conFromDS.createStatement();

            if (stmt.getQueryTimeout() == 30) {
                result = true;
            }

        } catch (SQLException sqe) {
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException sqe) {
            }

            try {
                if (conFromDS != null) {
                    conFromDS.close();
                }
            } catch (SQLException sqe) {
            }
        }
        return result;
    }

    public boolean preparedStatementTest() {
        boolean result = false;
        Connection conFromDS = null;
        PreparedStatement stmt = null;
        try {
            conFromDS = ds.getConnection();
            stmt = conFromDS.prepareStatement("select * from customer_stmt_wrapper");

            if (stmt.getQueryTimeout() == 30) {
                result = true;
            }

        } catch (SQLException sqe) {
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException sqe) {
            }

            try {
                if (conFromDS != null) {
                    conFromDS.close();
                }
            } catch (SQLException sqe) {
            }
        }
        return result;
    }

    public boolean callableStatementTest() {
        boolean result = false;
        Connection conFromDS = null;
        CallableStatement stmt = null;
        try {
            conFromDS = ds.getConnection();
            stmt = conFromDS.prepareCall("select * from customer_stmt_wrapper");

            if (stmt.getQueryTimeout() == 30) {
                result = true;
            }

        } catch (SQLException sqe) {
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException sqe) {
            }

            try {
                if (conFromDS != null) {
                    conFromDS.close();
                }
            } catch (SQLException sqe) {
            }
        }
        return result;
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
