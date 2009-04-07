package test;

import java.io.*;
import java.util.*;
import javax.servlet.*;

public class MyListener implements ServletContextListener {

    /**
     * Receives notification that the web application initialization
     * process is starting.
     *
     * @param sce The servlet context event
     */
    public void contextInitialized(ServletContextEvent sce) {
        try {
            doContextInitialized(sce);
        } catch (Exception e) {
            sce.getServletContext().log("Error during contextInitialized");
            throw new RuntimeException(e);
        }
    }

    /**
     * Receives notification that the servlet context is about to be shut down.
     *
     * @param sce The servlet context event
     */
    public void contextDestroyed(ServletContextEvent sce) {
        // Do nothing
    }

    private void doContextInitialized(ServletContextEvent sce)
            throws Exception {

        ServletContext sc = sce.getServletContext();

        /*
         * Register servlet
         */
        ServletRegistration.Dynamic sr = sc.addServlet(
            "NewServlet", "test.NewServlet");
        sr.addMapping("/newServlet");
        sr.setAsyncSupported(true);

        /*
         * Register filter
         */
        FilterRegistration.Dynamic fr = sc.addFilter(
            "NewFilter", "test.NewFilter");
        fr.addMappingForServletNames(EnumSet.of(DispatcherType.REQUEST),
                                     true, "NewServlet"); 
        fr.setAsyncSupported(true);
    }
}
