/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 *
 * Contributor(s):
 *
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

package com.sun.s1asdev.cdi.hello.mdb.client;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueReceiver;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.InitialContext;

import test.ejb.session.HelloSless;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

    // in milli-seconds
    private static long TIMEOUT = 90000;

    private static SimpleReporterAdapter stat = new SimpleReporterAdapter(
            "appserv-tests");
    private static final String TEST_NAME = "request-and-application-scope-ejb-mdb";

    public static void main(String[] args) {
        Client client = new Client(args);

        stat.addDescription("cdi-hello-mdb");
        client.doTest();
        stat.printSummary("cdi-hello-mdbID");
        System.exit(0);
    }

    @Resource(name = "FooCF", mappedName = "jms/cdi_hello_mdb_QCF")
    private static QueueConnectionFactory queueConFactory;

    @Resource(name = "MsgBeanQueue", mappedName = "jms/cdi_hello_mdb_InQueue")
    private static javax.jms.Queue msgBeanQueue;

    @Resource(name = "ClientQueue", mappedName = "foo")
    private static javax.jms.Queue clientQueue;

    private QueueConnection queueCon;
    private QueueSession queueSession;
    private QueueSender queueSender;
    private QueueReceiver queueReceiver;

    // Only one target bean with Remote intf SlessSub so no linking info
    // necessary
    private static @EJB
    HelloSless helloStatelessRemoteBean;

    private int numMessages = 2;

    public Client(String[] args) {

        if (args.length == 1) {
            numMessages = new Integer(args[0]).intValue();
        }

    }

    private void doTest() {
        try {
            if (queueConFactory == null) {

                System.out.println("Java SE mode...");
                InitialContext ic = new InitialContext();
                queueConFactory = (javax.jms.QueueConnectionFactory) ic
                        .lookup("jms/cdi_hello_mdb_QCF");
                msgBeanQueue = (javax.jms.Queue) ic
                        .lookup("jms/cdi_hello_mdb_InQueue");
                clientQueue = (javax.jms.Queue) ic
                        .lookup("jms/cdi_hello_mdb_OutQueue");

            }

            setup();
            doTest(numMessages);
            stat.addStatus(TEST_NAME, stat.PASS);
        } catch (Throwable t) {
            stat.addStatus(TEST_NAME, stat.FAIL);
            t.printStackTrace();
        } finally {
            cleanup();
        }
    }

    private void setup() throws Exception {
        queueCon = queueConFactory.createQueueConnection();
        queueSession = queueCon.createQueueSession(false,
                Session.AUTO_ACKNOWLEDGE);
        // Producer will be specified when actual msg is sent.
        queueSender = queueSession.createSender(null);
        queueReceiver = queueSession.createReceiver(clientQueue);
        queueCon.start();
    }

    private void cleanup() {
        try {
            if (queueCon != null) {
                queueCon.close();
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private void sendMsgs(javax.jms.Queue queue, Message msg, int num)
            throws JMSException {
        for (int i = 0; i < num; i++) {
            System.out.println("Sending message " + i + " to " + queue
                    + " at time " + System.currentTimeMillis());
            queueSender.send(queue, msg);
            System.out.println("Sent message " + i + " to " + queue
                    + " at time " + System.currentTimeMillis());
        }
    }

    private void doTest(int num) throws Exception {

        Destination dest = msgBeanQueue;

        Message message = queueSession.createTextMessage("foo");

        message.setBooleanProperty("flag", true);
        message.setIntProperty("num", 2);
        sendMsgs((javax.jms.Queue) dest, message, num);
        
        System.out.println("remote bean:" + helloStatelessRemoteBean);
        String response = helloStatelessRemoteBean.hello();
        if (!response.equals("hello")){
            System.out.println("Expected hello, but instead got" + response);
            stat.addStatus(TEST_NAME, stat.FAIL);
        }

        System.out.println("Waiting for queue message");
        Message recvdmessage = queueReceiver.receive(TIMEOUT);
        if (recvdmessage != null) {
            System.out.println("Received message : "
                    + ((TextMessage) recvdmessage).getText());
        } else {
            System.out.println("timeout after " + TIMEOUT + " seconds");
            throw new JMSException("timeout" + TIMEOUT + " seconds");
        }
    }
}
