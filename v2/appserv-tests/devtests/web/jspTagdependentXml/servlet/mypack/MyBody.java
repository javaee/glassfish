package mypack;

import javax.servlet.jsp.tagext.BodyTagSupport;
import javax.servlet.jsp.PageContext;


public class MyBody extends BodyTagSupport {
	
  String echoStr;
  String staticStr;

  public void setEcho(String s) {
    echoStr = s;
  }

  public String getEcho() {
    return echoStr;
  }

  public void setStatic(String s) {
    staticStr = s;
  }

  public String getStatic() {
    return staticStr;
  }

  public int doEndTag() {

    try {
        if (echoStr != null && ! echoStr.equals(""))
            pageContext.getOut().print(getEcho());
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

