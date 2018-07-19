package example;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.SkipPageException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.*;
import java.io.IOException;

/**
 * Prints Hello World
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public class HelloWorldTag extends TagSupport {

    @Override public int doStartTag() throws JspException {
        try {
            pageContext.getOut().write("Hello World");
        } catch (IOException ioe) {
            throw new JspException(ioe);
        }
        return SKIP_BODY;
    }

    @Override public int doEndTag() throws JspException {
        return EVAL_PAGE;
    }

}
