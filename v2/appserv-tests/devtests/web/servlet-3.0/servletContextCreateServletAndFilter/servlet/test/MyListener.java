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
        Class<NewServlet> servletCl = (Class<NewServlet>)
            Class.forName("test.NewServlet");
        NewServlet servlet = sc.createServlet(servletCl);
        servlet.setMyParameter("myServletParamValue");
        ServletRegistration sr = sc.addServlet("NewServlet", servlet);
        sr.setInitParameter("servletInitParamName", "servletInitParamValue");
        sr.addMapping("/newServlet");

        /*
         * Register filter
         */
        Class<NewFilter> filterCl = (Class<NewFilter>)
            Class.forName("test.NewFilter");
        NewFilter filter = sc.createFilter(filterCl);
        filter.setMyParameter("myFilterParamValue");
        FilterRegistration fr = sc.addFilter("NewFilter", filter);
        fr.setInitParameter("filterInitParamName", "filterInitParamValue");
        fr.addMappingForServletNames(EnumSet.of(DispatcherType.REQUEST),
                                     true, "NewServlet"); 
    }
}
