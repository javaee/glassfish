package samples.ejb.serializabletest.servlet;

import com.sun.s1asdev.connector.serializabletest.ejb.SimpleSessionHome;
import com.sun.s1asdev.connector.serializabletest.ejb.SimpleSession;

import java.io.*;
import javax.servlet.*;
import javax.naming.*;
import javax.servlet.http.*;


public class SimpleServlet extends HttpServlet {


    InitialContext initContext = null;


    public void init() {
    }


    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doPost(request, response);
    }

    /**
     * handles the HTTP POST operation *
     */
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        PrintWriter out = response.getWriter();

        try {
            String testId = "serializable connector test";

            response.setContentType("text/html");

            InitialContext ic = new InitialContext();
            SimpleSessionHome simpleSessionHome = (SimpleSessionHome) ic.lookup("java:comp/env/ejb/simpleSession");

            out.println("Running serializable connector test ");
            SimpleSession bean = simpleSessionHome.create();

            boolean passed = false;

            try {
                if (!bean.test1()) {
                    out.println(testId + " test1 :  " + " FAIL");
                    out.println("TEST:FAIL");
                } else {
                    out.println(testId + " test1 :  " + " PASS");
                    out.println("TEST:PASS");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch(Exception e){

        } finally {
            out.println("END_OF_TEST");
        }
    }
}
