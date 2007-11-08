package taglib;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.SimpleTagSupport;

import javax.naming.*;
import javax.annotation.Resource;
import javax.annotation.Resources;
import javax.sql.DataSource;

@Resource(name="myDataSource4", type=DataSource.class)
@Resources({ @Resource(name="myDataSource5", type=DataSource.class),
             @Resource(name="jdbc/myDataSource6", type=DataSource.class) })

public class MySimpleTag extends SimpleTagSupport {

    private @Resource DataSource ds1;
    private @Resource(name="myDataSource2") DataSource ds2;
    private DataSource ds3;

    @Resource(name="jdbc/myDataSource3")
    private void setDataSource(DataSource ds) {
        ds3 = ds;
    }

    public void doTag() throws JspException, IOException {

        try {

            JspWriter jsw = getJspContext().getOut();

            int loginTimeout = ds1.getLoginTimeout();
            jsw.write("ds1-login-timeout=" + loginTimeout);
            loginTimeout = ds2.getLoginTimeout();
            jsw.write(",ds2-login-timeout=" + loginTimeout);
            loginTimeout = ds3.getLoginTimeout();
            jsw.write(",ds3-login-timeout=" + loginTimeout);

            InitialContext ic = new InitialContext();

            DataSource ds4 = (DataSource)
                ic.lookup("java:comp/env/myDataSource4");
            loginTimeout = ds4.getLoginTimeout();
            jsw.write(",ds4-login-timeout=" + loginTimeout);

            DataSource ds5 = (DataSource)
                ic.lookup("java:comp/env/myDataSource5");
            loginTimeout = ds5.getLoginTimeout();
            jsw.write(",ds5-login-timeout=" + loginTimeout);

            DataSource ds6 = (DataSource)
                ic.lookup("java:comp/env/jdbc/myDataSource6");
            loginTimeout = ds6.getLoginTimeout();
            jsw.write(",ds6-login-timeout=" + loginTimeout);

        } catch (Exception ex) {
            ex.printStackTrace();
            throw new JspException(ex);
        } 
    }
}
