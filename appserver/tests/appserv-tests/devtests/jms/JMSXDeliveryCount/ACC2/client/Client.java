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

package org.glassfish.test.jms.jmsxdeliverycount.client;

import javax.naming.*;
import javax.jms.*;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {
    private final static SimpleReporterAdapter STAT = new SimpleReporterAdapter("appserv-tests");

    public static void main (String[] args) {
        STAT.addDescription("JMSXDeliveryCount-acc1-2");
        Client client = new Client(args);
        client.sendMessages();
        client.doTest();
        STAT.printSummary("JMSXDeliveryCount-acc1-2ID");
    }

    public Client (String[] args) {
    }

    public void sendMessages() {
        try {
            Context ctx = new InitialContext();
            Queue queue = (Queue) ctx.lookup("jms/jms_unit_test_Queue");
            QueueConnectionFactory qconFactory = (QueueConnectionFactory) ctx.lookup("jms/jms_unit_test_QCF");
            QueueConnection conn = qconFactory.createQueueConnection();
            conn.start();
            QueueSession qsession = conn.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
            QueueSender qsender = qsession.createSender(queue);
            for (int i=0; i<10; i++) {
                TextMessage msg = qsession.createTextMessage("" + i);
                qsender.send(msg);
            }
            qsession.close();
            conn.close();
        } catch(Exception e) {
            e.printStackTrace();
            STAT.addStatus("JMSXDeliveryCount-acc1-2 ", STAT.FAIL);
        }
    }

    public void doTest() {
        try {
            Context ctx = new InitialContext();
            Queue queue = (Queue) ctx.lookup("jms/jms_unit_test_Queue");
            QueueConnectionFactory qconFactory = (QueueConnectionFactory) ctx.lookup("jms/jms_unit_test_QCF");
            QueueConnection qcon = qconFactory.createQueueConnection();
            qcon.start();
            QueueSession qsession = qcon.createQueueSession(true, Session.SESSION_TRANSACTED);
            MessageConsumer qreceiver = qsession.createConsumer(queue);
            TextMessage message = (TextMessage) qreceiver.receive(10000);
            if (message == null) {
                STAT.addStatus("JMSXDeliveryCount-acc1-2 ", STAT.FAIL);
                return;
            }
            int deliveryCount = message.getIntProperty("JMSXDeliveryCount");
            if (deliveryCount != 1) {
                STAT.addStatus("JMSXDeliveryCount-acc1-2 ", STAT.FAIL);
                return;
            }
            qreceiver.close();
            qsession.rollback();

            MessageConsumer qreceiver1 = qsession.createConsumer(queue);
            message = (TextMessage) qreceiver1.receive(10000);
            if (message == null) {
                STAT.addStatus("JMSXDeliveryCount-acc1-2 ", STAT.FAIL);
                return;
            }
            deliveryCount = message.getIntProperty("JMSXDeliveryCount");
            if (deliveryCount != 2) {
                STAT.addStatus("JMSXDeliveryCount-acc1-2 ", STAT.FAIL);
                return;
            }
            qreceiver1.close();
            qsession.rollback();

            MessageConsumer qreceiver2 = qsession.createConsumer(queue);
            message = (TextMessage) qreceiver2.receive(1000);
            if (message == null) {
                STAT.addStatus("JMSXDeliveryCount-acc1-2 ", STAT.FAIL);
                return;
            }
            deliveryCount = message.getIntProperty("JMSXDeliveryCount");
            if (deliveryCount != 3) {
                STAT.addStatus("JMSXDeliveryCount-acc1-2 ", STAT.FAIL);
                return;
            }
            qsession.commit();

            for (int i=1; i<10; i++) {
                message = (TextMessage) qreceiver2.receive(1000);
                if (message == null) {
                    STAT.addStatus("JMSXDeliveryCount-acc1-2 ", STAT.FAIL);
                    return;
                }
                deliveryCount = message.getIntProperty("JMSXDeliveryCount");
                if (deliveryCount != 1) {
                    STAT.addStatus("JMSXDeliveryCount-acc1-2 ", STAT.FAIL);
                    return;
                }
                qsession.commit();
            }
            qcon.close();
            STAT.addStatus("JMSXDeliveryCount-acc1-2 ", STAT.PASS);
        } catch(Exception e) {
            e.printStackTrace();
            STAT.addStatus("JMSXDeliveryCount-acc1-2 ", STAT.FAIL);
        }
    }
}
