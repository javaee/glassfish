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

package org.glassfish.jdbc.devtests.v3;

import java.io.*;
import java.lang.reflect.Constructor;
import java.net.*;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.sql.DataSource;
import java.util.Map;
import javax.annotation.Resource;
import org.glassfish.jdbc.devtests.v3.test.SimpleTest;
import org.glassfish.jdbc.devtests.v3.util.HtmlUtil;

/**
 *
 * @author jagadish
 */
public class TestResultServlet extends HttpServlet {

    @Resource(name = "jdbc/jdbc-common-resource", mappedName = "jdbc/jdbc-common-resource")
    DataSource dsCommon;
    @Resource(name = "jdbc/jdbc-multiple-user-test-resource", mappedName = "jdbc/jdbc-multiple-user-test-resource")
    DataSource dsMultipleUserCred;
    @Resource(name = "jdbc/jdbc-app-auth-test-resource", mappedName = "jdbc/jdbc-app-auth-test-resource")
    DataSource dsAppAuth;
    @Resource(name = "jdbc/jdbc-stmt-timeout-test-resource", mappedName = "jdbc/jdbc-stmt-timeout-test-resource")
    DataSource dsStmtTimeout;
    @Resource(name = "jdbc/jdbc-max-conn-usage-test-resource", mappedName = "jdbc/jdbc-max-conn-usage-test-resource")
    DataSource dsMaxConnUsage;
    @Resource(name = "jdbc/jdbc-conn-leak-tracing-test-resource", mappedName = "jdbc/jdbc-conn-leak-tracing-test-resource")
    DataSource dsConnLeakTracing;
    @Resource(name = "jdbc/jdbc-associate-with-thread-test-resource", mappedName = "jdbc/jdbc-associate-with-thread-test-resource")
    DataSource dsAssocWithThread;
    @Resource(name = "jdbc/jdbc-lazy-assoc-resource", mappedName = "jdbc/jdbc-lazy-assoc-test-resource")
    DataSource dsLazyAssoc;
    @Resource(name = "jdbc/jdbc-simple-xa-test-resource-1", mappedName = "jdbc/jdbc-simple-xa-test-resource-1")
    DataSource dsXA;
    @Resource(name = "jdbc/jdbc-lazy-enlist-resource-1", mappedName = "jdbc/jdbc-lazy-enlist-resource-1")
    DataSource dsLazyEnlist;
    @Resource(name = "jdbc/double-resource-reference-resource-1", mappedName = "jdbc/double-resource-reference-resource-1")
    DataSource dsDoubleReference;

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        SimpleTest testMultipleUserCred = null;
        SimpleTest testAppAuth = null;
        SimpleTest testStmtTimeout = null;
        SimpleTest testMaxConnUsage = null;
        SimpleTest testConnLeakTracing = null;
        SimpleTest testAssocWithThread = null;
        SimpleTest testLazyAssoc = null;
        SimpleTest testXA = null;
        SimpleTest testLazyEnlist = null;
        SimpleTest testDoubleResourceReference = null;
        SimpleTest[] testsOther = null;
        out.println("<html>");
        out.println("<head>");
        out.println("<title>Servlet TestResultServlet</title>");
        out.println("</head>");
        out.println("<body>");
        out.println("<h1>Servlet TestResultServlet at " + request.getContextPath() + "</h1>");
        StringBuffer buf = new StringBuffer();

        //TODO should be configurable to run any individual test
        boolean lazyAssocAlone = false;
        String testName = (String) request.getParameter("testName");
        if (testName != null && testName.equalsIgnoreCase("lazy-assoc")) {
            lazyAssocAlone = true;
        }

        try {
            testMultipleUserCred = loadMultipleUserCredTest();
            testAppAuth = loadAppAuthTest();
            testStmtTimeout = loadStmtTimeoutTest();
            testMaxConnUsage = loadMaxConnUsageTest();
            testConnLeakTracing = loadConnLeakTracingTest();
            testAssocWithThread = loadAssocWithThreadTest();
            testXA = loadSimpleXATest();
            testsOther = initializeTests();
            testLazyAssoc = loadLazyAssocTest();
            testLazyEnlist = loadLazyEnlistTest();
            testDoubleResourceReference = loadDoubleResourceReferenceTest();
        } catch (Exception e) {
            HtmlUtil.printException(e, out);
        }

        try {
            buf.append("<table border=1><tr><th>Test Name</th><th> Pass </th></tr>");

            if (!lazyAssocAlone) {


                //Run Multiple User Credentials Test
                Map<String, Boolean> mapMultipleUserCred =
                        testMultipleUserCred.runTest(dsMultipleUserCred, out);
                for (Map.Entry entry : mapMultipleUserCred.entrySet()) {
                    buf.append("<tr> <td>");
                    buf.append(entry.getKey());
                    buf.append("</td>");
                    buf.append("<td>");
                    buf.append(entry.getValue());
                    buf.append("</td></tr>");
                } 

                //Run Application Authentication Test
                Map<String, Boolean> mapAppAuth =
                        testAppAuth.runTest(dsAppAuth, out);
                for (Map.Entry entry : mapAppAuth.entrySet()) {
                    buf.append("<tr> <td>");
                    buf.append(entry.getKey());
                    buf.append("</td>");
                    buf.append("<td>");
                    buf.append(entry.getValue());
                    buf.append("</td></tr>");
                }

                //Run Statement Timeout Test
                Map<String, Boolean> mapStmtTimeout =
                        testStmtTimeout.runTest(dsStmtTimeout, out);
                for (Map.Entry entry : mapStmtTimeout.entrySet()) {
                    buf.append("<tr> <td>");
                    buf.append(entry.getKey());
                    buf.append("</td>");
                    buf.append("<td>");
                    buf.append(entry.getValue());
                    buf.append("</td></tr>");
                }

                //Run Max Connection Usage Test
                Map<String, Boolean> mapMaxConnUsage =
                        testMaxConnUsage.runTest(dsMaxConnUsage, out);
                for (Map.Entry entry : mapMaxConnUsage.entrySet()) {
                    buf.append("<tr> <td>");
                    buf.append(entry.getKey());
                    buf.append("</td>");
                    buf.append("<td>");
                    buf.append(entry.getValue());
                    buf.append("</td></tr>");
                }

                //Run Associate With Thread Test
                Map<String, Boolean> mapAssocWithThread =
                        testAssocWithThread.runTest(dsAssocWithThread, out);
                for (Map.Entry entry : mapAssocWithThread.entrySet()) {
                    buf.append("<tr> <td>");
                    buf.append(entry.getKey());
                    buf.append("</td>");
                    buf.append("<td>");
                    buf.append(entry.getValue());
                    buf.append("</td></tr>");
                } 


                //Run SimpleXADS test
          /*   Map<String, Boolean> mapXADStest =
                testXA.runTest(dsXA, out);
                for (Map.Entry entry : mapXADStest.entrySet()) {
                buf.append("<tr> <td>");
                buf.append(entry.getKey());
                buf.append("</td>");
                buf.append("<td>");
                buf.append(entry.getValue());
                buf.append("</td></tr>");
                }   */

                //Run other tests
                for (SimpleTest test : testsOther) {
                    Map<String, Boolean> map = test.runTest(dsCommon, out);
                    for (Map.Entry entry : map.entrySet()) {
                        buf.append("<tr> <td>");
                        buf.append(entry.getKey());
                        buf.append("</td>");
                        buf.append("<td>");
                        buf.append(entry.getValue());
                        buf.append("</td></tr>");
                    }
                } 

                 //Order of test is important : lazy enlist has to be before connection leak tracing
                Map<String, Boolean> mapDoubleResourceReference =
                        testDoubleResourceReference.runTest(dsDoubleReference, out);
                for (Map.Entry entry : mapDoubleResourceReference.entrySet()) {
                    buf.append("<tr> <td>");
                    buf.append(entry.getKey());
                    buf.append("</td>");
                    buf.append("<td>");
                    buf.append(entry.getValue());
                    buf.append("</td></tr>");
                }

                //Order of test is important : lazy enlist has to be before connection leak tracing
                Map<String, Boolean> mapLazyEnlist =
                        testLazyEnlist.runTest(dsLazyEnlist, out);
                for (Map.Entry entry : mapLazyEnlist.entrySet()) {
                    buf.append("<tr> <td>");
                    buf.append(entry.getKey());
                    buf.append("</td>");
                    buf.append("<td>");
                    buf.append(entry.getValue());
                    buf.append("</td></tr>");
                }

                //Order of test is important : lazy enlist has to be before connection leak tracing
                //Run Connection Leak Tracing Test
                Map<String, Boolean> mapConnLeakTracing =
                        testConnLeakTracing.runTest(dsConnLeakTracing, out);
                for (Map.Entry entry : mapConnLeakTracing.entrySet()) {
                    buf.append("<tr> <td>");
                    buf.append(entry.getKey());
                    buf.append("</td>");
                    buf.append("<td>");
                    buf.append(entry.getValue());
                    buf.append("</td></tr>");
                }


            }

            //Always Run Lazy Connection Association Test as last test
            Map<String, Boolean> mapLazyAssoc =
                    testLazyAssoc.runTest(dsLazyAssoc, out);
            for (Map.Entry entry : mapLazyAssoc.entrySet()) {
                buf.append("<tr> <td>");
                buf.append(entry.getKey());
                buf.append("</td>");
                buf.append("<td>");
                buf.append(entry.getValue());
                buf.append("</td></tr>");
            }


            buf.append("</table>");
            out.println(buf.toString());
        } catch (Throwable e) {
            out.println("got outer excpetion");
            out.println(e.getMessage());
            HtmlUtil.printException(e, out);

        } finally {
            out.println("</body>");
            out.println("</html>");

            out.close();
            out.flush();
        }
    }

   private SimpleTest loadDoubleResourceReferenceTest() throws Exception {
        String test = "org.glassfish.jdbc.devtests.v3.test.DoubleResourceReferenceTest";
        Class testClass = Class.forName(test);
        Constructor c = testClass.getConstructor();
        SimpleTest testInstance = (SimpleTest) c.newInstance();
        return testInstance;
    }

    private SimpleTest loadAppAuthTest() throws Exception {
        String test = "org.glassfish.jdbc.devtests.v3.test.ApplicationAuthTest";
        Class testClass = Class.forName(test);
        Constructor c = testClass.getConstructor();
        SimpleTest testInstance = (SimpleTest) c.newInstance();
        return testInstance;
    }

    private SimpleTest loadAssocWithThreadTest() throws Exception {
        String test = "org.glassfish.jdbc.devtests.v3.test.AssocWithThreadTest";
        Class testClass = Class.forName(test);
        Constructor c = testClass.getConstructor();
        SimpleTest testInstance = (SimpleTest) c.newInstance();
        return testInstance;
    }

    private SimpleTest loadLazyAssocTest() throws Exception {
        String test = "org.glassfish.jdbc.devtests.v3.test.LazyConnectionAssociationTest";
        Class testClass = Class.forName(test);
        Constructor c = testClass.getConstructor();
        SimpleTest testInstance = (SimpleTest) c.newInstance();
        return testInstance;
    }

    private SimpleTest loadLazyEnlistTest() throws Exception {
        String test = "org.glassfish.jdbc.devtests.v3.test.LazyConnectionEnlistmentTest";
        Class testClass = Class.forName(test);
        Constructor c = testClass.getConstructor();
        SimpleTest testInstance = (SimpleTest) c.newInstance();
        return testInstance;
    }

    private SimpleTest loadConnLeakTracingTest() throws Exception {
        String test = "org.glassfish.jdbc.devtests.v3.test.ConnectionLeakTracingTest";
        Class testClass = Class.forName(test);
        Constructor c = testClass.getConstructor();
        SimpleTest testInstance = (SimpleTest) c.newInstance();
        return testInstance;
    }

    private SimpleTest loadMaxConnUsageTest() throws Exception {
        String test = "org.glassfish.jdbc.devtests.v3.test.MaxConnectionUsageTest";
        Class testClass = Class.forName(test);
        Constructor c = testClass.getConstructor();
        SimpleTest testInstance = (SimpleTest) c.newInstance();
        return testInstance;
    }

    private SimpleTest loadMultipleUserCredTest() throws Exception {
        String test = "org.glassfish.jdbc.devtests.v3.test.MultipleUserCredentialsTest";
        Class testClass = Class.forName(test);
        Constructor c = testClass.getConstructor();
        SimpleTest testInstance = (SimpleTest) c.newInstance();
        return testInstance;
    }

    private SimpleTest[] initializeTests() throws Exception {
        String[] tests = {"org.glassfish.jdbc.devtests.v3.test.ConnectionSharingTest",
            "org.glassfish.jdbc.devtests.v3.test.LeakTest",
            "org.glassfish.jdbc.devtests.v3.test.UserTxTest",
            "org.glassfish.jdbc.devtests.v3.test.NoTxConnTest",
            "org.glassfish.jdbc.devtests.v3.test.MultipleConnectionCloseTest",
            "org.glassfish.jdbc.devtests.v3.test.MarkConnectionAsBadTest",
            "org.glassfish.jdbc.devtests.v3.test.ContainerAuthTest"
        };

        SimpleTest[] testInstances = new SimpleTest[tests.length];
        for (int i = 0; i < tests.length; i++) {
            Class testClass = Class.forName(tests[i]);
            //Constructor c = testClass.getConstructor(javax.sql.DataSource.class, java.io.PipedInputStream.class);
            Constructor c = testClass.getConstructor();
            testInstances[i] = (SimpleTest) c.newInstance();
        }
        return testInstances;
    }    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">

    /**
     * Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /** 
     * Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /** 
     * Returns a short description of the servlet.
     */
    public String getServletInfo() {
        return "Short description";
    }

    private SimpleTest loadSimpleXATest() throws Exception {
        String test = "org.glassfish.jdbc.devtests.v3.test.SimpleXATest";
        Class testClass = Class.forName(test);
        Constructor c = testClass.getConstructor();
        SimpleTest testInstance = (SimpleTest) c.newInstance();
        return testInstance;
    }

    private SimpleTest loadStmtTimeoutTest() throws Exception {
        String test = "org.glassfish.jdbc.devtests.v3.test.StatementTimeoutTest";
        Class testClass = Class.forName(test);
        Constructor c = testClass.getConstructor();
        SimpleTest testInstance = (SimpleTest) c.newInstance();
        return testInstance;
    }
    // </editor-fold>
}
