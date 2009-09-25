package com.sun.s1asdev.connector.installed_libraries_test.ejb;


import javax.ejb.EJBContext;
import javax.ejb.SessionBean;
import javax.naming.InitialContext;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class SimpleSessionBean implements SessionBean {

    private EJBContext ejbcontext;
    private transient javax.ejb.SessionContext m_ctx = null;
    transient javax.sql.DataSource ds;


    public void setSessionContext(javax.ejb.SessionContext ctx) {
        m_ctx = ctx;
    }

    public void ejbCreate() {
    }

    public void ejbRemove() {
    }

    public void ejbActivate() {
    }

    public void ejbPassivate() {
    }

    public boolean test1() {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        Connection conn2 = null;
        Statement stmt2 = null;
        ResultSet rs2 = null;
        try {

            InitialContext ctx = new InitialContext();
            ds = (javax.sql.DataSource) ctx.lookup("java:comp/env/DataSource1");
            conn = ds.getConnection();
            stmt = conn.createStatement();
            String query1 = "SELECT * FROM TXLEVELSWITCH";
            rs = stmt.executeQuery(query1);

            conn2 = ds.getConnection();
            stmt2 = conn2.createStatement();
            rs2 = stmt2.executeQuery("SELECT * FROM TXLEVELSWITCH");

            return true;
        } catch (Exception e) {
            System.out.println("Caught Exception---");
            e.printStackTrace();
            return false;
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (Exception e1) {
                }
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (Exception e1) {
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (Exception e1) {
                }
            }

            if (rs2 != null) {
                try {
                    rs2.close();
                } catch (Exception e1) {
                }
            }
            if (stmt2 != null) {
                try {
                    stmt2.close();
                } catch (Exception e1) {
                }
            }
            if (conn2 != null) {
                try {
                    conn2.close();
                } catch (Exception e1) {
                }
            }

        }
    }


}



