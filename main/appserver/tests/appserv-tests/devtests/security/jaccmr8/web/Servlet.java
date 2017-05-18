/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2013-2017 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jacc.test.mr8;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.naming.*;
import javax.ejb.EJB;
import javax.ejb.EJBs;
import java.net.URLEncoder;
import java.net.URLDecoder;
import java.security.Principal;

import javax.servlet.annotation.WebServlet;

@WebServlet(name = "Servlet", urlPatterns = {"/servlet","/authuser","/anyauthuser","/star","/denyuncoveredpost"})
public class Servlet extends HttpServlet {

	@EJB(beanName = "HelloEJB", beanInterface = Hello.class)
	private Hello helloStateless;

	@EJB(beanName = "HelloStatefulEJB", beanInterface = HelloStateful.class)
	private HelloStateful helloStateful;

	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		System.out.println("In jaccmr8::Servlet... init()");
	}

	public void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setContentType("text/html");
		PrintWriter out = resp.getWriter();

		String mode = req.getParameter("mode");
		if (mode == null) mode = "stateful";
		mode = URLDecoder.decode(mode,"UTF-8");
		String name = req.getParameter("name");
		if (name == null) name = "NotDeclared";
		name = URLDecoder.decode(name,"UTF-8");

		// EJB information
		String callerPrincipal = "NONE";
		boolean isInEJBRole = false;
		boolean isAnyAuthUserEJB = false;
		String invokeAnyAuthUser = "No";
		String invokeAuthUser = "No";
		if ("stateful".equals(mode)) {
			System.out.println("Invoking Stateful EJB");
			callerPrincipal = helloStateful.hello(name);
			isAnyAuthUserEJB = helloStateful.inRole("**");
			isInEJBRole = helloStateful.inRole(name);
			try {
				helloStateful.methodAnyAuthUser();
				invokeAnyAuthUser = "Yes";
			}
			catch (Exception exc) {
				System.out.println("FAILED invoke of methodAnyAuthUser()");
				invokeAnyAuthUser = exc.toString();
			}
			try {
				helloStateful.methodAuthUser();
				invokeAuthUser = "Yes";
			}
			catch (Exception exc) {
				System.out.println("FAILED invoke of methodAuthUser()");
				invokeAuthUser = exc.toString();
			}
			System.out.println("Successfully invoked Stateful EJB");
		} else if ("stateless".equals(mode)) {
			System.out.println("Invoking Stateless EJB");
			callerPrincipal = helloStateless.hello(name);
			isAnyAuthUserEJB = helloStateless.inRole("**");
			isInEJBRole = helloStateless.inRole(name);
			try {
				helloStateless.methodAnyAuthUser();
				invokeAnyAuthUser = "Yes";
			}
			catch (Exception exc) {
				System.out.println("FAILED invoke of methodAnyAuthUser()");
				invokeAnyAuthUser = exc.toString();
			}
			try {
				helloStateless.methodAuthUser();
				invokeAuthUser = "Yes";
			}
			catch (Exception exc) {
				System.out.println("FAILED invoke of methodAuthUser()");
				invokeAuthUser = exc.toString();
			}
			System.out.println("Successfully invoked Stateless EJB");
		} else {
			System.out.println("Mode: " + mode);
		}

		// Servlet information
		String principalName = "NONE";
		String principalType = "UNKNOWN";
		Principal p = req.getUserPrincipal();
		if (p != null) {
			principalName = p.getName();
			principalType = p.getClass().getName();
		}
		String userPrincipal = principalName + " is " + principalType; 
		boolean isAnyAuthUserWeb = req.isUserInRole("**");
		boolean isInWebRole = req.isUserInRole(name);

		out.println("<HTML> <HEAD> <TITLE>Servlet Output</TITLE> </HEAD> <BODY>");
		out.println("<CENTER>JACC MR8 Servlet</CENTER> <p> ");
		out.println(" Request URL: " + req.getRequestURL() + "<br>");
		out.println(" HTTP Method: " + req.getMethod() + "<br>");
		out.println("Context Path: " + req.getContextPath() + "<br>");
		out.println("Servlet Path: " + req.getServletPath() + "<br>");
		out.println("<br> <CENTER>Results</CENTER> <p> ");
		out.println("EJB Caller Principal: " + callerPrincipal + "<br>");
		out.println("EJB isCallerInRole: " + isInEJBRole + "<br>");
		out.println("EJB isUserInAnyAuthUserRole: " + isAnyAuthUserEJB + "<br>");
		out.println("EJB Invoke AnyAuthUser: " + invokeAnyAuthUser + "<br>");
		out.println("EJB Invoke AuthUser: " + invokeAuthUser + "<br>");
		out.println("WEB User Principal: " + userPrincipal + "<br>");
		out.println("WEB isUserInRole: " + isInWebRole + "<br>");
		out.println("WEB isUserInAnyAuthUserRole: " + isAnyAuthUserWeb + "<br>");
		out.println("</BODY> </HTML> ");
	}

	public void destroy() {
		System.out.println("In jaccmr8::Servlet destroy");
	}
}
