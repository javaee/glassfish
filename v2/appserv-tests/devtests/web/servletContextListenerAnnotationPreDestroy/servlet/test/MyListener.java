package test;

import java.io.*;
import javax.annotation.PreDestroy;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletContextEvent;

public class MyListener implements ServletContextListener {

    private ServletContext sc = null;

    /**
     * Receives notification that the web application initialization
     * process is starting.
     *
     * @param sce The servlet context event
     */
    public void contextInitialized(ServletContextEvent sce) {
        sc = sce.getServletContext();
        sc.log("contextInitialized");
    }

    /**
     * Receives notification that the servlet context is about to be shut down.
     *
     * @param sce The servlet context event
     */
    public void contextDestroyed(ServletContextEvent sce) {
        sc.log("contextDestroyed");
    }


    @PreDestroy
    public void myPreDestroy() {
        sc.log("contextDestroyed");
        try {
            FileOutputStream fos = new FileOutputStream("/tmp/mytest");
            OutputStreamWriter osw = new OutputStreamWriter(fos);
            osw.write("SUCCESS");
            osw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
