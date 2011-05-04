/*
 * Servlet.java
 *
 * Created on December 16, 2002, 2:02 PM
 */

package client;

import javax.servlet.*;
import javax.naming.*;
import javax.servlet.http.*;
import javax.rmi.PortableRemoteObject;
import Data.SPSession;
import Data.SPSessionHome;

/**
 *
 * @author  mvatkina
 * @version
 */
public class Servlet extends HttpServlet {
    
    /** Initializes the servlet.
     */
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        
    }
    
    /** Destroys the servlet.
     */
    public void destroy() {
        
    }
    
    /** Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, java.io.IOException {
        response.setContentType("text/html");
        java.io.PrintWriter out = response.getWriter();
        
        out.println("<html>");
        out.println("<head>");
        out.println("<title>Servlet</title>");
        out.println("</head>");
        out.println("<body>");
        
        out.println("</body>");
        out.println("</html>");
        
        try {
            Context initial = new InitialContext();
            Object objref = initial.lookup("java:comp/env/ejb/SPSession");
            SPSessionHome home =
            (SPSessionHome)PortableRemoteObject.narrow(objref,
            SPSessionHome.class);
            
            SPSession myspsession = home.create();
            out.println("<pre>");
            
            myspsession.createPartsAndSuppliers();
            out.println("Created " + myspsession.checkAllParts() + " Parts.");
            out.println("Created " + myspsession.checkAllSuppliers() + " Suppliers.");
            
            out.println("Removing Part 200...");
            myspsession.removePart(new java.lang.Integer(200));
            
            out.println("Removing Supplier 145/145...");
            myspsession.removeSupplier(new java.lang.Integer(145), new java.lang.Integer(145));
            
            out.println("Left " + myspsession.checkAllParts() + " Parts.");
            out.println("Left " + myspsession.checkAllSuppliers() + " Suppliers.");
            
            out.println("</pre>");
            
        } catch (Exception ex) {
            System.err.println("Caught an exception:");
            ex.printStackTrace();
        }
        out.close();
    }
    
    /** Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, java.io.IOException {
        processRequest(request, response);
    }
    
    /** Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, java.io.IOException {
        processRequest(request, response);
    }
    
    /** Returns a short description of the servlet.
     */
    public String getServletInfo() {
        return "Short description";
    }
    
}
