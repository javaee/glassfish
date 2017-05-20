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
import javax.ejb.ConcurrentAccessException;
import javax.ejb.ConcurrentAccessTimeoutException;
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
import javax.naming.*;

@EJB(name="helloStateful", beanInterface=HelloStateful.class)
@WebServlet(urlPatterns="/HelloServlet", loadOnStartup=1)
public class HelloServlet extends HttpServlet {

    // Environment entries
    private String foo = null;

    @EJB HelloSingleton singleton;
    @EJB Hello hello;
    @EJB HelloRemote helloRemote;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

	System.out.println("In HelloServlet::init");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
    throws ServletException, IOException {
	System.out.println("In HelloServlet::doGet");

	resp.setContentType("text/html");
        PrintWriter out = resp.getWriter();

	try {
	    HelloStateful sful = (HelloStateful) new InitialContext().lookup("java:comp/env/helloStateful");
	    sful.hello();
	    hello.foo();
	} catch(Exception e) {
	    e.printStackTrace();
	}

	System.out.println("Remote intf bean says " +
			   helloRemote.hello());

	System.out.println("Calling testNoWait. This one should work since it's not a concurrent invocation");
	singleton.testNoWait();

	System.out.println("Call async wait, then sleep a bit to make sure it takes affect");
	singleton.asyncWait(1);
	try {
	    // Sleep a bit to make sure async call processes before we proceed
	    Thread.sleep(100);
	} catch(Exception e) {
	    System.out.println(e);
	}

	try {
	    System.out.println("Calling testNoWait");
	    singleton.testNoWait();
	    throw new RuntimeException("Expected ConcurrentAccessException");
	} catch(ConcurrentAccessTimeoutException cate) {
	    throw new RuntimeException("Expected ConcurrentAccessException");
	} catch(ConcurrentAccessException cae) {
	    System.out.println("Got expected exception for concurrent access on method with 0 wait");
	}

	singleton.wait(10);

	singleton.reentrantReadWrite();

	singleton.callSing2WithTxAndRollback();
	singleton.hello();

	singleton.read();
	singleton.write();
	singleton.reentrantReadRead();
	singleton.reentrantWriteWrite();
	singleton.reentrantWriteRead();

       	out.println("<HTML> <HEAD> <TITLE> JMS Servlet Output </TITLE> </HEAD> <BODY BGCOLOR=white>");
            out.println("<CENTER> <FONT size=+1 COLOR=blue>DatabaseServelt :: All information I can give </FONT> </CENTER> <p> " );
            out.println("<FONT size=+1 color=red> Context Path :  </FONT> " + req.getContextPath() + "<br>" ); 
            out.println("<FONT size=+1 color=red> Servlet Path :  </FONT> " + req.getServletPath() + "<br>" ); 
            out.println("<FONT size=+1 color=red> Path Info :  </FONT> " + req.getPathInfo() + "<br>" ); 
            out.println("</BODY> </HTML> ");

    }


}
