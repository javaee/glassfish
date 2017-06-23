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

package org.glassfish.jdbc.devtests.v3.test;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;
import org.glassfish.jdbc.devtests.v3.util.HtmlUtil;

/**
 * Tests if Connection pool tries to evict (expel) some free connections
 * if a new connection cannot fit in the pool. Assumes max-pool-size = 32, 
 * steady-pool-size = 8, pool-resize-quantity = 2, match-connections = true.

 * @author shalini
 */
public class MultipleUserCredentialsTest implements SimpleTest {

    Map<String, Boolean> resultsMap = new HashMap<String, Boolean>();
    
    public Map<String, Boolean> runTest(DataSource ds, PrintWriter out) {
        try {
            if (testMultipleUserCredentials(ds, out)) {
                resultsMap.put("jdbc-multiple-user-credentials", true);
            }else{
                resultsMap.put("jdbc-multiple-user-credentials", false);
            }
        } catch (Exception e) {
            resultsMap.put("jdbc-multiple-user-credentials", false);
        }

        HtmlUtil.printHR(out);        
        return resultsMap;                        
    }

    /**
     * Tests if unmatched free connections are purged and new connections are 
     * provided even if the user credentials don't match.
     * @param ds
     * @param out
     * @return boolean result
     */
    private boolean testMultipleUserCredentials(DataSource ds, PrintWriter out) {
        Connection conns1[] = new Connection[16];
        Connection conns2[] = new Connection[16];
        boolean passed = true;
        out.println("<h4> Multiple User Credentials Test</h4>");
        
        //First user acquires 16 connections
        out.println("<br> Getting 16 connections as DBUSER...");
        for(int i=0; i<conns1.length; i++) {
            try {
                conns1[i] = ds.getConnection("DBUSER", "DBPASSWORD");
            } catch (SQLException ex) {
                HtmlUtil.printException(ex, out);
                passed = false;
            }
        }

        //Second user acquires 16 connections
        out.println("<br> Getting 16 connections as dbuser");
        for(int i=0; i<conns2.length; i++) {
            try {
                conns2[i] = ds.getConnection("dbuser", "dbpassword");
            } catch (SQLException ex) {
                HtmlUtil.printException(ex, out);
                passed = false;
            }
        }
        
        //All connections are returned to the pool
        out.println("<br> Closing all the connections acquired by DBUSER");
        for(int i=0; i<conns1.length; i++) {
            try {
                conns1[i].close();
            } catch(Exception ex) {
                HtmlUtil.printException(ex, out);
                passed = false;
            }
        }

        out.println("<br> Closing all the connections acquired by dbuser");
        for(int i=0; i<conns2.length; i++) {
            try {
                conns2[i].close();
            } catch(Exception ex) {
                HtmlUtil.printException(ex, out);
                passed = false;
            }
        }

        //Request from third and fourth user 
        out.println("<br> Getting subsequent connections for APP and DERBYUSER");
        try {
            Connection conn3 = ds.getConnection("APP", "APP");
            conn3.close();
            Connection conn4 = ds.getConnection("DERBYUSER", "DERBYPASSWORD");
            conn4.close();
        } catch(Exception ex) {
            HtmlUtil.printException(ex, out);
            passed = false;
        }
        out.println("<br> Unmatched free connections evicted and new connections got");
        out.println("<br> Test result : " + passed);
        return passed;
    }
}
