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

package com.sun.s1asdev.jdbc.statementwrapper.ejb;

import javax.ejb.*;

import com.sun.appserv.jdbc.DataSource;

import javax.naming.*;
import javax.sql.*;
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
        System.out.println("[**SimpleBMPBean**] Done with setEntityContext....");
    }

    public Integer ejbCreate() throws CreateException {
        return new Integer(1);
    }

    private int getRandomNumber(int maxLimit) {
        //return (int)(Math.random() *5 );
        return maxLimit;
    }

    public boolean test1(int maxConnections) {
        boolean result = false;
        Connection connections[] = new Connection[getRandomNumber(maxConnections)];
        try {
            for (int i = 0; i < connections.length; i++) {
                connections[i] = ds.getConnection();
            }
        } catch (Exception e) {
            result = false;
        } finally {
            for (int i = 0; i < connections.length; i++) {
                try {
                    connections[i].close();
                } catch (Exception e) {
                }
                result = true;
            }
        }
        return result;
    }

    public boolean test2() {
        Connection conn = null;
        try {
            conn = ds.getConnection();
            System.out.println("test-2() : " + ds.getConnection(conn));
            Context ic = new InitialContext();
            NestedBMPHome home = (NestedBMPHome) javax.rmi.PortableRemoteObject.narrow(ic.lookup("java:comp/env/ejb/NestedBMPEJB"), NestedBMPHome.class);
            NestedBMP nestedBean = home.create();

            nestedBean.test1();

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (Exception e) {
            }
        }
        return true;
    }

    public void test3() {
        Connection conn = null;
        try {
            conn = ds.getConnection();
            System.out.println("test-3() : " + ds.getConnection(conn));
        } catch (Exception e) {
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (Exception e) {
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
