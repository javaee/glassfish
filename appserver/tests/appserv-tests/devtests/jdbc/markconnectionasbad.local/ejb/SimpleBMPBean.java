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
