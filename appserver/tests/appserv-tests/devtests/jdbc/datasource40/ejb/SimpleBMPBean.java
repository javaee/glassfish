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

package com.sun.s1asdev.jdbc.datasource40.ejb;

import javax.ejb.*;
import javax.naming.*;
import javax.sql.*;
import java.sql.*;
import java.lang.reflect.*;

import com.sun.enterprise.connectors.ConnectorRuntime;

public class SimpleBMPBean
        implements EntityBean {

    protected DataSource ds;
    protected DataSource cpds;
    protected DataSource xads;

    public void setEntityContext(EntityContext entityContext) {
        Context context = null;
        try {
            context = new InitialContext();
            ds = (DataSource) context.lookup("java:comp/env/DSDataSource");
        } catch (NamingException e) {
            throw new EJBException("cant find DS datasource");
        }


        try {
            cpds = (DataSource) context.lookup("java:comp/env/CPDataSource");
        } catch (NamingException e) {
            throw new EJBException("cant find CP datasource");
        }

        try {
            xads = (DataSource) context.lookup("java:comp/env/XADataSource");
        } catch (NamingException e) {
            throw new EJBException("cant find XA datasource");
        }
        System.out.println("[**SimpleBMPBean**] Done with setEntityContext....");
    }

    public Integer ejbCreate() throws CreateException {
        return new Integer(1);
    }

    /**
     * Verify whether the datasource obtained has "unwrap" method <br>
     *
     * @return
     */
    public boolean test1() {
        boolean passed = false;
        try {
            Class dataSourceClass = ds.getClass();
            Method[] methods = dataSourceClass.getMethods();
            for (int i = 0; i < methods.length; i++) {
                if (methods[i].getName().equalsIgnoreCase("unwrap")) {
                    passed = true;
                    System.out.println("Found unwrap() in DataSource class");
                    break;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            passed = false;
        }

        return passed;
    }

    /**
     * verify whether the unwrap() on connection's close actually closes appserver's wrapper connection<br>
     *
     * @return
     */
    public boolean test2() {
        Connection conn = null;
        boolean passed = false;
        try {
            conn = ds.getConnection();
            String physicalConnection1 = (((com.sun.appserv.jdbc.DataSource) ds).getConnection(conn)).toString();
            Connection proxyConnection = conn.unwrap(java.sql.Connection.class);
            System.out.println("Proxy Connection : AutoCommit  : " + proxyConnection.getAutoCommit());
            //closing the proxy connection must close "conn" and return it to pool.
            //since there is only one connection in pool, second getConnection will fail if proxyConnection's close
            //did not close the logical connection of pool.
            proxyConnection.close();


            conn = ds.getConnection();
            String physicalConnection2 = (((com.sun.appserv.jdbc.DataSource) ds).getConnection(conn)).toString();

            //Ensure that both the connections are the same.
 		System.out.println("PhysicalConn 1 : " + physicalConnection1);
                System.out.println("PhysicalConn 2 : " + physicalConnection1);

            if (physicalConnection2.equals(physicalConnection1)) {
                passed = true;
		System.out.println("Physical Connections are equal");
            }else{
		System.out.println("Physical Connections are not equal");
	    }
	    

        } catch (Exception e) {
            e.printStackTrace();
            passed = false;
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (Exception e) {
                }
            }
        }
        return passed;
    }

    /**
     * Check whether the datasource.unwrap() works fine<br>
     * For respective resource-type, appropriate driver datasource need to be returned<br>
     *
     * @return
     */
    public boolean test3() {
        Connection conn = null;
        boolean passed = false;
        boolean dstest = false;
        boolean cptest = false;
        boolean xatest = false;

        try {
            Object nativeds = ds.unwrap(javax.sql.DataSource.class);

            if (nativeds instanceof javax.sql.DataSource) {
                dstest = true;
            } else {
                System.out.println("DS did not return object of type javax.sql.DataSource");
                dstest = false;
            }

            Object nativecpds = cpds.unwrap(javax.sql.ConnectionPoolDataSource.class);
            if (nativecpds instanceof javax.sql.ConnectionPoolDataSource) {
                cptest = true;
            } else {
                System.out.println("CP DS did not return object of type javax.sql.ConnectionPoolDataSource");
                cptest = false;
            }
            Object nativexads = xads.unwrap(javax.sql.XADataSource.class);
            if (nativexads instanceof javax.sql.XADataSource) {
                xatest = true;
            } else {
                System.out.println("XA DS did not return object of type javax.sql.XADataSource");
                xatest = false;
            }

            if (dstest && cptest && xatest) {
                passed = true;
                System.out.println("DataSource unwrap() returns appropriate db-datasources");
            }else{
                System.out.println("DataSource unwrap() did not return appropriate db-datasources");
            }

                
        } catch (Exception e) {
            e.printStackTrace();
            passed = false;
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
