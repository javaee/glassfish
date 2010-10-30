/*
 * Zombie.java
 *
 * Created on October 28, 2007, 8:29 PM
 */
package com.elf.enterprise.zombie;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

/**
 *
 * @author bnevins
 * @version
 */
public class Zombie extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        ServletContext ctx = getServletContext();

        if (ctx.getAttribute("undead") == null) {
            ctx.setAttribute("undead", true);
            System.out.println("GlassFish will become a ZOMBIE when an orderly shutdown is attempted.");
            Runtime.getRuntime().addShutdownHook(new Thread() {

                @Override
                public void run() {
                    System.exit(0);
                }
            });
        }
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
        return "Turn AppServer into a Zombie at shutdown time!";
    }
    // </editor-fold>
}
