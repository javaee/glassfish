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
            jsw.print("ARGS:");
            for (int i=0; args!=null && i<args.length; i++) {
                jsw.print(args[i]);
            }
        } catch (IOException ioe) {
            throw new JspException(ioe.toString(), ioe);
        }

	return EVAL_PAGE;
    }

    public void setArray(String[] args) {
        this.args = args;
    }
}
