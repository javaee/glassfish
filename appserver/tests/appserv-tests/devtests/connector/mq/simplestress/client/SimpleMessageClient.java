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
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class SimpleMessageClient implements Runnable{

    private static SimpleReporterAdapter stat = 
        new SimpleReporterAdapter("appserv-tests");

    int id =0;

    public SimpleMessageClient(int i) {
        this.id = i;
    }

    public static void main(String[] args) {
        int NUM_CLIENTS = 50;
        int TIME_OUT = 30000;
        try {
            for (int i =0; i < NUM_CLIENTS; i++) {
                Thread client = new Thread(new SimpleMessageClient(i));
                client.start();
            }
        } catch (Throwable t) {
            t.printStackTrace();
            stat.addStatus("simple mdb main", stat.FAIL);
        }

        Context                 jndiContext = null;
        ConnectionFactory       connectionFactory = null;
        Connection              connection = null;
        Session                 session = null;
        Queue                   queue = null;
        MessageConsumer         msgConsumer = null;
        TextMessage             message = null;

        try {
            jndiContext = new InitialContext();
            connectionFactory = (ConnectionFactory)
                jndiContext.lookup
                ("java:comp/env/jms/CFactory");
            queue = (Queue) jndiContext.lookup("java:comp/env/jms/clientQueue");

            connection =
                connectionFactory.createConnection();
            session =
                connection.createSession(false,
                    Session.AUTO_ACKNOWLEDGE);
            connection.start();
            msgConsumer = session.createConsumer(queue);

            for (int i =0; i < NUM_CLIENTS; i++) {
                TextMessage msg = (TextMessage) msgConsumer.receive(TIME_OUT);
                if(msg==null) {
                    System.out.println("Received null so waiting.");
                    Thread.sleep(TIME_OUT);
                    msg = (TextMessage) msgConsumer.receive(TIME_OUT);
                }
                System.out.println("Received :::::: " + msg.getText());
            }
            stat.addStatus("Simple Stress test", stat.PASS);
        }catch (Throwable t) {
            t.printStackTrace();
            stat.addStatus("simple stress test", stat.FAIL);
        }finally {
            stat.printSummary("simple stress program");
            System.exit(0);
        }
         

    }

    public void run() {

        Context                 jndiContext = null;
        ConnectionFactory       connectionFactory = null;
        Connection              connection = null;
        Session                 session = null;
        Queue                   queue = null;
        MessageProducer         msgProducer = null;
        TextMessage             message = null;

        try {
            jndiContext = new InitialContext();
            connectionFactory = (ConnectionFactory)
                jndiContext.lookup
                ("java:comp/env/jms/CFactory");
            queue = (Queue) jndiContext.lookup("java:comp/env/jms/SampleQueue");

            connection =
                connectionFactory.createConnection();
            session =
                connection.createSession(false,
                    Session.AUTO_ACKNOWLEDGE);
            msgProducer = session.createProducer(queue);
            message = session.createTextMessage();

            message.setText("This is message " + id);
            System.out.println("Sending message: " +
                message.getText());
            msgProducer.send(message);
        } catch (Throwable e) {
            System.out.println("Exception occurred: " + e.toString());
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (JMSException e) {}
            } // if
        } // finally
    } // main
} // class

