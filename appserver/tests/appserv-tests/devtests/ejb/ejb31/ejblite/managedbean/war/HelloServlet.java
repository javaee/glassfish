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
import javax.naming.*;
import javax.annotation.Resource;

@WebServlet(urlPatterns="/HelloServlet", loadOnStartup=1)
public class HelloServlet extends HttpServlet {

    @Resource    
    private ManagedBeanExtra mbExtra;


    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
	System.out.println("In HelloServlet::init");
	mbExtra.hello();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
    throws ServletException, IOException {

	resp.setContentType("text/html");
        PrintWriter out = resp.getWriter();

	System.out.println("In HelloServlet::doGet");

	int numIter = 2;

	try {

	    doTest(numIter, "java:module/ManagedBeanNoInt", 0, "");
	    doTest(numIter, "java:module/ManagedBean1Int", numIter, "A");
	    doTest(numIter, "java:module/ManagedBean2Int", numIter*2, "AB");
	    doTest(numIter, "java:module/ManagedBean2IntPlusBean", numIter*2, "ABM");
	    doTest(numIter, "java:module/ManagedBeanNoIntPlusBean", 0, "M");

	    doTest(numIter, "java:module/ManagedBean2IntExcludeClass", numIter*2, "");

	    doTest(numIter, "java:module/ManagedBean1Class1MethodLevelInt", numIter*1, "BA");

	    doTest(numIter, "java:module/ManagedBean1MethodLevelIntExcludeClass",
		   numIter*1, "A");

	    doTest(numIter, "java:module/ManagedBean2MethodLevelInt", 0, "AB");
	    

	} catch(Exception e) {
	    throw new RuntimeException(e);
	}
	    


	out.println("<HTML> <HEAD> <TITLE> JMS Servlet Output </TITLE> </HEAD> <BODY BGCOLOR=white>");
            out.println("<CENTER> <FONT size=+1 COLOR=blue>DatabaseServelt :: All information I can give </FONT> </CENTER> <p> " );
            out.println("<FONT size=+1 color=red> Context Path :  </FONT> " + req.getContextPath() + "<br>" ); 
            out.println("<FONT size=+1 color=red> Servlet Path :  </FONT> " + req.getServletPath() + "<br>" ); 
            out.println("<FONT size=+1 color=red> Path Info :  </FONT> " + req.getPathInfo() + "<br>" ); 
            out.println("</BODY> </HTML> ");

    }

    private void doTest(int numIter, String lookup, int numInterceptors,
			String aroundInvoke) throws Exception {

	System.out.println("Test " + lookup + " Expect numInstances = " + numIter + 
			   " , numInterceptors = " + numInterceptors + 
			   " , aroundInvoke = " + aroundInvoke);
	for(int i = 0; i < numIter; i++) {
	    ManagedBeanSuper mb = (ManagedBeanSuper) new InitialContext().lookup(lookup);

	    try {
		mb.throwAppException();
		throw new RuntimeException("Expected AppException , got nothing");
	    } catch(AppException ae) {
		System.out.println("Successfully caught AppException");
	    } catch(Throwable t) {
		throw new RuntimeException("Expected AppException , got " + t, t);
	    }

	    try {
		mb.throwIllegalArgumentException();
		throw new RuntimeException("Expected IllegalArgumentException , got nothing");
	    } catch(IllegalArgumentException iae) {
		System.out.println("Successfully caught IllegalArgumentException");
	    } catch(Throwable t) {
		throw new RuntimeException("Expected IllegalArgumentException , got " + t, t);
	    }

	    if( i ==  (numIter - 1) ) {
		int actualNumInst =  mb.getNumInstances();
		int actualNumInter =  mb.getNumInterceptorInstances();
		String actualAround = mb.getAroundInvokeSequence();
		System.out.println("actual num Instances = " + actualNumInst);
		System.out.println("actual num Interceptors = " + actualNumInter);
		System.out.println("actual around invoke sequence = " + actualAround);

		if( ( actualNumInst != numIter ) || (actualNumInter != numInterceptors) ||
		    !actualAround.equals(aroundInvoke) ) {
		    throw new RuntimeException("actual results failure for " + lookup);
		} 
	    }

	}
    }


}
