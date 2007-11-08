package test;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionAttributeListener;
import javax.servlet.http.HttpSessionBindingEvent;

import javax.naming.*;
import javax.annotation.Resource;
import javax.annotation.Resources;
import javax.sql.DataSource;

@Resource(name="myDataSource4", type=DataSource.class)
@Resources({ @Resource(name="myDataSource5", type=DataSource.class),
             @Resource(name="jdbc/myDataSource6", type=DataSource.class) })

public class MyListener implements HttpSessionAttributeListener {

    private @Resource DataSource ds1;
    private @Resource(name="myDataSource2") DataSource ds2;
    private DataSource ds3;

    @Resource(name="jdbc/myDataSource3")
    private void setDataSource(DataSource ds) {
        ds3 = ds;
    }

    /**
     * Receives notification that an attribute has been added to a
     * session.
     *
     * @param hsbe The HttpSessionBindingEvent
     */
    public void attributeAdded(HttpSessionBindingEvent hsbe) {

        HttpSession session = hsbe.getSession();
        ServletContext sc = session.getServletContext();
    
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

            session.setAttribute("success", new Object());

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Receives notification that an attribute has been removed from a
     * session.
     *
     * @param hsbe The HttpSessionBindingEvent
     */
    public void attributeRemoved(HttpSessionBindingEvent hsbe) {
        // Do nothing
    }

    /**
     * Receives notification that an attribute has been replaced in a
     * session.
     *
     * @param hsbe The HttpSessionBindingEvent
     */
    public void attributeReplaced(HttpSessionBindingEvent se) {
        // Do nothing
    }
}
