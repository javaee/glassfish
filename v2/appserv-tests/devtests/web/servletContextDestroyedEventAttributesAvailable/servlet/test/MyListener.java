package test;

import java.io.*;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletContextEvent;

public class MyListener implements ServletContextListener {

    /**
     * Receives notification that the web application initialization
     * process is starting.
     *
     * @param sce The servlet context event
     */
    public void contextInitialized(ServletContextEvent sce) {
        sce.getServletContext().log("contextInitialized");
    }

    /**
     * Receives notification that the servlet context is about to be shut down.
     *
     * @param sce The servlet context event
     */
    public void contextDestroyed(ServletContextEvent sce) {

        ServletContext sc = sce.getServletContext();
        sc.log("contextDestroyed");

        String status = "FAIL";
        String attrValue = (String) sc.getAttribute("myName");
        if ("myValue".equals(attrValue)) {
            status = "SUCCESS";
        }

        try {
            FileOutputStream fos = new FileOutputStream("/tmp/mytest");
            OutputStreamWriter osw = new OutputStreamWriter(fos);
            osw.write(status);
            osw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
