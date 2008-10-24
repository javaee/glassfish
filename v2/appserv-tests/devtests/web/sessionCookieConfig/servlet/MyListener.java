import java.io.*;
import java.net.*;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletContextEvent;
import javax.servlet.SessionCookieConfig;

public class MyListener implements ServletContextListener {

    /*
     * Cookie path.
     */
    protected String COOKIE_PATH = "/";

    /*
     * Cookie domain.
     */
    protected String COOKIE_DOMAIN = ".iplanet.com";

    /*
     * Cookie comment.
     */
    protected String COOKIE_COMMENT
        =  URLEncoder.encode("Sun-Java-System/Application-Server-PE-8.0 Session Tracking Cookie");

    /**
     * Receives notification that the web application initialization
     * process is starting.
     *
     * @param sce The servlet context event
     */
    public void contextInitialized(ServletContextEvent sce) {

        ServletContext servletContext = sce.getServletContext();
        SessionCookieConfig sessionCookieConfig =
                new SessionCookieConfig(COOKIE_DOMAIN, COOKIE_PATH, COOKIE_COMMENT, true, true);
        servletContext.setSessionCookieConfig(sessionCookieConfig);

    }


    /**
     * Receives notification that the servlet context is about to be shut down.
     *
     * @param sce The servlet context event
     */
    public void contextDestroyed(ServletContextEvent sce) {

        ServletContext sc = sce.getServletContext();
        sc.log("contextDestroyed");

    }
}

