package mypackage;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;
import com.sun.s1peqe.transaction.txstressreadonly.ejb.beanA.*;



/**
 * Simple servlet to validate that the Hello, World example can
 * execute servlets.  In the web application deployment descriptor,
 * this servlet must be mapped to correspond to the link in the
 * "index.html" file.
 *
 * @author Craig R. McClanahan <Craig.McClanahan@eng.sun.com>
 */

public final class Hello extends HttpServlet {


    /**
     * Respond to a GET request for the content produced by
     * this servlet.
     *
     * @param request The servlet request we are processing
     * @param response The servlet response we are producing
     *
     * @exception IOException if an input/output error occurs
     * @exception ServletException if a servlet error occurs
     */
    public void doGet(HttpServletRequest request,
                      HttpServletResponse response)
      throws IOException, ServletException {
        TxRemoteHomeA home = null;
        Class homeClass = TxRemoteHomeA.class;
        int identity = 100;
        try {
            // Initialize the Context
            Context context = new InitialContext();
            System.out.println("Context Initialized...");

            // Create Home object
            java.lang.Object obj = context.lookup("java:comp/env/ejb/TxBeanA");
            home = (TxRemoteHomeA) PortableRemoteObject.narrow(obj, homeClass);
            System.out.println("Home Object Initialized...");
            TxRemoteA beanA = home.create();
            if(request.getParameter("input") != null && !(request.getParameter("input").equals("")))
                identity = Integer.parseInt(request.getParameter("input"));
            System.out.println("Before Calling txCommit on BeanA");
            beanA.txCommit(identity);
            System.out.println("Before Calling remove on BeanA");
            beanA.remove();

        } catch (Throwable ex) {
            //System.out.println("Exception in setup: " + ex.toString());
            //ex.printStackTrace();
        }


	response.setContentType("text/html");
	PrintWriter writer = response.getWriter();

	writer.println("<html>");
	writer.println("<head>");
	writer.println("<title>Sample Transaction Stress Test Application</title>");
	writer.println("</head>");
	writer.println("<body bgcolor=white>");

	writer.println("<table border=\"0\">");
	writer.println("<tr>");
	writer.println("<td>");
	//writer.println("<img src=\"images/tomcat.gif\">");
	writer.println("</td>");
	writer.println("<td>");
	writer.println("<h1>Sample Transaction Stress Test Application</h1>");
	writer.println("This is the output of a servlet that is part of");
	writer.println("the Hello, World application.  It displays the");
	writer.println("Number that you input.");
	writer.println("</td>");
	writer.println("</tr>");
	writer.println("</table>");

	writer.println("<table border=\"0\" width=\"100%\">");
	/*Enumeration names = request.getHeaderNames();
	while (names.hasMoreElements()) {
	    String name = (String) names.nextElement();
	    writer.println("<tr>");
	    writer.println("  <th align=\"right\">" + name + ":</th>");
	    writer.println("  <td>" + request.getHeader(name) + "</td>");
	    writer.println("</tr>");
	}*/
	    writer.println("<tr>");
	    writer.println("  <td>" + "Value supplied as Input param is " + identity+"</td>");
	    writer.println("</tr>");
	writer.println("</table>");

	writer.println("</body>");
	writer.println("</html>");

    }


}

