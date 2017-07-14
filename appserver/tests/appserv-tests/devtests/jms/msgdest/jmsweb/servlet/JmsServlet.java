/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2002-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
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
