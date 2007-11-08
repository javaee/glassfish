package test;

import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.ServletRequestListener;
import javax.servlet.ServletRequestEvent;

import javax.naming.*;
import javax.annotation.Resource;
import javax.annotation.Resources;
import javax.sql.DataSource;

@Resource(name="myDataSource4", type=DataSource.class)
@Resources({ @Resource(name="myDataSource5", type=DataSource.class),
             @Resource(name="jdbc/myDataSource6", type=DataSource.class) })

public class MyListener implements ServletRequestListener {

    private @Resource DataSource ds1;
    private @Resource(name="myDataSource2") DataSource ds2;
    private DataSource ds3;

    @Resource(name="jdbc/myDataSource3")
    private void setDataSource(DataSource ds) {
        ds3 = ds;
    }

    /**
     * Receives notification that a request is about to enter the scope
     * of the web application.
     *
     * @param sre The ServletRequestEvent
     */
    public void requestInitialized(ServletRequestEvent sre) {

        ServletRequest sr = sre.getServletRequest();
        ServletContext sc = sre.getServletContext();
    
        try {

            int loginTimeout = ds1.getLoginTimeout();
            sc.log("ds1-login-timeout=" + loginTimeout);
            loginTimeout = ds2.getLoginTimeout();
            sc.log(",ds2-login-timeout=" + loginTimeout);
            loginTimeout = ds3.getLoginTimeout();
            sc.log(",ds3-login-timeout=" + loginTimeout);

            InitialContext ic = new InitialContext();

            DataSource ds4 = (DataSource)
                ic.lookup("java:comp/env/myDataSource4");
            loginTimeout = ds4.getLoginTimeout();
            sc.log(",ds4-login-timeout=" + loginTimeout);

            DataSource ds5 = (DataSource)
                ic.lookup("java:comp/env/myDataSource5");
            loginTimeout = ds5.getLoginTimeout();
            sc.log(",ds5-login-timeout=" + loginTimeout);

            DataSource ds6 = (DataSource)
                ic.lookup("java:comp/env/jdbc/myDataSource6");
            loginTimeout = ds6.getLoginTimeout();
            sc.log(",ds6-login-timeout=" + loginTimeout);

            sr.setAttribute("success", new Object());

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Receives notification that a request is about to leave the scope
     * of the web application.
     *
     * @param sre The ServletRequestEvent
     */
    public void requestDestroyed(ServletRequestEvent sre) {
        // Do nothing
    }

}
