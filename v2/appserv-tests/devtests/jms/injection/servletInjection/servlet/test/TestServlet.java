package test;

import java.io.*;
import javax.annotation.Resource;
import javax.inject.Inject;
import javax.jms.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class TestServlet extends HttpServlet {
    @Resource(mappedName = "jms/ejb_jms_jmsejb_Queue")
    private Queue queue;

    @Inject
    @JMSConnectionFactory("jms/ejb_jms_jmsejb_QCF")
    @JMSSessionMode(JMSContext.AUTO_ACKNOWLEDGE)
    private JMSContext jmsContext;

    public void doGet(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {
        boolean success = true;
        try {
            JMSProducer producer = jmsContext.createProducer();
            TextMessage msg = jmsContext.createTextMessage("Hello Servlet");
            producer.send(queue, msg);
        } catch (Exception e) {
            throw new ServletException(e);
        }

        res.getWriter().print(success);
    }
}
