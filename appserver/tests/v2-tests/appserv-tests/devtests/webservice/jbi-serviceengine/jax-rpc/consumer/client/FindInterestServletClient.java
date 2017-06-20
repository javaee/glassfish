/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package myclient;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.Serializable;
import java.io.PrintWriter;
import java.rmi.RemoteException; 
import javax.naming.*;
import javax.xml.namespace.QName;

public class FindInterestServletClient extends HttpServlet
			{
    HttpServletResponse resp;
    public FindInterestServletClient() {
        System.out.println("FindInterestServletImpl() instantiated");
    }

    public void doGet(HttpServletRequest req, HttpServletResponse resp)
                throws javax.servlet.ServletException {
           this.resp = resp;
           doPost(req, resp);
    }
                                                                                
    public void doPost(HttpServletRequest req, HttpServletResponse resp)
              throws javax.servlet.ServletException {
       try{
           this.resp = resp;
           calculateInterest();
       }catch(Exception e) {
          throw new javax.servlet.ServletException(e);
       }

    }

    public void calculateInterest() throws Exception {
	System.out.println("calculateInterest invoked from servlet ");
        FindInterestClient client = new FindInterestClient();
	double interest= client.doTest();
        PrintWriter out = resp.getWriter();
                resp.setContentType("text/html");
                out.println("<html>");
                out.println("<head>");
                out.println("<title>FindInterestServletClient</title>");
                out.println("</head>");
                out.println("<body>");
                out.println("<p>");
                out.println("So the RESULT OF FindInterest SERVICE IS :");
                out.println("</p>");
                out.println("[" + interest + "]");
                out.println("</body>");
                out.println("</html>");
                out.flush();
                out.close();
    }
}
