package taglib;

import java.io.IOException;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

public class MyTag extends TagSupport {

    private String[] args;

    public int doEndTag() throws JspException {

        JspWriter jsw = pageContext.getOut();
        try {
            jsw.print("Hello World");
        } catch (IOException ioe) {
            throw new JspException(ioe.toString(), ioe);
        }

        return EVAL_PAGE;
    }
}
