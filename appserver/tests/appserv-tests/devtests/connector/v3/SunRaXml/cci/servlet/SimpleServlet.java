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

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import javax.naming.InitialContext;
import javax.naming.Context;
import javax.rmi.PortableRemoteObject;
import java.io.IOException;
import java.io.PrintWriter;


public class SimpleServlet extends HttpServlet {


    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doPost(request, response);
    }

    /**
     * handles the HTTP POST operation *
     */
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        test(request, response);
    }

    private void test(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String testId = "J2EE Connectors 1.5 : Standalone adapter tests";

        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        try {

            out.println(testId + " : CoffeeClient started in main...");
            out.println("J2EE Connectors 1.5 : Standalone CCI adapter Tests");
            Context initial = new InitialContext();
            com.sun.s1peqe.connector.cci.CoffeeRemoteHome home = (com.sun.s1peqe.connector.cci.CoffeeRemoteHome)
                    initial.lookup("java:comp/env/ejb/SimpleCoffee");


            com.sun.s1peqe.connector.cci.CoffeeRemote coffee = home.create();

            int count = coffee.getCoffeeCount();
            out.println("Coffee count = " + count);

            out.println("Inserting 3 coffee entries...");
            coffee.insertCoffee("Mocha", 10);
            coffee.insertCoffee("Espresso", 20);
            coffee.insertCoffee("Kona", 30);

            int newCount = coffee.getCoffeeCount();
            out.println("Coffee count = " + newCount);
            if (count == (newCount - 3)) {
                out.println("Connector:cci Connector " + testId + " rar Test status:" + " PASS");
                out.println("TEST:PASS");
            } else {
                out.println("Connector:cci Connector " + testId + " rar Test status:" + " FAIL");
                out.println("TEST:FAIL");

            }

            //print test summary
            //stat.printSummary(testId);


        } catch (Exception ex) {
            out.println("Caught an unexpected exception!");
            out.println("Connector:CCI Connector " + testId + " rar Test status:" + " PASS");
            ex.printStackTrace();
        } finally {
            out.println("END_OF_TEST");
        }
    }

    private void debug(String msg) {
        System.out.println("[Redeploy Connector CLIENT]:: --> " + msg);
    }
}
