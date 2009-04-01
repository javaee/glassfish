package taglib;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import javax.annotation.Resource;
import javax.annotation.PostConstruct;
import javax.sql.DataSource;

public class MyTag extends TagSupport {

    private StringBuffer sb;

    private @Resource DataSource ds1;
    private @Resource(name="myDataSource2") DataSource ds2;
    private DataSource ds3;

    @Resource(name="jdbc/myDataSource3")
    private void setDataSource(DataSource ds) {
        ds3 = ds;
    }

    @PostConstruct
    public void init() {
        sb = new StringBuffer();
        try {
            int loginTimeout = ds1.getLoginTimeout();
            sb.append("ds1-login-timeout=" + loginTimeout);
            loginTimeout = ds2.getLoginTimeout();
            sb.append(",ds2-login-timeout=" + loginTimeout);
            loginTimeout = ds3.getLoginTimeout();
            sb.append(",ds3-login-timeout=" + loginTimeout);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        } 

    }

    public int doStartTag() throws JspException {
        try {
          pageContext.getOut().print(sb.toString());
          return SKIP_BODY;
        } catch (IOException ioe) {
            throw new JspException(ioe);
        }
    }
}
