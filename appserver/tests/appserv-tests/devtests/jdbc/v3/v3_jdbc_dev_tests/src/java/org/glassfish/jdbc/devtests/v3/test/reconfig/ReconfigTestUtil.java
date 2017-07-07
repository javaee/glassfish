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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.glassfish.jdbc.devtests.v3.test.reconfig;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;
import javax.transaction.SystemException;
import org.glassfish.jdbc.devtests.v3.util.HtmlUtil;

/**
 *
 * @author shalini
 */
public class ReconfigTestUtil {
    Map<String, Boolean> resultsMap = new HashMap<String, Boolean>();

    Map<String, Boolean> poolPropertyChangeTest(DataSource ds, PrintWriter out, 
            boolean throwException) {
        //Tests the property change of jdbc connection pool by asadmin set command.
        try {
            if (testPropertyChange(ds, out, throwException)) {
                resultsMap.put("pool-property-change-test", true);
            }else{
                resultsMap.put("pool-property-change-test", false);
            }
        } catch (Exception e) {
            resultsMap.put("pool-property-change-test", false);
        } 
        return resultsMap;
    }

    Map<String, Boolean> resourceAttributeChangeTest(DataSource ds, PrintWriter out, 
            boolean throwException) {
        //Tests the attribute set of jdbc resource by asadmin set command.
        try {
            if (testJDBCResourceChange(ds, out, throwException)) {
                resultsMap.put("resource-change-wrong-table-test", true);
            }else{
                resultsMap.put("resource-change-wrong-table-test", false);
            }
        } catch (Exception e) {
            resultsMap.put("resource-change-wrong-table-test", false);
        } 
        
        try {
            if(testJDBCResourceChangeCorrectTable(ds, out, !throwException)) {
                resultsMap.put("resource-change-correct-table-test", true);
            } else {
                resultsMap.put("resource-change-correct-table-test", false);
            }
        } catch (Exception ex) {
            resultsMap.put("resource-change-correct-table-test", false);
        }
        return resultsMap;
    }
    
    Map<String, Boolean> poolAttributeChangeTest(DataSource ds, PrintWriter out, 
            int maxPoolSize, boolean throwException) {
        //Tests the attribute set by asadmin set command.
        try {
            if (testMaxPoolSize(ds, out, maxPoolSize, throwException)) {
                resultsMap.put("existing-pool-attribute-max-pool-size", true);
            }else{
                resultsMap.put("existing-pool-attribute-max-pool-size", false);
            }
        } catch (Exception e) {
            resultsMap.put("existing-pool-attribute-max-pool-size", false);
        }
        return resultsMap;
    }

    private boolean testJDBCResourceChange(DataSource ds, PrintWriter out, 
            boolean throwException) throws SystemException {
        HtmlUtil.printHR(out);
        out.println("<h4> Reconfig test : tablename : reconfigTestTable (reconfig-db)</h4>");
        boolean passed = true;

        Connection con = null;
        Statement stmt = null;
        ResultSet rs = null;
        out.println("Getting connection ....");
        try {
            con = ds.getConnection();
            //get data from a table from the database jdbc-dev-test-db (for jdbc-dev-test-pool)
            //exception will be thrown since the pool name has been changed to DerbyPool
            stmt = con.createStatement();
            rs = stmt.executeQuery("select * from reconfigTestTable");
        } catch (Exception ex) {
            out.println("Caught Exception ...");
            HtmlUtil.printException(ex, out);
            if ( throwException ) {
                passed = true;
            } else {
                passed = false;
            }
        } finally {
            try { con.close(); } catch ( Exception e ) {}
        }
        
        HtmlUtil.printHR(out);
        return passed;
    }

    private boolean testJDBCResourceChangeCorrectTable(DataSource ds, PrintWriter out, 
            boolean throwException) throws SystemException {
        HtmlUtil.printHR(out);
        out.println("<h4> Reconfig test : tableName : sampleTable (sample-db) </h4>");
        boolean passed = true;

        Connection con = null;
        Statement stmt = null;
        ResultSet rs = null;
        out.println("Getting connection ....");
        try {
            con = ds.getConnection();
            //get data from a table from the database sun-appserv-samples (for DerbyPool)
            //no exception will be thrown
            stmt = con.createStatement();
            rs = stmt.executeQuery("select * from sampleTable");
        } catch (Exception ex) {
            out.println("Caught Exception ...");
            HtmlUtil.printException(ex, out);
            if ( throwException ) {
                passed = true;
            } else {
                passed = false;
            }
        } finally {
            try { con.close(); } catch ( Exception e ) {}
        }
        
        HtmlUtil.printHR(out);
        return passed;        
    }
    
    public boolean testMaxPoolSize(DataSource ds, PrintWriter out, 
            int maxPoolSize, boolean throwException)  
            throws SystemException {
        HtmlUtil.printHR(out);
        out.println("\n<h4> Reconfig - Attribute max-pool-size test </h4>");

        boolean passed = true;
        Connection[] conns = new Connection[maxPoolSize];
        for( int i = 0; i < maxPoolSize; i++ ) {
            out.println("\nthrowException is : " + throwException );
            try {
                out.println("\nGetting connection : " + i );
                conns[i] = ds.getConnection();
                out.println("Connection Got : " + conns[i]);
            } catch (Exception e) {
                out.println("\nCaught exception (First try)");
                HtmlUtil.printException(e, out);
                e.printStackTrace();
                return false;
            }
        }

        //try getting an extra connection
        out.println("\nTry getting extra connection");
        Connection con = null;
        try {
            con = ds.getConnection();
            out.println("Got Connection : " + con);
        } catch( Exception e) {
            out.print("\nCaught exception" ) ;
            if ( throwException ) {
                passed = true;
            } else {
                passed = false;
            }
        } finally {
            try { con.close(); } catch ( Exception e ) {}
            for (int i = 0 ; i < maxPoolSize;i++ ) {
                try {
                    conns[i].close();
                } catch( Exception e) {
                }
            }
        }
        HtmlUtil.printHR(out);
        return passed;
    }

    private boolean testPropertyChange(DataSource ds, PrintWriter out, 
            boolean throwException) throws SystemException {
        HtmlUtil.printHR(out);
        out.println("\n<h4> Reconfig - Pool property change test </h4>");

        boolean passed = true;
        Connection con = null;
        out.println("\nthrowException is : " + throwException );
        try {
            out.println("\nGetting connection... ");
            con = ds.getConnection();
            out.println("Connection Got : " + con);
        } catch (Exception e) {
            out.println("\nCaught exception !!!");
            if ( throwException ) {
                passed = true;
            } else {
                passed = false;
            }
        } finally {
            try { con.close(); } catch ( Exception e ) {}            
        }
        HtmlUtil.printHR(out);
        return passed;        
    }
}
