/*
 * MyProtectedServlet.java
 *
 * Created on April 24, 2005, 2:14 AM
 */

import java.io.*;
import java.net.*;

import javax.servlet.*;
import javax.servlet.http.*;

/**
 *
 * @author lm115986
 * @version
 */
public class MyProtectedServlet extends HttpServlet {
    
    /** Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        
        System.out.println("MyProtectedServlet.processRequest " + request.getRequestURI() + " " + request.getQueryString());
        
        String myUrl = request.getRequestURI();
        if(myUrl.indexOf("login") >= 0) {
            login(request, response);
            return;
        } else if(myUrl.indexOf("redirect") >= 0) {
            redirect(request, response);
            return;
        }
        
        if(request.getRemoteUser() == null) {
            String callUrl = request.getRequestURI();
            String query = request.getQueryString();
            if(query != null) {
                callUrl = callUrl + "?" + query;
            }
            String nextEncUrl = java.net.URLEncoder.encode(callUrl);
            String redirectUrl = request.getContextPath() + "/application/redirect?nextencurl=" + nextEncUrl;
            response.sendRedirect(redirectUrl);
        } else {
            response.setContentType("text/html");
            PrintWriter out = response.getWriter();
            
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet MyProtectedServlet</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet MyProtectedServlet at " + request.getContextPath() + "</h1>");
            out.println("</body>");
            out.println("</html>");
            
            out.close();
        }
    }
    
    private void redirect(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
  
        response.sendRedirect(request.getParameter("nextencurl"));
    }
    
     protected void login(HttpServletRequest request, HttpServletResponse response)
     throws ServletException, IOException {

        request.getRequestDispatcher("/login.jsp").forward(request, response);
    }   
    
    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /** Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        processRequest(request, response);
    }
    
    /** Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        processRequest(request, response);
    }
    
    /** Returns a short description of the servlet.
     */
    public String getServletInfo() {
        return "Short description";
    }
    // </editor-fold>
}
