package test;

import java.io.*;
import java.util.*;
import javax.servlet.*;

public class ConfigListener implements ServletContextListener {

    /**
     * Receives notification that the web application initialization
     * process is starting.
     *
     * @param sce The servlet context event
     */
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext sc = sce.getServletContext();
        SessionCookieConfig scc = new SessionCookieConfig(
            null, null, null, false, false, "MYJSESSIONID");
        sc.setSessionCookieConfig(scc);
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
