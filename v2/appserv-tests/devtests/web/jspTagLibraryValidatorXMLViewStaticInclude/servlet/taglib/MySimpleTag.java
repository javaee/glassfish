package taglib;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.SimpleTagSupport;

public class MySimpleTag extends SimpleTagSupport {

    public void doTag() throws JspException, IOException {

        getJspContext().getOut().println("Hello World");
    }
}
