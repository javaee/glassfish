/**
 * Copyright š 2002 Sun Microsystems, Inc. All rights reserved.
 */

package com.sun.s1peqe.transaction.txglobal.ejb.beanB;

import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.ejb.SessionSynchronization;
import javax.ejb.*;

import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.Connection;
import javax.sql.DataSource;
import javax.naming.Context;
import javax.naming.InitialContext;
import java.util.Enumeration;
import java.rmi.RemoteException;

public class TxBeanB implements SessionBean, SessionSynchronization {

    private String user = null;
    private String dbURL1 = null;
    private String dbURL2 = null;
    private String password = null;
    private SessionContext ctx = null;   
    private int commitStatus = 2; // 0 - commit, 1 - rollback, 2 - don't know

    // ------------------------------------------------------------------------
    // Container Required Methods
    // ------------------------------------------------------------------------
    public void setSessionContext(SessionContext sc) {
        System.out.println("setSessionContext in BeanB");  
        try {
            ctx = sc;
            Context ic = new InitialContext();
            user = (String) ic.lookup("java:comp/env/user");
            password = (String) ic.lookup("java:comp/env/password");
            dbURL1 = (String) ic.lookup("java:comp/env/dbURL1");
            dbURL2 = (String) ic.lookup("java:comp/env/dbURL2");
        } catch (Exception ex) {
            System.out.println("Exception in setSessionContext: " +
                               ex.getMessage());
            ex.printStackTrace();
        }
    }

    public void ejbCreate() {
        System.out.println("ejbCreate in BeanB");  
    } 

    public void ejbDestroy() {
        System.out.println("ejbDestroy in BeanB");  
    }

    public void ejbActivate() {
        System.out.println("ejbActivate in BeanB");  
    }
  
    public void ejbPassivate() {
        System.out.println("ejbPassivate in BeanB");  
    }

    public void ejbRemove() {
        System.out.println("ejbRemove in BeanB");  
    }

    // ------------------------------------------------------------------------
    // Business Logic Methods
    // ------------------------------------------------------------------------
    public void test1() throws RemoteException {
        Connection con1 = null;
        Connection con2 = null;
        Statement stmt1 = null;
        Statement stmt2 = null;
        ResultSet rs1 = null;
        ResultSet rs2 = null;
        System.out.println("test1 in BeanB");  
        try {
            con1 = getConnection(dbURL1);
            stmt1 = con1.createStatement();
            rs1 = stmt1.executeQuery("SELECT * FROM usecase3_table");
            System.out.println("Queried DB1 Successfully...");

            con2 = getConnection(dbURL2);
            stmt2 = con2.createStatement();
            rs2 = stmt2.executeQuery("SELECT * FROM usecase3_table");
            System.out.println("Queried DB2 Successfully...");
        } catch (Exception ex) {
            System.out.println("Exception in test1: " + ex.toString());
            ex.printStackTrace();
        } finally {
            try {
                if (rs1 != null)
                    rs1.close();
                if (stmt1 != null)
                    stmt1.close();
                if (con1 != null)
                    con1.close();

                if (rs2 != null)
                    rs2.close();
                if (stmt2 != null)
                    stmt2.close();
                if (con2 != null)
                    con2.close();

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public void test2() throws RemoteException {
        Connection con1 = null;
        Connection con2 = null;
        Statement stmt1 = null;
        Statement stmt2 = null;
        ResultSet rs1 = null;
        ResultSet rs2 = null;
        System.out.println("test2 in BeanB");  
        try {
            con1 = getConnection(dbURL1);
            stmt1 = con1.createStatement();
            rs1 = stmt1.executeQuery("SELECT * FROM usecase3_table");
            System.out.println("Queried DB1 Successfully...");

            con2 = getConnection(dbURL2);
            stmt2 = con2.createStatement();
            rs2 = stmt2.executeQuery("SELECT * FROM usecase3_table");
            System.out.println("Queried DB2 Successfully...");
            // throw new EJBException("Throwing an EJB exception to force rollback ");
            ctx.setRollbackOnly();
        } catch (Exception ex) {
            System.out.println("Exception in test2: " + ex.toString());
            ex.printStackTrace();
        } finally {
            try {
                if (rs1 != null)
                    rs1.close();
                if (stmt1 != null)
                    stmt1.close();
                if (con1 != null)
                    con1.close();

                if (rs2 != null)
                    rs2.close();
                if (stmt2 != null)
                    stmt2.close();
                if (con2 != null)
                    con2.close();


            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public int getCommitStatus() {
        return commitStatus;
    }

    public void beforeCompletion() {
        System.out.println("in beforeCompletion");
    }

    public void afterBegin() {
        System.out.println("in afterBegin");
    }

    public void afterCompletion(boolean committed) {
        System.out.println("in afterCompletion. committed = " + committed);
        if (committed)
            commitStatus = 0;
        else
            commitStatus = 1;
    }



    private Connection getConnection(String dbURL) {
        Connection con = null;
        System.out.println("getConnection in BeanB");  
        try{
            Context context = new InitialContext();
            DataSource ds = (DataSource) context.lookup(dbURL);
            // con = ds.getConnection(user, password);
            con = ds.getConnection();
            System.out.println("Got DB Connection Successfully...");
        } catch (Exception ex) {
            System.out.println("Exception in getConnection: " + ex.toString());
	    ex.printStackTrace();
        }
        return con;
    }
}
