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
 * Tests Application Authentication
 * 
 * @author shalini
 */
public class ApplicationAuthTest implements SimpleTest {

    Map<String, Boolean> resultsMap = new HashMap<String, Boolean>();

    /**
     * Tests the JDBC Application Authentication
     * @param ds
     * @param out
     * @return
     */
    public Map<String, Boolean> runTest(DataSource ds, PrintWriter out) {
        try {
            if (testNoCredentials(ds, out)) {
                resultsMap.put("app-auth-no-credentials", true);
            }else{
                resultsMap.put("app-auth-no-credentials", false);
            }
        } catch (Exception e) {
            resultsMap.put("app-auth-no-credentials", false);
        }

        try {
            if (testWithCredentials(ds, out)) {
                resultsMap.put("app-auth-with-credentials", true);
            }else{
                resultsMap.put("app-auth-with-credentials", false);
            }
        } catch (Exception e) {
            resultsMap.put("app-auth-with-credentials", false);
        }

        try {
            if (testWrongCredentials(ds, out)) {
                resultsMap.put("app-auth-wrong-credentials", true);
            }else{
                resultsMap.put("app-auth-wrong-credentials", false);
            }
        } catch (Exception e) {
            resultsMap.put("app-auth-wrong-credentials", false);
        }
        
        try {
            if (testTwiceWithCredentials(ds, out)) {
                resultsMap.put("app-auth-twice-with-credentials", true);
            }else{
                resultsMap.put("app-auth-twice-with-credentials", false);
            }
        } catch (Exception e) {
            resultsMap.put("app-auth-twice-with-credentials", false);
        }

        try {
            if (compareConnections(ds, out)) {
                resultsMap.put("app-auth-compare-connections", true);
            }else{
                resultsMap.put("app-auth-compare-connections", false);
            }
        } catch (Exception e) {
            resultsMap.put("app-auth-compare-connections", false);
        }

        HtmlUtil.printHR(out);
        return resultsMap;                
    }

    /**
     * Tests application authentication by ensuring that connection with 
     * the same username is returned when got from 2 different credentials - 
     * first one being the right one.
     * @param ds
     * @param out
     * @return boolean result
     */
    private boolean compareConnections(DataSource ds, PrintWriter out) {
        boolean result = false;
        Connection conn1 = null;
        Connection conn2 = null;
        boolean passed1 = false;
        boolean passed2 = false;
        out.println("<h4> Compare Connections test </h4>");
        try {
            try {
                out.println("<br> Getting the NonTx Connection for \"DBUSER\" ...");
                conn1 = ((com.sun.appserv.jdbc.DataSource)ds).getNonTxConnection(
                        "DBUSER", "DBPASSWORD");
                out.println("<br> Testing Connection's userName...");
                if (conn1.getMetaData().getUserName().equals("DBUSER")) {
                    out.println("<br> UserName matches the right credentials");
                    passed1 = true;
                }
            } catch (Exception ex) {
                HtmlUtil.printException(ex, out);
                return result;
            }
            
            try {
                out.println("<br> Getting the NonTx Connection for \"APP\" ...");
                conn2 = ((com.sun.appserv.jdbc.DataSource)ds).getNonTxConnection(
                        "APP", "APP");
                out.println("<br> Testing Connection's userName...");
                if (conn2.getMetaData().getUserName().equals("APP")) {
                    out.println("<br> UserName matches the right credentials");
                    passed2 = true;
                }                
            } catch (Exception ex2) {
                HtmlUtil.printException(ex2, out);
            }
        } finally {
            if (conn1 != null) {
                try {
                    conn1.close();
                } catch (Exception e1) {
                    HtmlUtil.printException(e1, out);
                }
            }

            if (conn2 != null) {
                try {
                    conn2.close();
                } catch (Exception e1) {
                    HtmlUtil.printException(e1, out);
                }
            }            
        }
        result = passed1 && passed2;
        out.println("<br> Test result : " + result);
        return result;        
    }

    /**
     * Tests application authentication when username and password are not
     * specified.
     * @param ds
     * @param out
     * @return boolean result
     */
    private boolean testNoCredentials(DataSource ds, PrintWriter out) {
        boolean result = false;
        Connection conn = null;
        out.println("<h4> App Auth Test with no credentials </h4>");
        try {
            out.println("<br> Getting a Connection without any credentials");
            conn = ds.getConnection();
        } catch (Exception ex) {
            out.println("<br> in the Exception block since this should not pass");
            result = true;
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (Exception e1) {
                    HtmlUtil.printException(e1, out);
                }
            }
        }
        out.println("<br> Test result : " + result);
        return result;
    }

    /**
     * Tests the application authentication when right credentials are specified
     * initially and wrong credentials the second try.
     * @param ds
     * @param out
     * @return boolean result
     */
    private boolean testTwiceWithCredentials(DataSource ds, PrintWriter out) {
        boolean result = false;
        Connection conn = null;
        out.println("<h4> App Auth Test with 2 sets of credentials </h4>");
        try {
            out.println("<br> Getting a Connection with DBUSER (right credential)");
            conn = ds.getConnection("DBUSER", "DBPASSWORD");
        } catch (Exception ex) {
            HtmlUtil.printException(ex, out);
            return result;
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (Exception e1) {
                    HtmlUtil.printException(e1, out);
                }
            }
        }

        try {
            out.println("<br> Getting a connection with xyz (wrong credential)");
            conn = ds.getConnection("xyz", "xyz");
        } catch (Exception ex) {
            out.println("<br> Failed - Expected Result");
            result = true;
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (Exception e1) {
                    HtmlUtil.printException(e1, out);
                }
            }
        }
        out.println("<br> Test result : " + result);
        return result;
    }

    /**
     * Tests application authentication when right username/password are 
     * specified.
     * @param ds
     * @param out
     * @return boolean result
     */
    private boolean testWithCredentials(DataSource ds, PrintWriter out) {
        boolean result = true;
        Connection conn = null;
        out.println("<h4> App Auth Test with credentials </h4>");
        try {
            out.println("<br> Getting a Connection with DBUSER");
            conn = ds.getConnection("DBUSER", "DBPASSWORD");
        } catch (Exception ex) {
            HtmlUtil.printException(ex, out);
            result = false;
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (Exception e1) {
                    HtmlUtil.printException(e1, out);
                }
            }
        }
        out.println("<br> Test result : " + result);
        return result;
    }

    /**
     * Tests application authentication when wrong username/password are
     * specified.
     * @param ds
     * @param out
     * @return boolean result
     */
    private boolean testWrongCredentials(DataSource ds, PrintWriter out) {
        boolean result = false;
        Connection conn = null;
        out.println("<h4> App Auth Test with wrong credentials </h4>");
        try {
            out.println("<br> Getting a Connection with wrong credentials");
            conn = ds.getConnection("xyz", "xyz");
        } catch (Exception ex) {
            out.println("<br> in the Exception block - expected result");
            result = true;
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (Exception e1) {
                    HtmlUtil.printException(e1, out);
                }
            }
        }
        out.println("<br> Test result : " + result);
        return result;
    }

}
