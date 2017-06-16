/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2017 Oracle and/or its affiliates. All rights reserved.
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

package com.acme;

import java.io.IOException;
import java.io.PrintWriter;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.naming.InitialContext;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
@WebServlet(urlPatterns = "/HelloServlet", loadOnStartup = 1)
@EJB(name = "java:module/m1", beanName = "HelloSingleton", beanInterface = Hello.class)
public class NormalLookupInEARServlet extends HttpServlet {

    @Resource(name = "java:app/env/myString")
    protected String myString;

    private Hello singleton1;
    
    @Inject 
    TestBean tw;

    private String msg = "";

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        System.out.println("In HelloServlet::init");
        System.out.println("myString = '" + myString + "'");
        if ((myString == null) || !(myString.equals("myString"))) {
            msg += "@Resource lookup of myString failed";
            throw new RuntimeException("Invalid value " + myString
                    + " for myString");
        }

    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        System.out.println("In HelloServlet::doGet");
        resp.setContentType("text/html");

        PrintWriter out = resp.getWriter();
        try {
            InitialContext ic = new InitialContext();
            String appName = (String) ic.lookup("java:app/AppName");
            String moduleName = (String) ic.lookup("java:module/ModuleName");
            checkForNull(appName, "AppName lookup returned null");
            checkForNull(moduleName, "ModuleName lookup returned null");

            singleton1 = (Hello) ic.lookup("java:module/m1");
            checkForNull(singleton1,
                    "programmatic lookup of module-level singleton EJB failed");

            System.out.println("My AppName = "
                    + ic.lookup("java:app/AppName"));

            System.out.println("My ModuleName = "
                    + ic.lookup("java:module/ModuleName"));

        } catch (Exception e) {
            msg += "Exception occurred during test Exception: "
                    + e.getMessage();
            e.printStackTrace();
        }

        singleton1.hello();
        
        checkForNull(tw, "normal lookup of session scoped bean in war failed");

        out.println(msg);

    }

    protected void checkForNull(Object o, String errorMessage) {
        if (o == null)
            msg += " " + errorMessage;
    }
}
