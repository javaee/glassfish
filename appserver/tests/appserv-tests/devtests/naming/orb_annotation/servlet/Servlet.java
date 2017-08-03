/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2002-2017 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.s1asdev.orb.annotation;

import java.io.*;
import java.rmi.RemoteException;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.naming.*;
import javax.rmi.PortableRemoteObject;
import javax.annotation.Resource;
import javax.annotation.Resources;
import org.omg.CORBA.ORB;
import org.omg.PortableServer.POA;

public class Servlet extends HttpServlet {

    private @Resource(shareable=false) ORB unshareableOrb;
    private @Resource ORB shareableOrb;
    
    public void  init( ServletConfig config) throws ServletException {
        
        super.init(config);
        System.out.println("In webclient::servlet... init()");
    }
    
    public void service ( HttpServletRequest req , HttpServletResponse resp ) throws ServletException, IOException {
                 
        resp.setContentType("text/html");
        PrintWriter out = resp.getWriter();
        
        try {

            InitialContext ic = new InitialContext();

	    //out.println("doing shareable orb test");
            //out.println("ORB = " + shareableOrb);
            POA poa = (POA) shareableOrb.resolve_initial_references("RootPOA");
            //out.println("POA = " + poa);

	    //out.println("doing unshareable orb test");
            //out.println("ORB = " + unshareableOrb);
            POA poa1 = (POA) unshareableOrb.resolve_initial_references("RootPOA");
            //out.println("POA = " + poa1);

            out.println("<HTML> <HEAD> <TITLE> ORB Annotation Test  Servlet Output </TITLE> </HEAD> <BODY BGCOLOR=white>");
            out.println("<CENTER> <FONT size=+1 COLOR=blue>ORB Annotation Test Servlet </FONT> </CENTER> <p> " );
            out.println("<FONT size=+1 color=red> doing unshareable orb test :  </FONT> " + "<br>" ); 
            out.println("<FONT size=+1 color=red> ORB = " + unshareableOrb + "</FONT> "  + "<br>" ); 
            out.println("<FONT size=+1 color=red> POA =  </FONT> " + poa1 + "<br>" ); 

out.println("<FONT size=+1 color=red> doing shareable orb test :  </FONT> " + "<br>" ); 
            out.println("<FONT size=+1 color=red> ORB = " + shareableOrb + "</FONT> "  + "<br>" ); 
            out.println("<FONT size=+1 color=red> POA =  </FONT> " + poa + "<br>" ); 
            out.println("</BODY> </HTML> ");
            
        }catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("webclient servlet test failed");
            throw new ServletException(ex);
        } 
    }

    
    
    public void  destroy() {
        System.out.println("in webclient::servlet destroy");
    }
    
}
