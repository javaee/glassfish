package test;

import java.io.*;
import javax.annotation.Resource;
import javax.inject.Inject;
import javax.jms.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class TestServlet extends HttpServlet {
    @Resource(mappedName = "jms/jms_unit_test_Queue")
    private Queue queue;

    @Inject
    @JMSConnectionFactory("jms/jms_unit_test_QCF")
    @JMSSessionMode(JMSContext.AUTO_ACKNOWLEDGE)
    private JMSContext jmsContext;

    private static String requestScope = "around RequestScoped";

    public void doGet(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {
        boolean success = true;
        try {
            JMSProducer producer = jmsContext.createProducer();
            TextMessage msg = jmsContext.createTextMessage("Hello Servlet");
            producer.send(queue, msg);
            if(jmsContext.toString().indexOf(requestScope) == -1){
                throw new ServletException("NOT in requestScope scope!");
            }
        } catch (Exception e) {
            throw new ServletException(e);
        }

        res.getWriter().print(success);
    }
}
