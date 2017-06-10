package com.sun.s1asdev.jdbc.markconnectionasbad.local.ejb;

import javax.ejb.*;
import javax.naming.*;
import java.sql.*;

public class SimpleBMPBean
        implements EntityBean {

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
        return 1;
    }


    /**
     * Read Operation - Single(Local) DataSource - Shareable
     *
     * @return boolean
     */
    public String test1() {
        Connection physicalConnection = null;
        for (int i = 0; i < 5; i++) {
            Connection conn = null;
            try {
                conn = ds.getConnection();
                physicalConnection = ds.getConnection(conn);
                System.out.println("test-1 : " + physicalConnection);

            } catch (Exception e) {
                physicalConnection = null;
                return null;
            } finally {
                if (conn != null) {
                    try {
                        //System.out.println("Closing Connection : " + conn);
                        ds.markConnectionAsBad(conn);
                        conn.close();
                    } catch (Exception e1) {
                    }
                }
            }
        }

        return physicalConnection.toString();
    }

    /**
     * Write Operation - Single (Local) DataSource  Shareable
     *
     * @return boolean
     */
    public String test2() {
        boolean passed = true;
        Connection physicalConnection = null;
        for (int i = 0; i < 5; i++) {
            Connection conn = null;
            try {
                conn = ds.getConnection();
                Statement stmt = conn.createStatement();
                stmt.executeUpdate("insert into o_customer values (" + i + ",'a')");
                physicalConnection = ds.getConnection(conn);
                System.out.println("test-2 : " + physicalConnection);

            } catch (Exception e) {
                physicalConnection = null;
                return null;
            } finally {
                if (conn != null) {
                    try {
                        //System.out.println("Closing Connection : " + conn);
                        ds.markConnectionAsBad(conn);
                        conn.close();
                    } catch (Exception e1) {
                    }
                }
            }
        }

        return physicalConnection.toString();
    }

    /**
     * Read Operation - Single(Local) (No TX) DataSource - Shareable
     *
     * @return boolean
     */
    public boolean test3() {
        boolean passed = true;
        Connection previousConnection = null;
        Connection physicalConnection = null;
        for (int i = 0; i < 5; i++) {
            Connection conn = null;

            try {
                conn = ds.getConnection();
                physicalConnection = ds.getConnection(conn);
                if (previousConnection == physicalConnection) {
                    System.out.println("Previous & Current Connection are same");
                    passed = false;
                    break;
                }
                previousConnection = physicalConnection;
            } catch (Exception e) {
                passed = false;
                break;
            } finally {
                if (conn != null) {
                    try {
                        //System.out.println("Closing Connection : " + conn);
                        ds.markConnectionAsBad(conn);
                        conn.close();
                    } catch (Exception e1) {
                    }
                }
            }
        }

        return passed;
    }

    /**
     * Write Operation - Single (Local) ( No Tx) DataSource  Shareable
     *
     * @return boolean
     */
    public boolean test4() {
        boolean passed = true;
        Connection conn = null;
        Connection previousConnection = null;
        Connection physicalConnection = null;
        for (int i = 0; i < 5; i++) {
            try {
                conn = ds.getConnection();
                physicalConnection = ds.getConnection(conn);
                Statement stmt = conn.createStatement();
                stmt.executeUpdate("insert into o_customer values (" + i + ",'a')");
                if (previousConnection == physicalConnection) {
                    System.out.println("Previous & Current Connection are same");
                    passed = false;
                    break;
                }
                previousConnection = physicalConnection;

            } catch (Exception e) {
                passed = false;
                break;
            } finally {
                if (conn != null) {
                    try {
                        //System.out.println("Closing Connection : " + conn);
                        ds.markConnectionAsBad(conn);
                        conn.close();
                    } catch (Exception e1) {
                    }
                }
            }
        }

        return passed;
    }

    /**
     * Write Operation - Single (Local) DataSource  UnShareable
     *
     * @return boolean
     */
    public boolean test5(int numOfConnections, boolean expectSuccess) {
        boolean passed = true;
        Connection conns[] = new Connection[numOfConnections];
           com.sun.appserv.jdbc.DataSource ds1 = null ;
        try{
           ds1 = (com.sun.appserv.jdbc.DataSource)(new InitialContext()).lookup("java:comp/env/UnshareableDataSource");
        }catch(Exception e){
          e.printStackTrace();
        }
        try {
        for (int i = 0; i < numOfConnections; i++) {
                conns[i] = ds1.getConnection();
                Statement stmt = conns[i].createStatement();
                stmt.executeUpdate("insert into o_customer values (" + i + ",'a')");
        }
	} catch (Exception e) {
                if(expectSuccess){
                    passed = false;
                }
                e.printStackTrace();
            } finally {
        for (int i = 0; i < conns.length; i++) {
                if (conns[i] != null) {
                    try {
                        //System.out.println("Closing Connection : " + conn);
                        conns[i].close();
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
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
