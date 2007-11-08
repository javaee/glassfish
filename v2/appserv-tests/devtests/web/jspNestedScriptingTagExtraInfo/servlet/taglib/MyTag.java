package taglib;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.*;

public class MyTag extends TagSupport {

    private static int counter = 1; 

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int doStartTag() {
        pageContext.setAttribute(getName(), new Integer(counter++));
        return EVAL_BODY_INCLUDE;
    }

}
