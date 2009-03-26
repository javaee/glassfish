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

public class NewServlet extends HttpServlet {

    private ServletContext sc;

    private @Resource DataSource ds1;
    private @Resource(name="myDataSource2") DataSource ds2;
    private DataSource ds3;

    @Resource(name="jdbc/myDataSource3")
    private void setDataSource(DataSource ds) {
        ds3 = ds;
    }

    private @Resource String welcomeMessage;

    private String initParamValue;
    private String myParamValue;

    public void setMyParameter(String value) {
        myParamValue = value;
    }

    public void init() throws ServletException {

        initParamValue = getServletConfig().getInitParameter(
            "servletInitParamName");

        sc = getServletContext();

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
                throw new Exception("welcomeMessage not injected!");
            }

            sc.setAttribute("success", new Object());

        } catch (Throwable t) {
            sc.log("Error during init", t);
            throw new ServletException(t);
        }
    }

    public void doGet(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {
        if (!"myServletParamValue".equals(myParamValue)) {
            throw new ServletException("Wrong servlet instance");
        }

        if (!"servletInitParamValue".equals(initParamValue)) {
            throw new ServletException("Missing servlet init param");
        }

        if (!"myFilterParamValue".equals(
                req.getAttribute("myFilterParamName"))) {
            throw new ServletException("Wrong filter instance");
        }

        if (!"filterInitParamValue".equals(
                req.getAttribute("filterInitParamName"))) {
            throw new ServletException("Missing filter init param");
        }

        if (sc.getAttribute("success") == null) {
            throw new ServletException("Missing ServletContext attribute");
        }
    }
}
