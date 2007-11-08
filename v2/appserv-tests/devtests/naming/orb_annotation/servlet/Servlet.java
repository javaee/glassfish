/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
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
