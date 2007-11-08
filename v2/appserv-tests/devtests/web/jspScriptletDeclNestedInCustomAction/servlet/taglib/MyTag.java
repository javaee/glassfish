package taglib;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.*;

public class MyTag extends TagSupport {

    public int doStartTag() throws JspException {
        return EVAL_BODY_INCLUDE;
    }

}
