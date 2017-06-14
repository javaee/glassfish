/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
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
package org.glassfish.tests.embedded.cdi_ejb_jpa;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import javax.annotation.sql.DataSourceDefinition;

@WebServlet(name = "BasicCDITestServlet",
urlPatterns = "/BasicCDITestServlet")

@DataSourceDefinition(
            name="java:app/jdbc/DB1",
            className="org.apache.derby.jdbc.EmbeddedDataSource",
            portNumber=1527,
            serverName="localhost",
            databaseName="sun-appserv-samples",
            user="APP",
            password="APP",
            properties={"connectionAttributes=;create=true"}
)
public class BasicCDITestServlet extends HttpServlet {

    @javax.inject.Inject
    TestBean testBean;

    @javax.inject.Inject
    TestRequestScopedBean trsb;

    @Override
    protected void doGet(HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse) throws ServletException, IOException {
        PrintWriter out = httpServletResponse.getWriter();
        out.println("Hi from BasicCDITestServlet");
        if (testBean == null) {
            out.println("TestBean not injected.");
        } else if (trsb == null) {
            out.println("TestRequestScopeBean not injected.");
        } else {
            out.println("TestBean injected. [" + testBean + "]");
            out.println("TestRequestScopeBean injected. [ " + trsb + "]");
            out.println("All CDI beans have been injected.");
        }
        invokeTestBean(out);
        out.flush();
        out.close();
    }

    private void invokeTestBean(PrintWriter out) {
        testBean.addPerson("Ada");
        testBean.addPerson("Bob");
        testBean.addPerson("Cub");
        out.println("Added persons.");

        Person p1 = testBean.getPerson(1L);
        Person p2 = testBean.getPerson(2L);
        Person p3 = testBean.getPerson(3L);
        out.println("Retrieved persons: " + p1 + ", " + p2 + ", " + p3);
    }
}
