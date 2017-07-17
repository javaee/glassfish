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

package com.sun.s1asdev.ejb.ejb30.hello.session3;

import javax.ejb.Stateless;

import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;

import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;
import javax.persistence.EntityManager;
import javax.naming.InitialContext;
import javax.annotation.sql.*;
import javax.sql.DataSource;
import java.sql.Connection;


@DataSourceDefinitions(
        value = {
                //pure annotation that has url and no standard properties
                //test-case for issue-14918
		@DataSourceDefinition(name="java:app/jdbc/appds_driver",
       	 		className="org.apache.derby.jdbc.ClientDriver",
        		user="dbuser",
        		password="dbpassword",
        		transactional=false,
        		url="jdbc:derby://localhost:1527/testdb;create=true"
		),

                @DataSourceDefinition(name = "java:global/env/HelloEJB_DataSource",
                        className = "org.apache.derby.jdbc.EmbeddedXADataSource",
                        minPoolSize = 0,
                        initialPoolSize = 0,
                        user = "APP",
                        password = "APP",
                        databaseName = "hello-ejb-global",
                        properties = {"connectionAttributes=;create=true"}

                ),

                @DataSourceDefinition(name = "java:comp/env/HelloEJB_DataSource",
                        minPoolSize = 0,
                        initialPoolSize = 0,
                        className = "org.apache.derby.jdbc.EmbeddedXADataSource",
                        user = "APP",
                        password = "APP",
                        databaseName = "hello-ejb-comp",
                        properties = {"connectionAttributes=;create=true"}
                ),
                @DataSourceDefinition(name = "java:module/env/HelloEJB_DataSource",
                        minPoolSize = 0,
                        initialPoolSize = 0,
                        className = "org.apache.derby.jdbc.EmbeddedXADataSource",
                        user = "APP",
                        password = "APP",
                        databaseName = "hello-ejb-module",
                        properties = {"connectionAttributes=;create=true"}
                ),
                // annotation + DD where URL specified in annotation wins
                //test-case for issue-14918
		@DataSourceDefinition(name="java:module/env/HelloEJB_DataSource_driver",
        		className="org.apache.derby.jdbc.ClientDriver",
        		user="dbuser",
        		password="dbpassword",
        		transactional=false,
        		url="jdbc:derby://localhost:1527/testdb;create=true"
		)
        }
)

@Stateless
public class HelloEJB implements Hello {

    public void hello() {

        boolean appDSDriver = lookupDataSource("java:app/jdbc/appds_driver", true);
        boolean moduleDSDriver= lookupDataSource("java:module/env/HelloEJB_DataSource_driver", true);

        boolean global = lookupDataSource("java:global/env/HelloEJB_DataSource", true);
        boolean comp = lookupDataSource("java:comp/env/HelloEJB_DataSource", true);
        boolean moduleHelloEjb = lookupDataSource("java:module/env/HelloEJB_DataSource", true);

        boolean globalHelloStatefulEJB = lookupDataSource("java:global/env/HelloStatefulEJB_DataSource", true);
        boolean compHelloStatefulEJB = lookupDataSource("java:comp/env/HelloStatefulEJB_DataSource", false);
        boolean appHelloStatefulEjb = lookupDataSource("java:app/env/HelloStatefulEJB_DataSource", true);

        boolean globalServlet = lookupDataSource("java:global/env/Servlet_DataSource", true);
        boolean compServlet = lookupDataSource("java:comp/env/Servlet_DataSource", false);
        boolean appServletDataSource = lookupDataSource("java:app/env/Servlet_DataSource", true);
        boolean moduleServletDataSource = lookupDataSource("java:module/env/Servlet_DataSource", false);

        boolean globalServlet_DD_DataSource = lookupDataSource("java:global/env/Servlet_DD_DataSource", true);
        boolean compServlet_DD_DataSource = lookupDataSource("java:comp/env/Servlet_DD_DataSource", false);

        boolean globalHelloStateful_DD_DataSource = lookupDataSource("java:global/env/HelloStatefulEJB_DD_DataSource", true);
        boolean compHelloStateful_DD_DataSource = lookupDataSource("java:comp/env/HelloStatefulEJB_DD_DataSource", false);

        boolean globalHello_DD_DataSource = lookupDataSource("java:global/env/HelloEJB_DD_DataSource", true);
        boolean compHello_DD_DataSource = lookupDataSource("java:comp/env/HelloEJB_DD_DataSource", false);


        if (appDSDriver && moduleDSDriver && global && comp && globalHelloStatefulEJB && !compHelloStatefulEJB && globalServlet
                && !compServlet && appServletDataSource && globalServlet_DD_DataSource && !compServlet_DD_DataSource
                && globalHelloStateful_DD_DataSource && !compHelloStateful_DD_DataSource &&
                globalHello_DD_DataSource && compHello_DD_DataSource && appHelloStatefulEjb &&
                moduleHelloEjb && !moduleServletDataSource) {
            System.out.println("HelloEJB successful datasource definitions lookup");
        } else {
            System.out.println("HelloEJB datasource definitions lookup failure");
            throw new RuntimeException("HelloEJB failure");
        }


        System.out.println("In HelloEJB::hello()");
    }

    private boolean lookupDataSource(String dataSourceName, boolean expectSuccess) {
        Connection c = null;
        try {
            InitialContext ic = new InitialContext();
            System.out.println("lookup dataSource : " + dataSourceName);
            DataSource ds = (DataSource) ic.lookup(dataSourceName);
            c = ds.getConnection();
            System.out.println("got connection : " + c);
            return true;
        } catch (Exception e) {
            if(expectSuccess){
                e.printStackTrace();
            }
            return false;
        } finally {
            try {
                if (c != null) {
                    c.close();
                }
            } catch (Exception e) {
            }
        }
    }
}
