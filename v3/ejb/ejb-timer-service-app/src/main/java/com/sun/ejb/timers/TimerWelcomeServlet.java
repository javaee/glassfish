/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.ejb.timers;

import java.io.*;
import java.net.*;
import java.util.Set;

import javax.servlet.*;
import javax.ejb.*;
import javax.servlet.http.*;

import com.sun.ejb.containers.TimerLocal;

/**
 *
 * @author mvatkina
 */

public class TimerWelcomeServlet extends HttpServlet {

    @EJB
    TimerLocal timer;

    /** 
    * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
    * @param request servlet request
    * @param response servlet response
    */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        try {
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Timer Application</title>");  
            out.println("</head>");
            out.println("<body>");
            out.println("<h3>Welcome to Timer Application</h3>");
            out.println("<br>");

            // Report timers
            Set persistenttimers = timer.findActiveTimersOwnedByThisServer();
            Set nonpersistenttimers = timer.findActiveNonPersistentTimersOwnedByThisServer();
            int persistentsize = persistenttimers.size();
            int nonpersistentsize = nonpersistenttimers.size();

            out.println("There " + ((persistentsize == 1)? "is " : "are  ") 
                    + persistentsize
                    + " active persistent timer" + ((persistentsize == 1)? "" : "s")
                    + " on this container");
            out.println("<br>");
            out.println("There " + ((nonpersistentsize == 1)? "is " : "are  ") 
                    + nonpersistentsize
                    + " active non-persistent timer" + ((nonpersistentsize == 1)? "" : "s")
                    + " on this container");
            out.println("<br>");

        }catch(Throwable e){
            out.println("Problem accessing timers... ");
            out.println(e);
            e.printStackTrace();
        }
        finally {
            out.println("</body>");
            out.println("</html>");
            
            out.close();
            out.flush();

        }
    }


    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /** 
    * Handles the HTTP <code>GET</code> method.
    * @param request servlet request
    * @param response servlet response
    */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        processRequest(request, response);
    } 

    /** 
    * Handles the HTTP <code>POST</code> method.
    * @param request servlet request
    * @param response servlet response
    */
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        processRequest(request, response);
    }

    /** 
    * Returns a short description of the servlet.
    */
    public String getServletInfo() {
        return "Timer Application Servlet";
    }
    // </editor-fold>
}
