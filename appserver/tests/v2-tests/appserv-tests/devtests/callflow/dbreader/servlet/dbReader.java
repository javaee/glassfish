/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
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

/*
 * dbReader.java
 *
 * Created on May 10, 2006, 10:31 AM
 */

package com.sun.enterprise.admin.monitor.callflow;

import java.io.*;
import java.net.*;
import java.sql.*;
import javax.annotation.Resource;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.sql.DataSource;
import com.sun.enterprise.admin.monitor.callflow.Agent;
import com.sun.enterprise.Switch;

/**
 *
 * DB Reader Utility Servlet. Returns the Rows in each callflow table.
 * @author Harpreet Singh
 * @version
 */
public class dbReader extends HttpServlet {
    
    /** Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     */
    @Resource (name="jdbc/__CallFlowPool", mappedName="jdbc/__CallFlowPool")
    private DataSource callflowDS;
    private static final String __COUNT = " select count(*) from ";
    private static final String __SELECT = " select count(*) from  ";
    private static final String __WHERE = " where ";
    private static final String __DELETE =" delete from ";
    private static final String REQUEST_START_TBL__SERVER = "REQUEST_START_TBL__SERVER";
    private static final String RS = REQUEST_START_TBL__SERVER;
    
    private static final String REQUEST_END_TBL__SERVER = "REQUEST_END_TBL__SERVER";
    private static final String RE = REQUEST_END_TBL__SERVER;
    
    private static final String METHOD_START_TBL__SERVER = "METHOD_START_TBL__SERVER";
    private static final String MS = METHOD_START_TBL__SERVER;
    
    private static final String METHOD_END_TBL__SERVER ="METHOD_END_TBL__SERVER";
    private static final String ME = METHOD_END_TBL__SERVER;
    
    private static final String START_TIME_TBL__SERVER ="START_TIME_TBL__SERVER";
    private static final String ST = START_TIME_TBL__SERVER;
    
    private static final String END_TIME_TBL__SERVER   ="END_TIME_TBL__SERVER";
    private static final String ET = END_TIME_TBL__SERVER;
    
    private static final String AND = " AND ";
    private static final String RID = ".REQUEST_ID ";
    private static final String COMMA = ",";
    private static final String EQUAL = " = ";
    
   // name of the servlet whose db value is to be queried
    private static final String SERVLET_NAME = "";
   
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        Connection con = null;
        con = getConnection();
        String cleandb = request.getParameter ("cleandb");
        if (cleandb != null){
            deleteRows (con);
	    out.println ("Cleaned rows from all tables in db");
            return;
        }
        String servletName = request.getParameter ("servletName");        
        if (servletName == null){
            out.println (" Specify servlet name whose data is to be looked up"+
                    " via a request parameter name ="+servletName);
        }
//        out.println (" Looking up information for "+ servletName);
//        out.println();
        String sql = __COUNT + MS +  COMMA + ME + COMMA + RS + COMMA + RE +
             __WHERE + MS +".MODULE_NAME = " + "'" + "/"+ servletName + "'" + AND +
                MS + RID + EQUAL + ME + RID + AND +
                MS + RID + EQUAL + RS + RID + AND +
                MS + RID + EQUAL + RE + RID;
//        out.println (" \nSQL : "+sql);
        int val = getCountCallFlowRows (con, sql);
        String sql2 = __COUNT + MS +  COMMA + ET +
             __WHERE + MS +".MODULE_NAME = " + "'" + "/" +servletName + "'" + AND +
                MS + RID + EQUAL + ET + RID;
        int et = getCountCallFlowRows (con, sql2);
        String sql3 = __COUNT + MS +  COMMA + ST +
             __WHERE + MS +".MODULE_NAME = " + "'" + "/"+ servletName + "'" + AND +
                MS + RID + EQUAL + ST + RID;

        int st = getCountCallFlowRows (con, sql3);
        out.println ("RS=RE=MS=ME="+ val +" ST="+st+" ET="+et);
//        out.println (" Cleaning up Database");
////        deleteRows (con);
        out.close();
        try {
            con.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
     
    private Connection getConnection () throws ServletException{
    
        Connection con = null;
        try {
            con = callflowDS.getConnection();
        } catch (SQLException ex) {
           throw new ServletException (ex);
        } 
        return con;
    }
    private String getRequestId (Connection con , String sql) throws ServletException {
        String result = null;
        try {
            Statement stmt = con.createStatement();
            stmt.executeQuery (sql);
            ResultSet set = stmt.getResultSet();
            set.next();
            result = set.getString(1);
        } catch (SQLException ex) {
            throw new ServletException (ex);
        }
        return result;
    }
    private int getCountCallFlowRows (Connection con, String sql) throws ServletException{
        int count = -1;
        try {
            Statement stmt = con.createStatement();
            stmt.executeQuery (sql);
            ResultSet set = stmt.getResultSet();
            set.next();
            count = set.getInt(1);
        } catch (SQLException ex) {
            throw new ServletException (ex);
        }
        return count;
        
    }
  private void deleteRows (Connection con) throws ServletException{
        try {
            Statement stmt = con.createStatement();
            stmt.execute (__DELETE + REQUEST_START_TBL__SERVER);            
            stmt.execute (__DELETE + REQUEST_END_TBL__SERVER);
            
            stmt.execute (__DELETE + METHOD_START_TBL__SERVER);
            stmt.execute (__DELETE + METHOD_END_TBL__SERVER);
            
            stmt.execute (__DELETE + START_TIME_TBL__SERVER);
            stmt.execute (__DELETE + END_TIME_TBL__SERVER);
            
        } catch (SQLException ex) {
            throw new ServletException (ex);
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
        return "Short description";
    }
    // </editor-fold>
}
