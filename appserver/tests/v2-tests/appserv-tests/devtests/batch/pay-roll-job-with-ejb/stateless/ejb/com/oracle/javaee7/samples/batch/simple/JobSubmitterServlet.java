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
