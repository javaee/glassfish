package test;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

import javax.naming.*;
import javax.annotation.Resource;
import javax.annotation.Resources;
import javax.sql.DataSource;

@Resource(name="myDataSource4", type=DataSource.class)
@Resources({ @Resource(name="myDataSource5", type=DataSource.class),
             @Resource(name="jdbc/myDataSource6", type=DataSource.class) })

public class TestServlet extends HttpServlet {

    private ServletContext sc;

    private @Resource DataSource ds1;
    private @Resource(name="myDataSource2") DataSource ds2;
    private DataSource ds3;

    @Resource(name="jdbc/myDataSource3")
    private void setDataSource(DataSource ds) {
        ds3 = ds;
    }

    private @Resource String welcomeMessage;

    public void init(ServletConfig config) throws ServletException {

        sc = config.getServletContext();

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

            if (!"Hello World from env-entry!".equals(welcomeMessage)) {
                throw new RuntimeException("welcomeMessage not injected!");
            }

            sc.setAttribute("success", new Object());

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void doGet(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {

        boolean passed = false;
        Object success = sc.getAttribute("success");
        if (success != null) {
            passed = true;
        }

        res.getWriter().print(passed);
    }
}
