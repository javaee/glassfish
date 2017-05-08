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

package com.acme;

import javax.ejb.EJB;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.annotation.WebServlet;
import javax.annotation.Resource;
import javax.naming.*;

@WebServlet(urlPatterns="/HelloServlet", loadOnStartup=1)
@EJB(name="java:module/ES1", beanName="SingletonBean", beanInterface=SingletonBean.class)
public class HelloServlet extends HttpServlet {

    @EJB(name="java:module/env/ES2")
    private SingletonBean simpleSingleton;

    @EJB(name="java:app/EL1")
    private StatelessBean simpleStateless;

    @EJB(name="java:app/env/EL2")
    private StatelessBean simpleStateless2;

    private SingletonBean sb2;
    private SingletonBean sb3;
    private SingletonBean sb4;
    private SingletonBean sb5;
    private StatelessBean slsb;
    private StatelessBean slsb2;
    private StatelessBean slsb3;
    private StatelessBean slsb4;
    private StatelessBean slsb5;

    @Resource
    private FooManagedBean foo;

    @Resource(name="foo2ref", mappedName="java:module/foomanagedbean")
    private FooManagedBean foo2;

    @Resource(mappedName="java:app/ejb-ejb31-ejblite-javamodule-web/foomanagedbean")
    private FooManagedBean foo3;

    private FooManagedBean foo4;
    private FooManagedBean foo5;
    private FooManagedBean foo6;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

	System.out.println("In HelloServlet::init");

	try {
	    InitialContext ic = new InitialContext();
	    sb2 = (SingletonBean) ic.lookup("java:module/SingletonBean");
	    sb3 = (SingletonBean) ic.lookup("java:module/SingletonBean!com.acme.SingletonBean");

	    sb4 = (SingletonBean) ic.lookup("java:module/ES1");
	    sb5 = (SingletonBean) ic.lookup("java:module/env/ES2");

	    slsb = (StatelessBean) ic.lookup("java:module/StatelessBean");
	    slsb2 = (StatelessBean) ic.lookup("java:app/ejb-ejb31-ejblite-javamodule-web/StatelessBean");
	    slsb3 = (StatelessBean) ic.lookup("java:app/ejb-ejb31-ejblite-javamodule-web/StatelessBean!com.acme.StatelessBean");

	    slsb4 = (StatelessBean) ic.lookup("java:app/EL1");
	    slsb5 = (StatelessBean) ic.lookup("java:app/env/EL2");

	    foo4 = (FooManagedBean) 
		ic.lookup("java:module/foomanagedbean");

	    foo5 = (FooManagedBean) 
		ic.lookup("java:app/ejb-ejb31-ejblite-javamodule-web/foomanagedbean");

	    foo6 = (FooManagedBean)
		ic.lookup("java:comp/env/foo2ref");

	    System.out.println("My AppName = " + 
			       ic.lookup("java:app/AppName"));

	    System.out.println("My ModuleName = " + 
			       ic.lookup("java:module/ModuleName"));

	} catch(Exception e) {
	    e.printStackTrace();
	    throw new ServletException(e);
	}
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
    throws ServletException, IOException {

	resp.setContentType("text/html");
        PrintWriter out = resp.getWriter();

	System.out.println("In HelloServlet::doGet");

	foo.foobar("foobar");

	simpleSingleton.hello();

	simpleStateless.hello();
	simpleStateless2.hello();

	sb2.hello();

	sb3.hello();

	sb4.hello();

	sb5.hello();

	slsb.hello();

	slsb2.hello();

	slsb3.hello();

	slsb4.hello();

	slsb5.hello();

	foo.foo();
	foo.foobar("foobar");
	foo2.foo();
	foo3.foo();
	foo4.foo();
	foo5.foo();	
	foo6.foo();

	out.println("<HTML> <HEAD> <TITLE> JMS Servlet Output </TITLE> </HEAD> <BODY BGCOLOR=white>");
            out.println("<CENTER> <FONT size=+1 COLOR=blue>DatabaseServelt :: All information I can give </FONT> </CENTER> <p> " );
            out.println("<FONT size=+1 color=red> Context Path :  </FONT> " + req.getContextPath() + "<br>" ); 
            out.println("<FONT size=+1 color=red> Servlet Path :  </FONT> " + req.getServletPath() + "<br>" ); 
            out.println("<FONT size=+1 color=red> Path Info :  </FONT> " + req.getPathInfo() + "<br>" ); 
            out.println("</BODY> </HTML> ");

    }


}
