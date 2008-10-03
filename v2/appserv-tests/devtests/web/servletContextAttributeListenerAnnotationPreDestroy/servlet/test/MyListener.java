package test;

import java.io.*;
import javax.annotation.PreDestroy;
import javax.servlet.*;

public class MyListener implements ServletContextAttributeListener {

    public void attributeAdded(ServletContextAttributeEvent scab) {
        // Do nothing
    }

    public void attributeRemoved(ServletContextAttributeEvent scab) {
        // Do nothing
    }

    public void attributeReplaced(ServletContextAttributeEvent scab) {
        // Do nothing
    }

    @PreDestroy
    public void myPreDestroy() {
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
