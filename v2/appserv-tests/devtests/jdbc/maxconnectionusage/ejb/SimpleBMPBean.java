package com.sun.s1asdev.jdbc.maxconnectionusage.ejb;

import javax.ejb.*;
import javax.naming.*;
import java.sql.*;

public class SimpleBMPBean implements EntityBean {

    protected com.sun.appserv.jdbc.DataSource ds;
    protected com.sun.appserv.jdbc.DataSource xads;

    public void setEntityContext(EntityContext entityContext) {
        Context context = null;
        try {
            context = new InitialContext();
            ds = (com.sun.appserv.jdbc.DataSource) context.lookup("java:comp/env/DataSource");
            xads = (com.sun.appserv.jdbc.DataSource) context.lookup("java:comp/env/XADataSource");
        } catch (NamingException e) {
            throw new EJBException("cant find datasource");
        }
    }

    public Integer ejbCreate() throws CreateException {
        return new Integer(1);
    }

    /**
     * Acquire 11 connections (assumption : maxconnectionusage property value is 10)<br>
     * With ConnectionSharing ON, Tx attribute "NotSupported" on this method, DataSource of type<br>
     * "javax.sql.DataSource" or "javax.sql.XADataSource" physical connections con-1 & con-11<br>
     * must be different. If they are different test is passed.<br>
     *
     * @return boolean
     */
    public boolean test1(boolean useXA) {
        Connection firstConnection = null;
        Connection lastConnection = null;
        com.sun.appserv.jdbc.DataSource ds = null;

        if (useXA) {
            ds = this.xads;
        } else {
            ds = this.ds;
        }

        boolean passed = false;
        for (int i = 0; i < 11; i++) {
            Connection conn = null;
            try {
                conn = ds.getConnection();
                System.out.println(ds.getConnection(conn));

                if (i == 0) {
                    firstConnection = ds.getConnection(conn);
                } else if (i == 10) {
                    lastConnection = ds.getConnection(conn);
                    //This is necessary for the last connection to
                    //make sure subsequent tests pass.
                    ds.markConnectionAsBad(conn);
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


    public String test2(boolean useXA, int value) {
        Connection physicalConnection = null;
        Connection conn = null;
        Statement stmt = null;
        String physicalConnectionString = null;
        com.sun.appserv.jdbc.DataSource ds = null;

        if (useXA) {
            ds = this.xads;
        } else {
            ds = this.ds;
        }

        try {
            conn = ds.getConnection();
            stmt = conn.createStatement();
            stmt.executeUpdate("insert into max_connection_usage values (" + value + ",'" + value + "')");
            physicalConnection = ds.getConnection(conn);
            physicalConnectionString = physicalConnection.toString();

        } catch (Exception e) {
            physicalConnection = null;
            return null;
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (Exception e) {
            }
            if (conn != null) {
                try {
                    //System.out.println("Closing Connection : " + conn);
                    conn.close();
                } catch (Exception e1) {
                }
            }
        }

        return physicalConnectionString;
    }

    public String test3(int count, boolean useXA, int value) {
        Connection physicalConnection = null;
        Connection conn = null;
        Statement stmt = null;
        ResultSet set = null;

        com.sun.appserv.jdbc.DataSource ds = null;

        if (useXA) {
            ds = this.xads;
        } else {
            ds = this.ds;
        }

        try {
            conn = ds.getConnection();
            stmt = conn.createStatement();
            set = stmt.executeQuery("select count(*) as COUNT from max_connection_usage where id=" + value);
            while (set.next()) {
                int resultCount = set.getInt("COUNT");
                //System.out.println("Expected count ["+count+"] & Actual count ["+ resultCount +"]" );
                if (count == resultCount) {
                    physicalConnection = ds.getConnection(conn);
                } else {
                    System.out.println("Expected count [" + count + "] does not match [" + resultCount + "]");
                    break;
                }
            }

        } catch (Exception e) {
            physicalConnection = null;
            return null;
        } finally {
            try {
                if (set != null) {
                    set.close();
                }
            } catch (Exception e) {
            }
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (Exception e) {
            }
            if (conn != null) {
                try {
                    //System.out.println("Closing Connection : " + conn);
                    conn.close();
                } catch (Exception e1) {
                }
            }
        }

        return physicalConnection.toString();
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
