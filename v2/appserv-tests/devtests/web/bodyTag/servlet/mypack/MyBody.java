package mypack;

import javax.servlet.jsp.tagext.BodyTagSupport;
import javax.servlet.jsp.PageContext;


public class MyBody extends BodyTagSupport {
	
  public int doEndTag() {

    try {
        if (bodyContent != null)
            pageContext.getOut().print(bodyContent.getString());
    } catch (java.io.IOException ioe) {
    }
    return EVAL_PAGE;
  }

  public int doStartTag() {
      return EVAL_BODY_BUFFERED;
  }
}

