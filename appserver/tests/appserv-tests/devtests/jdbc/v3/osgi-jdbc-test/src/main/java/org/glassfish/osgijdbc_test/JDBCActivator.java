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

package org.glassfish.osgijdbc_test;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.logging.Level;
import java.util.logging.Logger;


public class JDBCActivator implements BundleActivator {

    private static final Logger logger = Logger.getLogger(JDBCActivator.class.getPackage().getName());

    private BundleContext bundleContext;

    public void start(BundleContext bundleContext) throws Exception {
        this.bundleContext = bundleContext;
        test();
        debug("Bundle activated");
    }

    private void test(){

        testJdbcResources(null);
        
        testJdbcResources("(osgi.jdbc.driver.class=oracle.jdbc.pool.OracleDataSource)");
        testJdbcResources("(osgi.jdbc.driver.class=org.apache.derby.jdbc.EmbeddedXADataSource)");
        testJdbcResources("(osgi.jdbc.driver.class=org.apache.derby.jdbc.ClientDataSource)");

        testJdbcResources("(osgi.jdbc.driver.class=org.apache.derby.jdbc.ClientDataSource40)");
        testJdbcResources("(osgi.jdbc.driver.class=org.apache.derby.jdbc.EmbeddedXADataSource40)");

        testJdbcResources("(jndi-name=jdbc/oracle_type4_resource)");
        testJdbcResources("(jndi-name=jdbc/__TimerPool)");
        testJdbcResources("(jndi-name=jdbc/__default)");

        testJdbcResources("(&(jndi-name=jdbc/oracle_type4_resource)(osgi.jdbc.driver.class=oracle.jdbc.pool.OracleDataSource))");
        testJdbcResources("(&(jndi-name=jdbc/__TimerPool)(osgi.jdbc.driver.class=org.apache.derby.jdbc.EmbeddedXADataSource))");
        testJdbcResources("(&(jndi-name=jdbc/__default)(osgi.jdbc.driver.class=org.apache.derby.jdbc.ClientDataSource))");

        testJdbcResources("(&(jndi-name=jdbc/oracle_type4_resource)(osgi.jdbc.driver.class=org.apache.derby.jdbc.ClientDataSource))");

        
    }

    private void testJdbcResources(String filter) {
        debug("---------------------------[ "+filter+" ]---------------------------------");
        try {
            ServiceReference[] refs = bundleContext.getAllServiceReferences(javax.sql.DataSource.class.getName(), filter);
            if (refs != null) {
                for (ServiceReference ref : refs) {
                    DataSource ds = (DataSource) ref.getBundle().getBundleContext().getService(ref);
                    try {
                        Connection con = ds.getConnection();
                        debug("got connection [" + con + "] for resource [" + ref.getProperty("jndi-name") + "]");
                        con.close();
                    } catch (Exception e) {
                        System.err.println(e);
                    }
                }
            } else {
                debug("testJdbcResources, none found");
            }
        } catch (Exception e) {
            System.err.println(e);
        }
        debug("-----------------------------------------------------------------------");
    }


    public void stop(BundleContext bundleContext) throws Exception {
        debug("Bundle de-activated");
    }

    private void debug(String s) {
        if (logger.isLoggable(Level.INFO)) {
            logger.info("[osgi-jdbc-tester] : " + s);
        }
    }
}
