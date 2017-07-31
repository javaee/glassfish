/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.s1peqe.connector.mq.simplestress.client;

import javax.jms.*;
import javax.naming.*;
import java.util.*;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class SimpleMessageClient {
    static int TIME_OUT = 20000;
    static boolean debug = false;

    static String user = null;
    static String password = null;

    private static SimpleReporterAdapter stat = 
        new SimpleReporterAdapter("appserv-tests");

    int id =0;

    public SimpleMessageClient(int i) {
        this.id = i;
    }

    public static void main(String[] args) {
        System.out.println(args);

	//if (args.length >0) return;
	
	String testId = args[0];
	String testString = args[1];
	String verify = args[2];
	user = args[3];
	password = args[4];

        try {
            boolean pass = true;
	    System.out.println("Running the test : " + testId);

	    if (testString.startsWith("SEND")) { 
	        sendMessage();
	    }
	    
	    String output = null;
	    if (testString.endsWith("RECEIVE")) {
	        output = receiveMessage();
	    }

	    if (verify.equals("VERIFY")) {
	        pass = output.equals("REPLIED:CLIENT");
	    } else {
	        pass = (output == null);
	    }

            if (pass) {
                stat.addStatus(testId, stat.PASS);
            } else {
                stat.addStatus(testId, stat.FAIL);
	    }
        }catch (Throwable t) {
            t.printStackTrace();
            stat.addStatus(testId, stat.FAIL);
        }finally {
            stat.printSummary(testId);
            System.exit(0);
        }
         
    }

    public static String receiveMessage() {
        Context                 jndiContext = null;
        QueueConnectionFactory  queueConnectionFactory = null;
        QueueConnection         queueConnection = null;
        QueueSession            queueSession = null;
        Queue                   queue = null;
        QueueReceiver           queueReceiver = null;
        TextMessage             message = null;

        try {
            jndiContext = new InitialContext();
            queueConnectionFactory = (QueueConnectionFactory)
                jndiContext.lookup
                ("java:comp/env/jms/QCFactory");
            queue = (Queue) jndiContext.lookup("java:comp/env/jms/clientQueue");

            queueConnection =
                queueConnectionFactory.createQueueConnection(user,password);
            queueSession =
                queueConnection.createQueueSession(false,
                    Session.AUTO_ACKNOWLEDGE);
            queueConnection.start();
            queueReceiver = queueSession.createReceiver(queue);

	    HashMap map = new HashMap();

            long startTime = System.currentTimeMillis();
            boolean pass = true;
            TextMessage msg = (TextMessage) queueReceiver.receive(TIME_OUT);
	    if (msg == null) {
	       return null;
	    }
	    Integer id = new Integer(msg.getIntProperty("replyid"));
	    return msg.getText();
        }catch (Throwable t) {
            t.printStackTrace();
        }finally {
            if (queueConnection != null) {
                try {
                    queueConnection.close();
                } catch (JMSException e) {}
            } // if
        }
        return null;
    }

    public static void sendMessage() {

        Context                 jndiContext = null;
        QueueConnectionFactory  queueConnectionFactory = null;
        QueueConnection         queueConnection = null;
        QueueSession            queueSession = null;
        Queue                   queue = null;
        QueueSender             queueSender = null;
        TextMessage             message = null;

        try {
            jndiContext = new InitialContext();
            queueConnectionFactory = (QueueConnectionFactory)
                jndiContext.lookup
                ("java:comp/env/jms/QCFactory");
            queue = (Queue) jndiContext.lookup("java:comp/env/jms/SampleQueue");

            queueConnection =
                queueConnectionFactory.createQueueConnection(user,password);
            queueSession =
                queueConnection.createQueueSession(false,
                    Session.AUTO_ACKNOWLEDGE);
            queueSender = queueSession.createSender(queue);
            message = queueSession.createTextMessage();
            message.setText("CLIENT");
	    message.setIntProperty("id",1);
            queueSender.send(message);
	    debug("Send the message :" + message.getIntProperty("id") + ":" + message.getText());
        } catch (Throwable e) {
            System.out.println("Exception occurred: " + e.toString());
        } finally {
            if (queueConnection != null) {
                try {
                    queueConnection.close();
                } catch (JMSException e) {}
            } // if
        } // finally
    } // main

    static void debug(String msg) {
        if (debug) {
	   System.out.println(msg);
	}
    }
} // class

