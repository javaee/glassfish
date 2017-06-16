package com.sun.s1asdev.jdbc.markconnectionasbad.xa.ejb;


import javax.ejb.*;
import javax.naming.*;
import java.sql.Connection;
import java.sql.Statement;


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


    /* Read Operation - XA  DataSource  - NoTx -  Unshareable */
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
