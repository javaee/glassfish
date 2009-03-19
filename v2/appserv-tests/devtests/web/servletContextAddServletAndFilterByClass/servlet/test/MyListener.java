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
        } catch (ClassNotFoundException e) {
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
            throws ClassNotFoundException {

        ServletContext sc = sce.getServletContext();

        /*
         * Register servlet
         */
        Servlet servlet = new NewServlet();
        ServletRegistration sr = sc.addServlet("NewServlet", servlet);
        sr.setInitParameter("servletInitParamName", "servletInitParamValue");
        sr.addMapping("/newServlet");

        /*
         * Make sure that if we register a different servlet instance
         * under the same name, null is returned
         */
        if (sc.addServlet("NewServlet", new NewServlet()) != null) {
            throw new RuntimeException(
                "Duplicate servlet name not detected by " +
                "ServletContext#addServlet");
        }

        /*
         * Make sure that if we register the same servlet instance again
         * (under a different name), null is returned
         */
        if (sc.addServlet("AgainServlet", servlet) != null) {
            throw new RuntimeException(
                "Duplicate servlet instance not detected by " +
                "ServletContext#addServlet");
        }

        /*
         * Register filter
         */
        Filter filter = new NewFilter();
        FilterRegistration fr = sc.addFilter("NewFilter", filter);
        fr.setInitParameter("filterInitParamName", "filterInitParamValue");
        fr.addMappingForServletNames(EnumSet.of(DispatcherType.REQUEST),
                                     true, "NewServlet"); 

        /*
         * Make sure that if we register a different filter instance
         * under the same name, null is returned
         */
        if (sc.addFilter("NewFilter", new NewFilter()) != null) {
            throw new RuntimeException(
                "Duplicate filter name not detected by " +
                "ServletContext#addFilter");
        }

        /*
         * Make sure that if we register the same filter instance again
         * (under a different name), null is returned
         */
        if (sc.addFilter("AgainFilter", filter) != null) {
            throw new RuntimeException(
                "Duplicate filter instance not detected by " +
                "ServletContext#addFilter");
        }
    }
}
