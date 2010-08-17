package servlet;


import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.ejb.CreateException;
import java.io.IOException;
import java.io.PrintWriter;

import com.sun.s1asdev.connector.installed_libraries_test.ejb.SimpleSessionHome;
import com.sun.s1asdev.connector.installed_libraries_test.ejb.SimpleSession;

public class SimpleServlet extends HttpServlet {


    public void doGet (HttpServletRequest request, HttpServletResponse response)
          throws ServletException, IOException {
      doPost(request, response);
    }

    /** handles the HTTP POST operation **/
    public void doPost (HttpServletRequest request,HttpServletResponse response)
          throws ServletException, IOException {
        doTest(request, response);
    }

    public void doTest(HttpServletRequest request, HttpServletResponse response) throws IOException{
        PrintWriter out = response.getWriter();

        try{
        System.out.println("serializable connector test");

        //SimpleReporterAdapter stat = new SimpleReporterAdapter();
        //String testSuite = "serializable connector test";

        InitialContext ic = new InitialContext();
        SimpleSessionHome simpleSessionHome = (SimpleSessionHome) ic.lookup("java:comp/env/ejb/SimpleSessionEJB");
        out.println("Running serializable connector test ");

        //stat.addDescription("Running serializable connector test ");
        SimpleSession bean = simpleSessionHome.create();

        try {
            if (!bean.test1()) {
                //stat.addStatus(testSuite + " test1 :  ", stat.FAIL);
                out.println("TEST:FAIL");
            } else {
                //stat.addStatus(testSuite + " test1 :  ", stat.PASS);
                out.println("TEST:PASS");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        //stat.printSummary();
        }catch(NamingException ne){
            ne.printStackTrace();
        } catch (CreateException e) {
            e.printStackTrace();  
        }finally{
            out.println("END_OF_TEST");
            out.flush();
        }
    }
}
