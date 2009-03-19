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
        ServletContext sc = sce.getServletContext();

        /*
         * Register servlet
         */
        ServletRegistration sr = sc.addServlet("NewServlet", "test.NewServlet");
        sr.setInitParameter("servletInitName", "servletInitValue");
        sr.addMapping("/newServlet");

        /*
         * Register filter
         */
        FilterRegistration fr = sc.addFilter("NewFilter", "test.NewFilter");
        fr.setInitParameter("filterInitName", "filterInitValue");
        fr.addMappingForServletNames(EnumSet.of(DispatcherType.REQUEST),
                                     true, "NewServlet"); 
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
