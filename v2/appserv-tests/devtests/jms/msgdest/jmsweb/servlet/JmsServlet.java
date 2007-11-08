/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.s1asdev.jms.msgdest.jmsweb;

import java.io.*;
import java.rmi.RemoteException;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.naming.*;
import java.sql.*;
import javax.sql.*;
import javax.jms.*;
import javax.transaction.*;

public class JmsServlet extends HttpServlet {

    private Queue myQueue;
    private QueueConnectionFactory qcFactory;

    public void  init( ServletConfig config) throws ServletException {
        
        super.init(config);
        System.out.println("In jmsservlet... init()");
    }
    
    public void service ( HttpServletRequest req , HttpServletResponse resp ) throws ServletException, IOException {
                 
        resp.setContentType("text/html");
        PrintWriter out = resp.getWriter();
        
        try {

            InitialContext context = new InitialContext();

            UserTransaction userTx = (UserTransaction) 
                context.lookup("java:comp/UserTransaction");

            qcFactory = (QueueConnectionFactory) context.lookup("java:comp/env/jms/MyQueueConnectionFactory");
            myQueue = (Queue) context.lookup("java:comp/env/jms/MyQueue");

            userTx.begin();

            sendMessage("this is the jms servlet test");

            userTx.commit();

            userTx.begin();

            recvMessage();

            userTx.commit();

            out.println("<HTML> <HEAD> <TITLE> JMS Servlet Output </TITLE> </HEAD> <BODY BGCOLOR=white>");
            out.println("<CENTER> <FONT size=+1 COLOR=blue>DatabaseServelt :: All information I can give </FONT> </CENTER> <p> " );
            out.println("<FONT size=+1 color=red> Context Path :  </FONT> " + req.getContextPath() + "<br>" ); 
            out.println("<FONT size=+1 color=red> Servlet Path :  </FONT> " + req.getServletPath() + "<br>" ); 
            out.println("<FONT size=+1 color=red> Path Info :  </FONT> " + req.getPathInfo() + "<br>" ); 
            out.println("</BODY> </HTML> ");
            
        }catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("jmsservlet test failed");
            throw new ServletException(ex);
        } 
    }

    /**
     * Send a message. 
     */
    public String sendMessage(String msg) throws JMSException {
        QueueConnection connection = null;
        try {
            connection = qcFactory.createQueueConnection();
            QueueSession session = connection.createQueueSession
                (false, Session.AUTO_ACKNOWLEDGE);

            QueueSender sender = session.createSender(myQueue);

            // Send a message.
            TextMessage message = session.createTextMessage();
            message.setText(msg);
            sender.send(message);

            session.close();

        } finally {
            try {
                if( connection != null ) {
                    connection.close();
                }
            } catch(Exception e) {}
        }
        return msg;
    }

    private void recvMessage() throws JMSException {
        QueueConnection connection = null;
        try {
            connection = qcFactory.createQueueConnection();
            QueueSession session = connection.createQueueSession
                (false, Session.AUTO_ACKNOWLEDGE);

            connection.start();

            // Create a message consumer
            QueueReceiver receiver = session.createReceiver(myQueue);
            System.out.println("Waiting for message on " + myQueue);
            Message message = receiver.receive();
            System.out.println("Received message " + message);
        } finally {
            try {
                if( connection != null ) {
                    connection.close();
                }
            } catch(Exception e) {}
        }
    }
    
    public void  destroy() {
        System.out.println("in jmsservlet destroy");
    }
    
}
