package taglib;

import java.io.IOException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

public class MyTag extends TagSupport {

    public void release() {
        super.release();
        JspWriter jsw = pageContext.getOut();
        try {
            jsw.print("RELEASE CALLED");
        } catch (IOException ioe) {
        }
    }
}
