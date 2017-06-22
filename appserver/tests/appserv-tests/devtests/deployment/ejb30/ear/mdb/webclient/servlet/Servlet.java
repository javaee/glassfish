/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2005-2017 Oracle and/or its affiliates. All rights reserved.
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
