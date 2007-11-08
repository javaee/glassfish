/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.s1asdev.ejb.ejb30.hello.mdb;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.jms.*;
import javax.annotation.Resource;

public class Servlet extends HttpServlet {

    @Resource(name="FooCF")
    private QueueConnectionFactory queueConFactory;
    
    @Resource(name="MsgBeanQueue") 
    private javax.jms.Queue msgBeanQueue;
    
    @Resource(name="ClientQueue")
    private javax.jms.Queue clientQueue;
    
    private QueueConnection queueCon;
    private QueueSession queueSession;
    private QueueSender queueSender;
    private QueueReceiver queueReceiver;

    private int numMessages = 1; 
    // in milli-seconds
    private static long TIMEOUT = 90000;



    public void  init() throws ServletException {
        
        super.init();
        log("init()...");
    }

    public void sendMsgs(javax.jms.Queue queue, Message msg, int num) throws JMSException {
        for(int i = 0; i < num; i++) {
            System.out.println("Sending message " + i + " to " + queue +
                               " at time " + System.currentTimeMillis());
            queueSender.send(queue, msg);
            System.out.println("Sent message " + i + " to " + queue +
                               " at time " + System.currentTimeMillis());
        }
    }

    
    public void service ( HttpServletRequest req , HttpServletResponse resp ) throws ServletException, IOException {
                 
        log("service()...");

        try {
            resp.setContentType("text/html");
            PrintWriter out = resp.getWriter();
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<p>");

            queueCon = queueConFactory.createQueueConnection();
        
            queueSession = queueCon.createQueueSession
                (false, Session.AUTO_ACKNOWLEDGE);
        
            // Producer will be specified when actual msg is sent.
            queueSender = queueSession.createSender(null);
        
            queueReceiver = queueSession.createReceiver(clientQueue);
        
            queueCon.start();


            Destination dest = msgBeanQueue;

            Message message = queueSession.createTextMessage("foo");

            message.setBooleanProperty("flag", true);
            message.setIntProperty("num", 1);
            sendMsgs((javax.jms.Queue) dest, message, numMessages);

            log("Waiting for queue message");
            Message recvdmessage = queueReceiver.receive(TIMEOUT);
            if( recvdmessage != null ) {
                log("Received message : " +
                                   ((TextMessage)recvdmessage).getText());
                out.println("Message is [" + recvdmessage + "]");
                out.println("</body>");
                out.println("</html>");
                out.flush();
                out.close();
            } else {
                log("timeout after " + TIMEOUT + " seconds");
                throw new JMSException("timeout" + TIMEOUT + " seconds");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("webclient servlet test failed");
            throw new ServletException(ex);
        } finally {
            cleanup();
        } 
    }

    
    public void cleanup() {
        try {
            if( queueCon != null ) {
                queueCon.close();
            }
        } catch(Throwable t) {
            t.printStackTrace();
        }
    }

    public void  destroy() {
        log("destroy()...");
    }
    
    public void log (String message) {
       System.out.println("[webclient Servlet]:: " + message);
    }  
}
