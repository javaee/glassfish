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

package com.sun.s1asdev.jdbc.multiplecloseconnection.ejb;

import javax.ejb.CreateException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;

public class SimpleSessionBean implements SessionBean {

    private SessionContext ctxt_;
    private InitialContext ic_;
    private DataSource ds;

    public void setSessionContext(SessionContext context) {
        ctxt_ = context;
        try {
            ic_ = new InitialContext();
            ds = (DataSource) ic_.lookup("java:comp/env/DataSource");
        } catch (NamingException ne) {
            ne.printStackTrace();
        }
    }

    public void ejbCreate() throws CreateException {
    }

    public boolean test1() throws Exception {
        Connection conn1 = null;
        boolean passed = true;
        //clean the database
        try {
            conn1 = ds.getConnection();
        } catch (Exception e) {
            e.printStackTrace();
            passed = false;
        } finally {
            try {
                if (conn1 != null) {
                    conn1.close();
                    conn1.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
                passed = false;
            }
        }
        return passed;
    }

    public boolean test2() throws Exception {
        Connection conn1 = null;
        Statement stmt = null;
        boolean passed = true;
        //clean the database
        try {
            conn1 = ds.getConnection();
            stmt = conn1.createStatement();
            stmt.executeQuery("SELECT * FROM TXLEVELSWITCH");
        } catch (Exception e) {
            e.printStackTrace();
            passed = false;
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (Exception e) {
                    e.printStackTrace();
                    passed = false;
                }
            }
            try {
                if (conn1 != null) {
                    conn1.close();
                    conn1.createStatement();
                    // trying to create statement on a closed connection, must throw exception.
                }
            } catch (Exception e) {
                try {
                    conn1.close();
                } catch (Exception e1) {
                    e.printStackTrace();
                    //closing a connection multiple times is a no-op.
                    //If exception is thrown, its a failure.
                    passed = false;
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
