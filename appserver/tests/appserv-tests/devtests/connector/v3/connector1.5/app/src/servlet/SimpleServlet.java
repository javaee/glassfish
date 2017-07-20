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

package servlet;

import beans.MessageCheckerHome;
import beans.MessageChecker;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import java.io.IOException;
import java.io.PrintWriter;

public class SimpleServlet extends HttpServlet {


    public void doGet (HttpServletRequest request, HttpServletResponse response)
          throws ServletException, IOException {
      doPost(request, response);
    }

    /** handles the HTTP POST operation **/
    public void doPost (HttpServletRequest request,HttpServletResponse response)
          throws ServletException, IOException {
        doTest(request, response);
    }

    public String doTest(HttpServletRequest request, HttpServletResponse response) throws IOException{
        System.out.println("This is to test connector 1.5 "+
	             "contracts.");

        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        String res = "NOT RUN";
	debug("doTest() ENTER...");
        boolean pass = false;
        try {
            res  = "ALL TESTS PASSED";
	    int testCount = 1;
            out.println("Starting the test");
            out.flush();
            while (!done(out)) {

                notifyAndWait(out);
                if (!done(out)) {
                    debug("Running...");
                    pass = checkResults(expectedResults(out), out);
                    debug("Got expected results = " + pass);

                    //do not continue if one test failed
                    if (!pass) {
                        res = "SOME TESTS FAILED";
                        System.out.println("ID Connector 1.5 test - " + testCount + " FAIL");
                        out.println("TEST:FAIL");
                        
                    } else {
                        System.out.println("ID Connector 1.5 test - " + testCount + " PASS");
                        out.println("TEST:PASS");
		            }
                } else {
                    out.println("END_OF_EXECUTION");
                    break;
                }
            }
            out.println("END_OF_EXECUTION");
            

        } catch (Exception ex) {
            System.out.println("Importing transaction test failed.");
            ex.printStackTrace();
            res = "TEST FAILED";
            out.println("TEST:FAIL");
        }finally{
            out.println("END_OF_EXECUTION");
            out.flush();
        }
        System.out.println("connector15ID");


        debug("EXITING... STATUS = " + res);
        return res;
    }

    private boolean checkResults(int num, PrintWriter out) throws Exception {
        out.println("checking results");
        out.flush();
        Object o = (new InitialContext()).lookup("java:comp/env/ejb/messageChecker");
        MessageCheckerHome home = (MessageCheckerHome)
            PortableRemoteObject.narrow(o, MessageCheckerHome.class);
        MessageChecker checker = home.create();
        int result = checker.getMessageCount();
        return result == num;
    }

    private boolean done(PrintWriter out) throws Exception {
        out.println("Checking whether its completed");
        out.flush();
        Object o = (new InitialContext()).lookup("java:comp/env/ejb/messageChecker");
        MessageCheckerHome  home = (MessageCheckerHome)
            PortableRemoteObject.narrow(o, MessageCheckerHome.class);
        MessageChecker checker = home.create();
        return checker.done();
    }

    private int expectedResults(PrintWriter out) throws Exception {
        out.println("expectedResults");
        out.flush();
        Object o = (new InitialContext()).lookup("java:comp/env/ejb/messageChecker");
        MessageCheckerHome  home = (MessageCheckerHome)
            PortableRemoteObject.narrow(o, MessageCheckerHome.class);
        MessageChecker checker = home.create();
        return checker.expectedResults();
    }

    private void notifyAndWait(PrintWriter out) throws Exception {
        out.println("notifyAndWait");
        out.flush();
        Object o = (new InitialContext()).lookup("java:comp/env/ejb/messageChecker");
        MessageCheckerHome  home = (MessageCheckerHome)
            PortableRemoteObject.narrow(o, MessageCheckerHome.class);
        MessageChecker checker = home.create();
        checker.notifyAndWait();
    }


    private void debug(String msg) {
        System.out.println("[CLIENT]:: --> " + msg);
    }
}
