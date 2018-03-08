/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.s1peqe.ejb.mdb.simple.client;

import javax.jms.*;
import javax.naming.*;

import org.testng.Assert;
import org.testng.annotations.Configuration;
import org.testng.annotations.Test;

public class SimpleMessageClient {

    QueueConnectionFactory  qcf = null;
    TopicConnectionFactory  tcf = null;
    Queue queue = null;
    Topic topic = null;

    final String  MSG_TEXT = "Test Message - ";
    final int NUM_MSGS = 3;

    public static void main(String[] args) {
        org.testng.TestNG testng = new org.testng.TestNG();
        testng.setTestClasses(
            new Class[] { com.sun.s1peqe.ejb.mdb.simple.client.SimpleMessageClient.class } );
        testng.run();
    }

    @Configuration(beforeTestClass = true)
    public void setup() throws Exception {
        InitialContext ic = new InitialContext();
        qcf = (QueueConnectionFactory) ic.lookup("jms/QCFactory");
        queue = (Queue) ic.lookup("jms/SampleQueue");

        tcf = (TopicConnectionFactory) ic.lookup("jms/TCFactory");
        topic = (Topic) ic.lookup("jms/SampleTopic");
    }

    @Test
    public void sendMessageToMDB() throws Exception {
        QueueConnection  queueConnection = null;
        QueueSession queueSession = null;
        QueueSender queueSender = null;
        TextMessage message = null;

        try {

            queueConnection = qcf.createQueueConnection();
            queueSession = queueConnection.createQueueSession(
                false, Session.CLIENT_ACKNOWLEDGE);
            queueSender = queueSession.createSender(queue);
            message = queueSession.createTextMessage();
            message.setJMSDeliveryMode(DeliveryMode.PERSISTENT);

            System.out.println("Created durable queue subscriber, " +
                               "persistent delivery mode");

            for (int i = 1; i <= NUM_MSGS; i++) {
                message.setText(MSG_TEXT + i);
                if(i == NUM_MSGS)
                    message.setStringProperty("MESSAGE_NUM","LAST");

                System.out.println("Sending message: " + message.getText());
                queueSender.send(message);
                Thread.sleep(100);
            }
        } finally {
	    if (queueSession !=null)
                try {
                    queueSession.close();
                } catch(JMSException ex) { }

            if (queueConnection !=null)
                try {
                    queueConnection.close();
                } catch(JMSException ex) { }
        }
    }

    @Test(dependsOnMethods = {"sendMessageToMDB"} )
    public void recvMessage() throws Exception {
        TopicConnection connect = null;
        TopicSubscriber subscriber = null;
        TopicSession session = null;

        TextMessage message = null;

	System.out.println("inside recvMessage of mdb appclient");

        try {
            connect = tcf.createTopicConnection();
            session = connect.createTopicSession(false,0);
            subscriber = session.createSubscriber(topic);
            System.out.println("Started subscriber");
            connect.start();

            int msgcount = 0;
            while (msgcount < NUM_MSGS) {
                Message msg = subscriber.receive(10000);
                if (msg != null) {
                    msgcount++;
                    if (msg instanceof TextMessage) {
                        message = (TextMessage) msg;
                        System.out.println("Received message:" +
                                           message.getText());
                    }
                }
            }
        } finally {
            if (session != null)
                try {
                    session.close();
                } catch (JMSException e) { }
            if (connect != null)
                try {
                    connect.close();
                } catch (JMSException e) { }
        }
    }
}




