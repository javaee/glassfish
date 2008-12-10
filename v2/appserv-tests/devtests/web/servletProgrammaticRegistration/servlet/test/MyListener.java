package test;

import java.io.*;
import javax.servlet.*;

public class MyListener implements ServletContextListener {

    /**
     * Receives notification that the web application initialization
     * process is starting.
     *
     * @param sce The servlet context event
     */
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext sc = sce.getServletContext();
        ServletRegistration regis = sc.addServlet("NewServlet",
                                                  "test.NewServlet");
        regis.setInitParameter("myInitName", "myInitValue");

        sc.addServletMapping("NewServlet", new String[] {"/newServlet"});
    }

    /**
     * Receives notification that the servlet context is about to be shut down.
     *
     * @param sce The servlet context event
     */
    public void contextDestroyed(ServletContextEvent sce) {
        // Do nothing
    }

}
