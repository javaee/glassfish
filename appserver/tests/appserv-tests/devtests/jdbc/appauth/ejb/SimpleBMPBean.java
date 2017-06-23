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

package com.sun.s1asdev.jdbc.appauth.ejb;

import javax.ejb.*;
import javax.naming.*;
import javax.sql.*;
import java.sql.*;

public class SimpleBMPBean
        implements EntityBean {

    protected DataSource ds;
    int id;

    public void setEntityContext(EntityContext entityContext) {
        Context context = null;
        try {
            context = new InitialContext();
            ds = (DataSource) context.lookup("java:comp/env/DataSource");
        } catch (NamingException e) {
            throw new EJBException("cant find datasource");
        }
        System.out.println("[**SimpleBMPBean**] Done with setEntityContext....");
    }

    public Integer ejbCreate() throws CreateException {
        return new Integer(1);
    }

    public boolean test1() {
        //application auth + user/pwd not specified - should fail
        Connection conn = null;
        boolean passed = false;
        try {
            conn = ds.getConnection();
        } catch (Exception e) {
            passed = true;
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (Exception e1) {
                }
            }
        }
        return passed;
    }

    public boolean test2() {
        //application auth + user/pwd  specified - should pass
        Connection conn = null;
        boolean passed = true;
        try {
            conn = ds.getConnection("DBUSER", "DBPASSWORD");
        } catch (Exception e) {
            passed = false;
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (Exception e1) {
                }
            }
        }
        return passed;
    }

    public boolean test3() {
        //application auth + wrong user/pwd 
        Connection conn = null;
        boolean passed = false;
        try {
            conn = ds.getConnection("xyz", "xyz");
        } catch (Exception e) {
            passed = true;
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (Exception e1) {
                }
            }
        }
        return passed;
    }

    public boolean test4() {
        //application auth + user/pwd  specified - right initiall
        //wrong the second time
        Connection conn = null;
        boolean passed = false;

        try {
            conn = ds.getConnection("DBUSER", "DBPASSWORD");
        } catch (Exception e) {
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (Exception e1) {
                }
            }
        }

        try {
            conn = ds.getConnection("xyz", "xyz");
        } catch (Exception e) {
            passed = true;
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (Exception e1) {
                }
            }
        }
        return passed;
    }

    public boolean test5() {
        //application auth + user/pwd  specified - right initial
        //ensure that connection with same username is returned in both the cases.
        
        Connection conn = null;
        Connection conn2 = null;
        boolean passed = false;
        boolean result1 = false;
        boolean result2 = false;
        try {
            try {
                conn = ((com.sun.appserv.jdbc.DataSource) ds).getNonTxConnection("DBUSER", "DBPASSWORD");
                if (conn.getMetaData().getUserName().equals("DBUSER")) {
                    result1 = true;
                }
            } catch (Exception e) {
                return false;
            }

            try {
                conn2 = ((com.sun.appserv.jdbc.DataSource) ds).getNonTxConnection("APP", "APP");
                if (conn2.getMetaData().getUserName().equals("APP")) {
                    result2 = true;
                }
            } catch (Exception e) {}
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (Exception e1) {}
            }

            if (conn2 != null) {
                try {
                    conn2.close();
                } catch (Exception e1) {}
            }
        }
        passed = result1 && result2;

        return passed;
    }

    public void ejbLoad() {}

    public void ejbStore() {}

    public void ejbRemove() {}

    public void ejbActivate() {}

    public void ejbPassivate() {}

    public void unsetEntityContext() {}

    public void ejbPostCreate() {}
}
