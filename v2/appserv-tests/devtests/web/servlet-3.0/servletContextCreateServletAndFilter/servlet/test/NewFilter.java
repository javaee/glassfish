package test;

import java.io.*;
import javax.servlet.*;
import javax.naming.*;
import javax.annotation.*;
import javax.sql.DataSource;

@Resource(name="myDataSource4", type=DataSource.class)
@Resources({ @Resource(name="myDataSource5", type=DataSource.class),
             @Resource(name="jdbc/myDataSource6", type=DataSource.class) })

public class NewFilter implements Filter {

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

    public void init(FilterConfig config) throws ServletException {

        initParamValue = config.getInitParameter("filterInitParamName");

        ServletContext sc = config.getServletContext();
    
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

        } catch (Exception e) {
            sc.log(e, "Error during init");
            throw new ServletException(e);
        }
    }

    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain)
            throws IOException, ServletException {
        request.setAttribute("filterInitParamName", initParamValue);
        request.setAttribute("myFilterParamName", myParamValue);
        chain.doFilter(request, response);
    }

    public void destroy() {
        // do nothing
    }
}
