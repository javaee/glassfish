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
