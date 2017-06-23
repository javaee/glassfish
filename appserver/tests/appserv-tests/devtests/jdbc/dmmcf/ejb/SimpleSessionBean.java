/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package com.sun.s1asdev.jdbc.dmmcf.ejb;

import javax.naming.*;
import javax.sql.*;
import javax.ejb.*;
import java.sql.*;
import java.util.Set;
import java.util.HashSet;
import javax.transaction.UserTransaction;

public class SimpleSessionBean implements SessionBean {

    private SessionContext ctxt_;
    private InitialContext ic_;

    public void setSessionContext(SessionContext context) {
        ctxt_ = context;
        try {
            ic_ = new InitialContext();
        } catch (NamingException ne) {
            ne.printStackTrace();
        }
    }

    public void ejbCreate() throws CreateException {
    }

    /**
     * Get connection and do some database inserts. Then call another
     * EJB's method in the same transaction and change the inserted value.
     * Since all this is in the same tx, the other bean's method should
     * get the same connection (physical) and hence be able to see the
     * inserted value even though the tx has not committed yet.
     * The idea is to test connection sharing
     */
    public boolean test1() throws Exception {
        DataSource ds = (DataSource) ic_.lookup("java:comp/env/DataSource");
        Connection conn1 = null;
        Statement stmt1 = null;
        ResultSet rs1 = null;
        boolean passed = false;

        try {
            conn1 = ds.getConnection();
            stmt1 = conn1.createStatement();
            stmt1.executeUpdate("INSERT INTO CONNSHARING values (100, 'CONN_SHARING')");

            Object o = ic_.lookup("java:comp/env/ejb/SimpleSession2EJB");
            SimpleSession2Home home = (SimpleSession2Home)
                    javax.rmi.PortableRemoteObject.narrow(o, SimpleSession2Home.class);
            SimpleSession2 bean = home.create();

            return bean.test1();

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            if (stmt1 != null) {
                try {
                    stmt1.close();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
            if (conn1 != null) {
                try {
                    conn1.close();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    /**
     * Get connection and do some database inserts. Then call another
     * EJB's method in the same transaction and change the inserted value.
     * Since all this is in the same tx, the other bean's method should
     * get the same connection (physical) and hence be able to see the
     * inserted value even though the tx has not committed yet.
     * This test does the same thing as test1 except that it closes the
     * connection it obtains and then opens a new connection in bean2's method
     * The idea is to test connection sharing
     */
    public boolean test2() throws Exception {
        DataSource ds = (DataSource) ic_.lookup("java:comp/env/DataSource");
        Connection conn1 = null;
        Statement stmt1 = null;
        ResultSet rs1 = null;
        boolean passed = false;

        try {
            conn1 = ds.getConnection();
            stmt1 = conn1.createStatement();
            stmt1.executeUpdate("INSERT INTO CONNSHARING values (100, 'CONN_SHARING')");

            stmt1.close();
            conn1.close();

            Object o = ic_.lookup("java:comp/env/ejb/SimpleSession2EJB");
            SimpleSession2Home home = (SimpleSession2Home)
                    javax.rmi.PortableRemoteObject.narrow(o, SimpleSession2Home.class);
            SimpleSession2 bean = home.create();

            return bean.test1();

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            if (stmt1 != null) {
                try {
                    stmt1.close();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
            if (conn1 != null) {
                try {
                    conn1.close();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    public boolean test3() throws Exception {
        DataSource ds = (DataSource) ic_.lookup("java:comp/env/DataSource");
        Connection conn1 = null;
        Connection conn2 = null;
        Statement stmt1 = null;
        Statement stmt2 = null;
        ResultSet rs1 = null;
        boolean passed = false;

        try {
            conn1 = ds.getConnection();
            stmt1 = conn1.createStatement();
            stmt1.executeUpdate("INSERT INTO CONNSHARING values (100, 'CONN_SHARING')");

            conn2 = ds.getConnection();
            stmt2 = conn2.createStatement();
            stmt2.executeUpdate("INSERT INTO CONNSHARING values (200, 'CONN_SHARING_200')");

            Object o = ic_.lookup("java:comp/env/ejb/SimpleSession2EJB");
            SimpleSession2Home home = (SimpleSession2Home)
                    javax.rmi.PortableRemoteObject.narrow(o, SimpleSession2Home.class);
            SimpleSession2 bean = home.create();

            return bean.test2();

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            if (stmt1 != null) {
                try {
                    stmt1.close();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
            if (conn1 != null) {
                try {
                    conn1.close();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
            if (stmt2 != null) {
                try {
                    stmt2.close();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
            if (conn2 != null) {
                try {
                    conn2.close();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    public boolean test4() throws Exception {
        DataSource ds = (DataSource) ic_.lookup("java:comp/env/DataSource");
        Connection conn1 = null;
        Connection conn2 = null;
        Statement stmt1 = null;
        Statement stmt2 = null;
        boolean passed = false;

        try {
            conn1 = ds.getConnection();
            stmt1 = conn1.createStatement();
            stmt1.executeUpdate("INSERT INTO CONNSHARING values (100, 'CONN_SHARING')");

            conn2 = ds.getConnection();
            stmt2 = conn2.createStatement();
            stmt2.executeUpdate("INSERT INTO CONNSHARING values (200, 'CONN_SHARING_200')");

            stmt2.close();
            conn2.close();

            Object o = ic_.lookup("java:comp/env/ejb/SimpleSession2EJB");
            SimpleSession2Home home = (SimpleSession2Home)
                    javax.rmi.PortableRemoteObject.narrow(o, SimpleSession2Home.class);
            SimpleSession2 bean = home.create();

            return bean.test2();

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            if (stmt1 != null) {
                try {
                    stmt1.close();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
            if (conn1 != null) {
                try {
                    conn1.close();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        }
    }


    public boolean test5() throws Exception {
        boolean result;
        com.sun.appserv.jdbc.DataSource ds =
                (com.sun.appserv.jdbc.DataSource) ic_.lookup("java:comp/env/DataSource");
        Connection con1 = ds.getConnection();
        Connection physicalConn1 = ds.getConnection(con1);

        Connection con2 = ds.getConnection();
        Connection physicalConn2 = ds.getConnection(con2);
        Statement stmt = con2.createStatement();
        stmt.executeQuery("select * from connsharing");
        stmt.close();
        con2.close();

        Connection con3 = ds.getConnection();
        Connection physicalConn3 = ds.getConnection(con3);

        Connection con4 = ds.getConnection();
        Connection physicalConn4 = ds.getConnection(con4);

        con4.close();
        con1.close();
        con3.close();

        System.out.println("Conn 1 : " + physicalConn1);
        System.out.println("Conn 2 : " + physicalConn2);
        System.out.println("Conn 3 : " + physicalConn3);
        System.out.println("Conn 4 : " + physicalConn4);

        Set<Connection> physicalConnections = new HashSet<Connection>();
        physicalConnections.add(physicalConn1);
        physicalConnections.add(physicalConn2);
        physicalConnections.add(physicalConn3);
        physicalConnections.add(physicalConn4);

        if (physicalConnections.size() == 1) {
            result = true;
        } else {
            result = false;
        }
        physicalConnections.clear();
        return result;
    }

    public boolean test6() throws Exception {
        DataSource ds = (DataSource) ic_.lookup("java:comp/env/DataSource");
        Connection conn1 = null;
        boolean passed = false;

        try {
            conn1 = ds.getConnection();
            Object o = ic_.lookup("java:comp/env/ejb/SimpleSession2EJB");
            SimpleSession2Home home = (SimpleSession2Home)
                    javax.rmi.PortableRemoteObject.narrow(o, SimpleSession2Home.class);
            SimpleSession2 bean = home.create();
            for (int i = 0; i < 100; i++) {
                bean.test3();
            }

            passed = true;
        } catch (SQLException e) {
            e.printStackTrace();
            passed = false;
        } finally {
            try {
                if (conn1 != null)
                    conn1.close();
            } catch (Exception e1) {
            }
        }
        return passed;
    }

    /**
     * Query the value modified in the second bean and ensure that it
     * is correct.
     */
    public boolean query() throws Exception {
        DataSource ds = (DataSource) ic_.lookup("java:comp/env/DataSource");
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            conn = ds.getConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery("SELECT * FROM CONNSHARING WHERE c_id=100");
            if (rs.next()) {
                String str = rs.getString(2);
                System.out.println(" str => " + str);
                if ("CONN_SHARING_BEAN_2".equals(str.trim())) {
                    return true;
                }
            }


            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (stmt != null) {
                try {
                    stmt.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            //cleanup table
            try {
                stmt = conn.createStatement();
                stmt.executeUpdate("DELETE FROM CONNSHARING WHERE c_id=100");
                stmt.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (conn != null) {
                try {
                    conn.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

    }

    /**
     * Query the value modified in the second bean and ensure that it
     * is correct.
     */
    public boolean query2() throws Exception {
        DataSource ds = (DataSource) ic_.lookup("java:comp/env/DataSource");
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        ResultSet rs1 = null;
        String str1 = null;
        String str2 = null;


        try {
            conn = ds.getConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery("SELECT * FROM CONNSHARING WHERE c_id=100");
            if (rs.next()) {
                str1 = rs.getString(2);
                System.out.println(" str1 => " + str1);
            }

            rs1 = stmt.executeQuery("SELECT * FROM CONNSHARING WHERE c_id=200");
            if (rs1.next()) {
                str2 = rs1.getString(2);
                System.out.println(" str2 => " + str2);
            }
            if ("CONN_SHARING".equals(str1.trim()) &&
                    "CONN_SHARING_BEAN_2_2".equals(str2.trim())) {
                return true;
            }

            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (Exception e) {
                }
            }

            if (rs1 != null) {
                try {
                    rs1.close();
                } catch (Exception e) {
                }
            }

            if (stmt != null) {
                try {
                    stmt.close();
                } catch (Exception e) {
                }
            }

            //cleanup table
            try {
                stmt = conn.createStatement();
                stmt.executeUpdate("DELETE FROM CONNSHARING WHERE c_id=100");
                stmt.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                stmt = conn.createStatement();
                stmt.executeUpdate("DELETE FROM CONNSHARING WHERE c_id=200");
                stmt.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (conn != null) {
                try {
                    conn.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

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
