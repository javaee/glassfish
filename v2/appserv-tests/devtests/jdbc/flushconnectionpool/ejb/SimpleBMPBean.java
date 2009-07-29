package com.sun.s1asdev.jdbc.flushconnectionpool.ejb;

import javax.ejb.*;
import javax.naming.*;
import java.sql.*;

public class SimpleBMPBean implements EntityBean {

    protected com.sun.appserv.jdbc.DataSource ds;

    public void setEntityContext(EntityContext entityContext) {
        Context context = null;
        try {
            context = new InitialContext();
            ds = (com.sun.appserv.jdbc.DataSource) context.lookup("java:comp/env/DataSource");
        } catch (NamingException e) {
            throw new EJBException("cant find datasource");
        }
    }

    public Integer ejbCreate() throws CreateException {
        return new Integer(1);
    }

    /**
     * Acquire 4 connections, closing them at the end of the loop.
     * Cache the first connection, and before acquiring the last connection, 
     * do a flush connection pool. The 5th connection got after the 
     * flush should be different from the first connection got.
     *
     * @return boolean
     */
    public boolean test1() {
        Connection firstConnection = null;
        Connection lastConnection = null;
        com.sun.appserv.jdbc.DataSource ds = null;

        ds = this.ds;

        boolean passed = false;
        for (int i = 0; i < 5; i++) {
            Connection conn = null;
            try {
		//Sleep, do a flush and then get a connection.
		if(i ==4) {
			System.out.println("******** Sleeping...");
		    Thread.sleep(120000);
		}	    
                conn = ds.getConnection();
                System.out.println("********i=" + i + "conn=" + ds.getConnection(conn));

                if (i == 0) {
                    firstConnection = ds.getConnection(conn);
                } else if (i == 4) {
                    lastConnection = ds.getConnection(conn);
                }
                passed = (firstConnection != lastConnection);

            } catch (Exception e) {
                e.printStackTrace();
                passed = false;
            } finally {
                if (conn != null) {
                    try {
                        conn.close();
                    } catch (Exception e1) {
                    }
                }
            }
        }
        return passed;
    }


    /**
     * Acquire connection dont release it, call flush, close connection. 
     * Get connection once more. Same connection object should be got.
     * If so test Passes.
     */
    public boolean test2() {
        Connection firstConnection = null;
        Connection lastConnection = null;
        com.sun.appserv.jdbc.DataSource ds = null;

        ds = this.ds;

        boolean passed = false;
	Connection con = null;
	try {
            con = ds.getConnection();
	    firstConnection = ds.getConnection(con);
	    System.out.println("******* first : " + firstConnection);

	    //Sleeping to doa flush
	    Thread.sleep(120000);

	    con.close();
	    
	    //Now get another connection
	    con = ds.getConnection();
	    lastConnection = ds.getConnection(con);
	    System.out.println("******* last : " + lastConnection);
	} catch(Exception ex) {
	    passed = false;
	} finally {
	    if(con != null) {
		try {
		    con.close();
		} catch(Exception ex) {}
	    }
	}
	passed = firstConnection == lastConnection;
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
