/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.acme;

import java.sql.*;
import javax.ejb.*;
import javax.sql.DataSource;
import javax.naming.InitialContext;

/**
 *
 * @author marina vatkina
 */

@Stateless
public class MyBean {

    private static final String DEF_RESOURCE = "jdbc/__default";
    private static final String XA_RESOURCE = "jdbc/xa";

    public int verifydefault() throws Exception {
        InitialContext initCtx = new InitialContext();
        DataSource ds = (DataSource) initCtx.lookup(DEF_RESOURCE);

        return verify(ds);
   }

    public int verifyxa() throws Exception {
        InitialContext initCtx = new InitialContext();
        DataSource ds = (DataSource) initCtx.lookup(XA_RESOURCE);

        return verify(ds);
   }

    public int verify(DataSource ds) throws Exception {
        String selectStatement = "select * from student";
        Connection c = ds.getConnection();
        PreparedStatement ps = c.prepareStatement(selectStatement);
        ResultSet rs = ps.executeQuery();
        int result = 0;
        while (rs.next()) {
            result++;
            System.out.println("Found: " + rs.getString(1) + " : " + rs.getString(2));
        }
        rs.close();
        ps.close();
        c.close();

        return result;
    }

    public boolean testone(int id) throws Exception {
        InitialContext initCtx = new InitialContext();
        DataSource ds = (DataSource) initCtx.lookup(DEF_RESOURCE);

        return test(id, ds, false);
    }

    public boolean testtwo(int id) throws Exception {
        InitialContext initCtx = new InitialContext();
        DataSource ds1 = (DataSource) initCtx.lookup(DEF_RESOURCE);
        DataSource ds2 = (DataSource) initCtx.lookup(XA_RESOURCE);

        System.err.println("Insert in DEF_RESOURCE");
        boolean res1 = test(id, ds1, true);
        System.err.println("Insert in XA_RESOURCE");
        boolean res2 = test(id, ds2, true);
        return res1 && res2;
    }

    private boolean test(int id, DataSource ds, boolean useFailureInducer) throws Exception {
        String insertStatement = "insert into student values ( ? , ? )";
        Connection c = ds.getConnection();
        PreparedStatement ps = c.prepareStatement(insertStatement);

        if (useFailureInducer) {
            com.sun.jts.utils.RecoveryHooks.FailureInducer.activateFailureInducer();
            com.sun.jts.utils.RecoveryHooks.FailureInducer.setWaitPoint(com.sun.jts.utils.RecoveryHooks.FailureInducer.PREPARED, 60);
        }

        for (int i = 0; i < 3; i++) {
            System.err.println("Call # " + (i + 1));
            ps.setString(1, "BAA" + id + i);
            ps.setString(2, "BBB" + id + i);
            ps.executeUpdate();

            if (!useFailureInducer) {
                try {
                    Thread.sleep(7000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        ps.close();
        c.close();
        System.err.println("Insert successfully");

        return true;
    }

}
