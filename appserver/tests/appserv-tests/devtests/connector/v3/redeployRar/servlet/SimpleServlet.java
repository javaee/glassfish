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

import beans.VersionCheckerHome;
import beans.VersionChecker;

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

    public String doTest( HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        int versionToTest = Integer.valueOf((String)request.getParameter("versionToTest"));
        out.println("This is to test redeployment of connector modules. Testing version " + versionToTest);


        String res = "NOT RUN";
	    debug("doTest() ENTER...");
        boolean pass = false;
        try {
		pass = checkResults(versionToTest);
		debug("Got expected results = " + pass);

		//do not continue if one test failed
		if (!pass) {
			res = "SOME TESTS FAILED";
			System.out.println("Redeploy Connector 1.5 test - Version : "+ versionToTest + " FAIL");
            out.println("TEST:FAIL");
		} else {
			res  = "ALL TESTS PASSED";
			System.out.println("Redeploy Connector 1.5 test - Version : "+ versionToTest + " PASS");
            out.println("TEST:PASS");
		}
        } catch (Exception ex) {
            System.out.println("Redeploy connector test failed.");
            ex.printStackTrace();
            res = "TEST FAILED";
        }finally{
            out.println("Redeploy Connector 1.5");
            out.println("END_OF_TEST");
        }

        debug("EXITING... STATUS = " + res);

        return res;
    }

    private boolean checkResults(int num) throws Exception {
	    debug("checkResult" + num);
	    debug("got initial context" + (new InitialContext()).toString());
        Object o = (new InitialContext()).lookup("java:comp/env/ejb/MyVersionChecker");
	debug("got o" + o);
        VersionCheckerHome home = (VersionCheckerHome)
            PortableRemoteObject.narrow(o, VersionCheckerHome.class);
        debug("got home" + home);
	    VersionChecker checker = home.create();
	    debug("got o" + checker);
        //problem here!
	int result = checker.getVersion();
	debug("checkResult" + result);
        return result == num;
    }

    private void debug(String msg) {
        System.out.println("[Redeploy Connector CLIENT]:: --> " + msg);
    }
}
