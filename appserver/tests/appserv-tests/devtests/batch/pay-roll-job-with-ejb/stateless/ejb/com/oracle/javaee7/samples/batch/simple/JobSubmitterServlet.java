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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oracle.javaee7.samples.batch.simple;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import javax.annotation.Resource;
import javax.batch.operations.JobOperator;
import javax.batch.runtime.BatchRuntime;
import javax.batch.runtime.JobExecution;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.batch.runtime.context.BatchContext;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.inject.Inject;

/**
 *
 * @author makannan
 */
public class JobSubmitterServlet extends HttpServlet {
    
//    @Resource(name="concurrent/batch-executor-service")
//    ManagedExecutorService managedExecutorService;
    
    /**
     * Processes requests for both HTTP
     * <code>GET</code> and
     * <code>POST</code> methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        try {
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet JobSubmitterServlet V2</title>");
            out.println("</head>");
            out.println("<body>");
//                out.println("<h1>ManagedExecutorService: " + managedExecutorService + "</h1>");
                out.println("<h1>Servlet JobSubmitterServlet Version 2 at " + request.getContextPath() + "</h1>");
            out.println("<form method=\"POST\" >");
            out.println("<table>");
            out.println("<tr><td>Submit Job From XML</td><td><input type=\"submit\" name=\"submitJobFromXML\" value=\"Submit Job From XML\"/></td></tr>");
            out.println("<tr><td>List Jobs</td><td><input type=\"submit\" name=\"listJobs\" value=\"List Job\"/></td></tr>");
            if (request.getParameter("submitJobFromXML") != null) {
                submitJobFromXML(out);
            } else if (request.getParameter("listJobs") != null) {
                out.println("<tr><td>listJobs</td><td>Not implemented yet</td></tr>");
            } 


            out.println("</table>");
            out.println("</form>");
            out.println("</body>");
            out.println("</html>");
        } catch (Exception ex) {
            throw new ServletException(ex);
        } finally {
            out.close();
        }
    }

    private void submitJobFromXML(PrintWriter pw)
            throws Exception {
        
        JobOperator jobOperator = BatchRuntime.getJobOperator();
        pw.println("<tr><td>JobOperator class </td><td>" + jobOperator.getClass().getName() + " </td></tr>");

        Properties props = new Properties();
        for (int i=0; i<9; i++)
            props.put(i, i);
        Long id = jobOperator.start("PayRollJob", props);
        
        pw.println(jobInfo(jobOperator, id, pw));
        
    }
    
    //getJobNames()
    private String jobNames(JobOperator jobOperator) {
        StringBuilder sb = new StringBuilder("JobOperator.jobNames: ");
        for (String j : jobOperator.getJobNames())
            sb.append(" ").append(j);
        return sb.toString();
    }
    
    private String jobInfo(JobOperator jobOperator, long id, PrintWriter pw) {
        StringBuilder sb = new StringBuilder("JobOperator.jobNames: ");
        sb.append("<tr><td>Id</td><td" + id + "</td></tr>");
        JobExecution je = jobOperator.getJobExecution(id);
        sb.append("<tr><td>jobParams</td><td>" + je.getJobParameters() +" </td></tr>");
        sb.append(asString(jobOperator.getJobExecution(id)));
        return sb.toString();
    }
    
    private String asString(JobExecution je) {
        StringBuilder sb = new StringBuilder("JobExecution: ");
        sb.append("<tr><td>=>    createTime</td><td>").append(je.getCreateTime()).append("</td></tr>");
        sb.append("<tr><td>=>    endTime: </td><td>").append(je.getEndTime()).append("</td></tr>");
        sb.append("<tr><td>=>    executionId: </td><td>").append(je.getExecutionId()).append("</td></tr>");
        sb.append("<tr><td>=>    exitStatus: </td><td>").append(je.getExitStatus()).append("</td></tr>");
        sb.append("<tr><td>=>    instanceId: </td><td>").append(je.getInstanceId()).append("</td></tr>");
        sb.append("<tr><td>=>    jobParameters: </td><td>").append(je.getJobParameters()).append("</td></tr>");
        sb.append("<tr><td>=>    lastUpdatedTime: </td><td>").append(je.getLastUpdatedTime()).append("</td></tr>");
        sb.append("<tr><td>=>    status: </td><td>").append(je.getExitStatus()).append("</td></tr>");
        
        return sb.toString();
    }
    
    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP
     * <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP
     * <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>
}
