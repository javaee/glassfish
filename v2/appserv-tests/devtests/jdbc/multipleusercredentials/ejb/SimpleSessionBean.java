package com.sun.s1asdev.jdbc.multipleusercredentials.ejb;

import javax.ejb.*;
import javax.naming.*;
import javax.sql.*;
import java.sql.*;

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
        Connection conns[] = new Connection[16];
        Connection conns2[] = new Connection[16];
        boolean passed = true;

        for (int i = 0; i < conns.length; i++) {
            conns[i] = ds.getConnection("derby", "derby");
        }

        for (int i = 0; i < conns2.length; i++) {
            conns2[i] = ds.getConnection("javadb", "javadb");
        }

        for (int i = 0; i < conns.length; i++) {
            try {
                conns[i].close();
            } catch (Exception e) {
                e.printStackTrace();
                passed = false;
            }
        }

        for (int i = 0; i < conns2.length; i++) {
            try {
                conns2[i].close();
            } catch (Exception e) {
                e.printStackTrace();
                passed = false;
            }
        }

        try {
            Connection con = ds.getConnection("xyz", "xyz");
            con.close();
            Connection con1 = ds.getConnection("xyz1", "xyz1");
            con1.close();
        } catch (Exception e) {
            e.printStackTrace();
            passed = false;
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
