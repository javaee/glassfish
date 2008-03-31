package myapp;

import java.io.*;
import java.lang.reflect.Constructor;
import java.net.*;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.sql.DataSource;
import java.util.Map;
import javax.annotation.Resource;
import myapp.test.SimpleTest;
import myapp.util.HtmlUtil;

public class MyServlet extends HttpServlet {

    @Resource(name = "jdbc/__default", mappedName = "jdbc/__default")
    DataSource ds1;

    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        Boolean pass = false;
        SimpleTest[] tests = null;
        StringBuffer buf = null;
        Boolean  notestcase = false;
        String testcase = request.getParameter("testcase");
        System.out.println("testcase="+testcase);
        if (testcase != null) {

          out.println("<html>");
          out.println("<head>");
          out.println("<title>Servlet MyServlet</title>");
          out.println("</head>");
          out.println("<body>");
          out.println("<h1>Servlet MyServlet at " + request.getContextPath() + "</h1>");

          buf = new StringBuffer();

          try {
	    if ("usertx".equals(testcase)) {
                 tests = initializeUserTxTest();
	    } else if ("noleak".equals(testcase)) {
                 tests = initializeLeakTest();
	    }
          } catch (Exception e) {
               HtmlUtil.printException(e, out);
          }

          try {
               buf.append("Test Name:Pass<br>");
               for (SimpleTest test : tests) {
                  Map<String, Boolean> map = test.runTest(ds1, out);
                   for (Map.Entry entry : map.entrySet()) {
                      buf.append(entry.getKey());
                      buf.append(":");
                      buf.append(entry.getValue());
                      buf.append("<br>");
                  }
               }
               out.println(buf.toString());
            } catch (Throwable e) {
               out.println("got outer excpetion");
               out.println(e);
               e.printStackTrace();
            } finally {
               out.println("</body>");
               out.println("</html>");
               out.close();
               out.flush();
           }
	}
    }

    private SimpleTest[] initializeUserTxTest() throws Exception {
        String[] tests = {
            "myapp.test.UserTxTest"
        };

        SimpleTest[] testInstances = new SimpleTest[tests.length];
        for (int i = 0; i < tests.length; i++) {
            Class testClass = Class.forName(tests[i]);
            Constructor c = testClass.getConstructor();
            testInstances[i] = (SimpleTest) c.newInstance();
        }
        return testInstances;
    }

    private SimpleTest[] initializeLeakTest() throws Exception {
        String[] tests = {
            "myapp.test.LeakTest"
        };

        SimpleTest[] testInstances = new SimpleTest[tests.length];
        for (int i = 0; i < tests.length; i++) {
            Class testClass = Class.forName(tests[i]);
            Constructor c = testClass.getConstructor();
            testInstances[i] = (SimpleTest) c.newInstance();
        }
        return testInstances;
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
        return "Short description";
    }
    // </editor-fold>
}
