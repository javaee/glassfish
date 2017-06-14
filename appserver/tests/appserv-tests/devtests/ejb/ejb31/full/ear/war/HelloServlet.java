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
@EJB(name="java:module/m1", beanName="HelloSingleton", beanInterface=Hello.class)
public class HelloServlet extends HttpServlet {

    @Resource(mappedName="java:module/foobarmanagedbean")
    private FooBarManagedBean fbmb;

    @Resource
    private FooBarManagedBean fbmb2;

    @EJB(name="java:module/env/m2")
    private Hello m1;

    @EJB(name="java:app/a1")
    private HelloRemote a1;

    @EJB(name="java:app/env/a2")
    private HelloRemote a2;

    @Resource(name = "java:app/env/myString")
    protected String myString;

    private Hello singleton1;
    private Hello singleton2;
    private Hello singleton3;
    private Hello singleton4;
    private Hello singleton5;
    private HelloRemote stateless1;
    private HelloRemote stateless2;

    @Resource 
    private Foo2ManagedBean foo;
    
    @Resource(name="foo2ref", mappedName="java:module/somemanagedbean")
    private Foo2ManagedBean foo2;

    @Resource(name="foo3ref", mappedName="java:app/ejb-ejb31-full-ear-ejb/somemanagedbean")
    private Foo foo3;

    private Foo2ManagedBean foo4;
    private Foo2ManagedBean foo5;
    private Foo foo6;
    private Foo2ManagedBean foo7;
    private Foo foo8;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

	System.out.println("In HelloServlet::init");
	System.out.println("myString = '" + myString + "'");
	if( (myString == null) || !(myString.equals("myString") ) ) {
	    throw new RuntimeException("Invalid value " + myString + " for myString");
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

	    // lookup via intermediate context 
	    Context appCtx = (Context) ic.lookup("java:app");
	    Context appCtxEnv = (Context) appCtx.lookup("env");
	    stateless2 = (HelloRemote) appCtxEnv.lookup("AS2");
	    NamingEnumeration<Binding> bindings = appCtxEnv.listBindings("");
	    System.out.println("java:app/env/ bindings ");
	    while(bindings.hasMore()) {
		System.out.println("binding : " + bindings.next().getName());
	    }


	    foo4 = (Foo2ManagedBean) ic.lookup("java:module/somemanagedbean");
	    foo5 = (Foo2ManagedBean) ic.lookup("java:app/" + moduleName +
						  "/somemanagedbean");
	    foo6 = (Foo) ic.lookup("java:app/ejb-ejb31-full-ear-ejb/somemanagedbean");
	    foo7 = (Foo2ManagedBean) ic.lookup("java:comp/env/foo2ref");
	    foo8 = (Foo) ic.lookup("java:comp/env/foo3ref");


	    singleton1 = (Hello) ic.lookup("java:module/m1");

	    // standard java:app name for ejb 
	    singleton2 = (Hello) ic.lookup("java:app/ejb-ejb31-full-ear-ejb/HelloSingleton");

	    singleton3 = (Hello) ic.lookup("java:global/" + appName + "/ejb-ejb31-full-ear-ejb/HelloSingleton");

	    // lookup some java:app defined by ejb-jar
	    singleton4 = (Hello) ic.lookup("java:app/env/AS1");

	    // global dependency
	    singleton5 = (Hello) ic.lookup("java:global/GS1");


	    stateless1 = (HelloRemote) ic.lookup("java:app/env/AS2");

	    System.out.println("My AppName = " + 
			       ic.lookup("java:app/AppName"));

	    System.out.println("My ModuleName = " + 
			       ic.lookup("java:module/ModuleName"));



	    try {
		org.omg.CORBA.ORB orb = (org.omg.CORBA.ORB) ic.lookup("java:module/MORB1");
		throw new RuntimeException("Should have gotten naming exception");
	    } catch(NamingException ne) {
		System.out.println("Successfully was *not* able to see ejb-jar module-level dependency");
	    }

	} catch(Exception e) {
	    e.printStackTrace();
	}

	foo.foo();
	foo2.foo();
	foo3.foo();

	foo4.foo();
	foo5.foo();
	foo6.foo();
	foo7.foo();
      	foo8.foo();

	m1.hello();
	a1.hello();
	a2.hello();
	singleton1.hello();
	singleton2.hello();
	singleton3.hello();
	singleton4.hello();
	singleton5.hello();

	stateless1.hello();
	stateless2.hello();


       	out.println("<HTML> <HEAD> <TITLE> JMS Servlet Output </TITLE> </HEAD> <BODY BGCOLOR=white>");
            out.println("<CENTER> <FONT size=+1 COLOR=blue>DatabaseServelt :: All information I can give </FONT> </CENTER> <p> " );
            out.println("<FONT size=+1 color=red> Context Path :  </FONT> " + req.getContextPath() + "<br>" ); 
            out.println("<FONT size=+1 color=red> Servlet Path :  </FONT> " + req.getServletPath() + "<br>" ); 
            out.println("<FONT size=+1 color=red> Path Info :  </FONT> " + req.getPathInfo() + "<br>" ); 
            out.println("</BODY> </HTML> ");

    }


}
