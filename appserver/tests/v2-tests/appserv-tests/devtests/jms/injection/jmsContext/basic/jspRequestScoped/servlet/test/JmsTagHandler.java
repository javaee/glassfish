/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package test;

import java.io.IOException;
import javax.annotation.Resource;
import javax.inject.Inject;
import javax.jms.*;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

/**
 *
 * @author LILIZHAO
 */
public class JmsTagHandler extends TagSupport {
    private String text;

    @Resource(mappedName = "jms/jms_unit_test_Queue")
    private Queue queue;

    @Inject
    @JMSConnectionFactory("jms/jms_unit_test_QCF")
    @JMSSessionMode(JMSContext.AUTO_ACKNOWLEDGE)
    private JMSContext jmsContext;

    private static String requestScope = "around RequestScoped";

    @Override
    public int doStartTag() throws JspException {
        try {
            System.out.println("jsp tag start...");

            JMSProducer producer = jmsContext.createProducer();
            TextMessage msg = jmsContext.createTextMessage("Hello JSP Tag");
            producer.send(queue, msg);
            System.out.println("JMSContext:"+jmsContext.toString());
            if(jmsContext.toString().indexOf(requestScope) == -1){
                throw new JspException("NOT in requestScope scope!");
            }
            //Get the writer object for output.
            JspWriter out = pageContext.getOut();
 
            //Perform substr operation on string.
            out.println(text);
 
        } catch (Exception e) {
            throw new JspException(e);
        }
        return SKIP_BODY;
    }
    public String getText() {
        return text;
    }
    public void setText(String text) {
        this.text = text;
    }
}
