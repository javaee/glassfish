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

package com.sun.s1asdev.jdbc.markconnectionasbad.xa.ejb;

import javax.ejb.*;
import javax.naming.*;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.util.Set;
import java.util.HashSet;


public class SimpleBMPBean
        implements EntityBean {

    protected com.sun.appserv.jdbc.DataSource ds;
    protected com.sun.appserv.jdbc.DataSource ds1;
    protected com.sun.appserv.jdbc.DataSource localds;


    public void setEntityContext(EntityContext entityContext) {
        Context context = null;
        try {
            context = new InitialContext();
            ds = (com.sun.appserv.jdbc.DataSource) context.lookup("java:comp/env/UnShareableDataSource");
            ds1 = (com.sun.appserv.jdbc.DataSource) context.lookup("java:comp/env/ShareableDataSource");
            localds = (com.sun.appserv.jdbc.DataSource) context.lookup("java:comp/env/LocalDataSource");
        } catch (NamingException e) {
            throw new EJBException("cant find datasource");
        }
    }

    public Integer ejbCreate() throws CreateException {
        return new Integer(1);
    }


    public Set<Integer> getFromLocalDS(int count) {
        int connHashCode = 0;	
        Connection conn = null;
	Set<Integer> hashCodeSet = new HashSet();
        for (int i = 0; i < count; i++) {
	    try {
		conn = localds.getNonTxConnection();
		connHashCode = (localds.getConnection(conn)).hashCode();
                hashCodeSet.add(connHashCode);
            } catch (Exception e) {

            } finally {
                if (conn != null) {
                    try {
                        conn.close();
                    } catch (Exception e1) {
                    }
                }
            }
	}
	return hashCodeSet;
    }

    /* Read Operation - Driver  - shareable */
    public boolean test0() {

        boolean passed = true;
        for (int i = 0; i < 5; i++) {
            Connection conn = null;
            try {
                conn = localds.getConnection();
            } catch (Exception e) {
                passed = false;
            } finally {
                if (conn != null) {
                    try {
                        System.out.println("Marking conn in Test-1: " + localds.getConnection(conn));
                        localds.markConnectionAsBad(conn);
                        conn.close();
                    } catch (Exception e1) {
                    }
                }
            }
        }

        return passed;
    }

    /* Read Operation - Driver  - NoTx -  Unshareable */
    public boolean test1() {

        boolean passed = true;
        for (int i = 0; i < 5; i++) {
            Connection conn = null;
            try {
                conn = ds.getConnection();
                //System.out.println(ds.getConnection(conn));

            } catch (Exception e) {
                passed = false;
            } finally {
                if (conn != null) {
                    try {
                        System.out.println("Marking conn in Test-1: " + conn);
                        ds.markConnectionAsBad(conn);
                        conn.close();
                    } catch (Exception e1) {
                    }
                }
            }
        }

        return passed;
    }

    /* Write Operation - XA  DataSource  - NoTx -  Unshareable */
    public boolean test2() {
        boolean passed = true;
        for (int i = 0; i < 5; i++) {
            Connection conn = null;
            try {
                conn = ds.getConnection();
                //System.out.println(ds.getConnection(conn));

                Statement stmt = conn.createStatement();
                stmt.executeUpdate("insert into o_customer values (" + i + ",'a')");

            } catch (Exception e) {
                passed = false;
            } finally {
                if (conn != null) {
                    try {
                        System.out.println("Marking conn in Test-2: " + conn);
                        ds.markConnectionAsBad(conn);
                        conn.close();
                    } catch (Exception e1) {
                    }
                }
            }
        }

        return passed;
    }


    /* Read Operation - XA  DataSource  - NoTx -  Shareable */
    public boolean test3() {
        boolean passed = true;
        for (int i = 0; i < 5; i++) {
            Connection conn = null;
            try {
                conn = ds1.getConnection();
                //System.out.println(ds1.getConnection(conn));

            } catch (Exception e) {
                passed = false;
            } finally {
                if (conn != null) {
                    try {
                        System.out.println("Marking conn in Test-3: " + conn);
                        ds1.markConnectionAsBad(conn);
                        conn.close();
                    } catch (Exception e1) {
                    }
                }
            }
        }

        return passed;
    }

    /* Write Operation - XA  DataSource  - NoTx -  Shareable */
    public boolean test4() {
        boolean passed = true;
        for (int i = 0; i < 5; i++) {
            Connection conn = null;
            try {
                conn = ds1.getConnection();
                //System.out.println(ds1.getConnection(conn));
                Statement stmt = conn.createStatement();
                stmt.executeUpdate("insert into o_customer values (" + i + ",'a')");

            } catch (Exception e) {
                passed = false;
            } finally {
                if (conn != null) {
                    try {
                        System.out.println("Marking conn in Test-4: " + conn);
                        ds1.markConnectionAsBad(conn);
                        conn.close();
                    } catch (Exception e1) {
                    }
                }
            }
        }

        return passed;
    }


    /* Read Operation - XA  DataSource  - Tx -  Unshareable */
    public boolean test5() {
        boolean passed = true;
        for (int i = 0; i < 5; i++) {
            Connection conn = null;
            try {
                conn = ds.getConnection();
                //System.out.println(ds.getConnection(conn));

            } catch (Exception e) {
                passed = false;
            } finally {
                if (conn != null) {
                    try {
                        System.out.println("Marking conn in Test-5: " + conn);
                        ds.markConnectionAsBad(conn);
                        conn.close();
                    } catch (Exception e1) {
                    }
                }
            }
        }

        return passed;
    }

    /* Write Operation - XA  DataSource  - Tx -  Unshareable */
    public boolean test6() {
        boolean passed = true;
        for (int i = 0; i < 5; i++) {
            Connection conn = null;
            try {
                conn = ds.getConnection();
                //System.out.println(ds.getConnection(conn));
                Statement stmt = conn.createStatement();
                stmt.executeUpdate("insert into o_customer values (" + i + ",'a')");

            } catch (Exception e) {
                passed = false;
            } finally {
                if (conn != null) {
                    try {
                        System.out.println("Marking conn in Test-6: " + conn);
                        ds.markConnectionAsBad(conn);
                        conn.close();
                    } catch (Exception e1) {
                    }
                }
            }
        }

        return passed;
    }


    /* Read Operation - XA  DataSource  - Tx -  Shareable */
    public boolean test7() {
        boolean passed = true;
        for (int i = 0; i < 5; i++) {
            Connection conn = null;
            try {
                conn = ds1.getConnection();
                //System.out.println(ds1.getConnection(conn));

            } catch (Exception e) {
                passed = false;
            } finally {
                if (conn != null) {
                    try {
                        System.out.println("Marking conn in Test-7: " + conn);
                        ds1.markConnectionAsBad(conn);
                        conn.close();
                    } catch (Exception e1) {
                    }
                }
            }
        }

        return passed;
    }

    /* Write Operation - XA  DataSource  - Tx -  Shareable */
    public boolean test8() {
        boolean passed = true;
        for (int i = 0; i < 5; i++) {
            Connection conn = null;
            try {
                conn = ds1.getConnection();
                //System.out.println(ds1.getConnection(conn));
                Statement stmt = conn.createStatement();
                stmt.executeUpdate("insert into o_customer values (" + i + ",'a')");

            } catch (Exception e) {
                passed = false;
            } finally {
                if (conn != null) {
                    try {
                        System.out.println("Marking conn in Test-8: " + conn);
                        ds1.markConnectionAsBad(conn);
                        conn.close();
                    } catch (Exception e1) {
                    }
                }
            }
        }

        return passed;
    }

    /**
     * LAO Write/ Write
     */
    public boolean test9() {
        boolean passed = true;
        Connection conn = null;
        Connection conn1 = null;
        try {
            conn = localds.getConnection();
            Statement stmt = conn.createStatement();
            stmt.executeUpdate("insert into owner values (9,'localds')");

            conn1 = ds1.getConnection();
	    System.out.println("Conn1 got " + conn1);
            Statement stmt1 = conn1.createStatement();
            stmt1.executeUpdate("insert into o_customer values (9,'xads')");

        } catch (Exception e) {
            passed = false;
            e.printStackTrace();
        } finally {
            if (conn != null) {
                try {
                    localds.markConnectionAsBad(conn);
                    conn.close();
                } catch (Exception e1) {
                }
            }
            if (conn1 != null) {
                try {
                    ds1.markConnectionAsBad(conn1);
                    conn1.close();
		    System.out.println("Conn1 closed "+ conn1);
                } catch (Exception e1) {
                }
            }
        }
        return passed;
    }

    /**
     * LAO - Local Read, XA Write
     */

    public boolean test10() {
        boolean passed = true;
        Connection conn = null;
        Connection conn1 = null;
        try {
            conn = localds.getConnection();
            Statement stmt = conn.createStatement();
            stmt.executeQuery("select * from owner");

            conn1 = ds1.getConnection();
            Statement stmt1 = conn1.createStatement();
            stmt1.executeUpdate("insert into o_customer values (10,'xads')");

        } catch (Exception e) {
            passed = false;
            e.printStackTrace();
        } finally {
            if (conn != null) {
                try {
                    localds.markConnectionAsBad(conn);
                    conn.close();
                } catch (Exception e1) {
                }
            }
            if (conn1 != null) {
                try {
                    ds1.markConnectionAsBad(conn1);
                    conn1.close();
                } catch (Exception e1) {
                }
            }
        }
        return passed;
    }


    /**
     * LAO - Local Read, XA Read
     */

    public boolean test11() {
        boolean passed = true;
        Connection conn = null;
        Connection conn1 = null;
        try {
            conn = localds.getConnection();
            Statement stmt = conn.createStatement();
            stmt.executeQuery("select * from owner");

            conn1 = ds1.getConnection();
            Statement stmt1 = conn1.createStatement();
            stmt1.executeQuery("select * from o_customer");

        } catch (Exception e) {
            passed = false;
            e.printStackTrace();
        } finally {
            if (conn != null) {
                try {
                    localds.markConnectionAsBad(conn);
                    conn.close();
                } catch (Exception e1) {
                }
            }
            if (conn1 != null) {
                try {
                    ds1.markConnectionAsBad(conn1);
                    conn1.close();
                } catch (Exception e1) {
                }
            }
        }
        return passed;
    }


    /**
     * LAO - Local Write, XA Read
     */

    public boolean test12() {
        boolean passed = true;
        Connection conn = null;
        Connection conn1 = null;
        try {
            conn = localds.getConnection();
            Statement stmt = conn.createStatement();
            stmt.executeUpdate("insert into owner values (12,'localds')");

            conn1 = ds1.getConnection();
            Statement stmt1 = conn1.createStatement();
            stmt1.executeQuery("select * from o_customer");


        } catch (Exception e) {
            passed = false;
            e.printStackTrace();
        } finally {
            if (conn != null) {
                try {
                    localds.markConnectionAsBad(conn);
                    conn.close();
                } catch (Exception e1) {
                }
            }
            if (conn1 != null) {
                try {
                    ds1.markConnectionAsBad(conn1);
                    conn1.close();
                } catch (Exception e1) {
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
