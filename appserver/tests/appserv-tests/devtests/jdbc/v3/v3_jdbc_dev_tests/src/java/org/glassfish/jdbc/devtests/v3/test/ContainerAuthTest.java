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
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;
import org.glassfish.jdbc.devtests.v3.util.HtmlUtil;

/**
 *
 * @author shalini
 */
public class ContainerAuthTest implements SimpleTest {

    Map<String, Boolean> resultsMap = new HashMap<String, Boolean>();
    
    public Map<String, Boolean> runTest(DataSource ds, PrintWriter out) {
        try {
            if (testContAuthUserPass(ds, out)) {
                resultsMap.put("jdbc-cont-auth-test1", true);
            }else{
                resultsMap.put("jdbc-cont-auth-test1", false);
            }
        } catch (Exception e) {
            resultsMap.put("jdbc-cont-auth-test1", false);
        }
        try {
            if (testContAuthNoUserPass(ds, out)) {
                resultsMap.put("jdbc-cont-auth-test2", true);
            }else{
                resultsMap.put("jdbc-cont-auth-test2", false);
            }
        } catch (Exception e) {
            resultsMap.put("jdbc-cont-auth-test2", false);
        }

        HtmlUtil.printHR(out);
        return resultsMap;                
    }

    /**
     * Tests Container Authentication when no username/password are specified.
     * @param ds
     * @param out
     * @return boolean result
     */
    private boolean testContAuthNoUserPass(DataSource ds, PrintWriter out) {
        Connection con = null;
        boolean passed = true;
        out.println("<h4>Container Auth Test - No username/password</h4>");
        try {
            out.print("<br> Getting a connection ...");
            con = ds.getConnection();
        } catch(Exception ex) {
            HtmlUtil.printException(ex, out);
            passed = false;
        } finally {
            if(con != null) {
                try {
                    con.close();
                } catch(Exception ex) {
                    HtmlUtil.printException(ex, out);
                }
            }
            out.println("<br> Test result : " + passed);
            return passed;
        }
    }

    /**
     * Tests Container Authentication when username/password are specified
     * while getting a connection.
     * @param ds
     * @param out
     * @return boolean result
     */
    private boolean testContAuthUserPass(DataSource ds, PrintWriter out) {
        Connection con = null;
        boolean passed = true;
        out.println("<h4> Container Authentication Test - username/password specified </h4>");
        try {
            out.println("<br> Getting a connection with username/password");
            con = ds.getConnection("DBUSER", "DBPASSWORD");
        } catch(Exception ex) {
            HtmlUtil.printException(ex, out);
            passed = false;
        } finally {
            if(con != null) {
                try {
                    con.close();
                } catch(Exception ex) {
                    HtmlUtil.printException(ex, out);
                }
            }
            out.println("<br> Test result : " + passed);
            return passed;
        }
    }
}
