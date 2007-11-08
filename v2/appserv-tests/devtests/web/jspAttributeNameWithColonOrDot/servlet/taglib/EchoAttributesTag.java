package test;

import java.util.ArrayList;
import java.util.Iterator;
import java.io.IOException;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;
import javax.servlet.jsp.tagext.DynamicAttributes;

/**
 * SimpleTag handler that echoes all its attributes 
 */
public class EchoAttributesTag 
    extends TagSupport
    implements DynamicAttributes
{
    private ArrayList keys;
    private ArrayList values;

    public EchoAttributesTag() {
        keys = new ArrayList();
        values = new ArrayList();
    }

    public int doEndTag() throws JspException {
        JspWriter out = pageContext.getOut();
        try {
            for (int i = 0; i < keys.size(); i++) {
                String key = (String) keys.get(i);
                Object value = values.get(i);
                out.print(key + "=" + value);
                if (i < keys.size()-1) {
                    out.print(",");
                }
            }
	} catch (IOException ioe) {
	    throw new JspException(ioe.toString(), ioe);
	}
      

        return EVAL_PAGE;
    }

    public void setDynamicAttribute(String uri, String localName,
                                    Object value) 
            throws JspException
    {
        keys.add(localName);
        values.add(value);
    }
}


