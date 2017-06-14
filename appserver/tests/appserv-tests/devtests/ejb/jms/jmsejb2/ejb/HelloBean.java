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

package com.sun.s1asdev.ejb.jms.jmsejb2;

import java.util.Enumeration;
import java.io.Serializable;
import java.rmi.RemoteException; 
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.ejb.EJBException;
import javax.naming.*;
import javax.jms.*;
import javax.transaction.UserTransaction;

public class HelloBean implements SessionBean {
    private String str;
    private SessionContext sc;
    private Queue queue;
    private QueueConnectionFactory qcFactory;
    private QueueConnection savedConnection;
    private QueueSession savedSession;

    public HelloBean() {}

    public void ejbCreate(String str) throws RemoteException {
	System.out.println("In ejbCreate !!");
        this.str = str;
        try {
            Context context = new InitialContext();

            System.out.println("HelloEJB has BEAN MANAGED TRANSACTIONS");

            queue = (Queue) context.lookup("java:comp/env/jms/QueueName");

            qcFactory = (QueueConnectionFactory) 
                context.lookup("java:comp/env/jms/MyQueueConnectionFactory");

        } catch(Exception e) {
            e.printStackTrace();
            throw new RemoteException();
        }
    }

    /**
     * Send message is a user-demarcated transaction without committing
     * the transaction.   A subsequent business method will commit the
     * transaction and receive the message.
     */
    public String sendMessageNoCommitPart1(String msg) throws EJBException {
        try {
            if( savedConnection == null ) {
                savedConnection = qcFactory.createQueueConnection();
                savedSession = savedConnection.createQueueSession(true, 0);
                savedConnection.start();
            }

            sc.getUserTransaction().begin();

            sendMessageInternal(savedSession, msg);

            System.out.println("Sent message " + msg);
        } catch(Exception e) {
            e.printStackTrace();
            throw new EJBException(e);
        }

        // NOTE : leave connection and session open.  they will be 
        // re-enlisted when the next business method is called since
        // the transaction is still active.

        return msg;
    }

    /**
     * Commit tx started in part1 and receive sent message in a new tx.
     */
    public void sendMessageNoCommitPart2() throws EJBException {

        try {

            // commit transaction started in Part1
            sc.getUserTransaction().commit();

            // start a new transaction
            sc.getUserTransaction().begin();

            Message message = recvMessageInternal(savedSession);

            if( message != null ) {
                System.out.println("Received message " + message);
                sc.getUserTransaction().commit();
            } else {
                throw new EJBException("no message received");
            }

        } catch(Exception e) {
            e.printStackTrace();
            try {
                sc.getUserTransaction().rollback();
            } catch(Exception re) {}

            throw new EJBException(e);
        } finally {
            try {
                if( savedConnection != null ) {
                    savedConnection.close();
                    savedConnection = null;
                    savedSession = null;
                }
            } catch(Exception e) { e.printStackTrace(); }
        }
    }

    private void sendMessageInternal(QueueSession session, String msg) 
        throws JMSException {
        // Create a message producer.
        QueueSender sender = session.createSender(queue);

        // Send a message.
        TextMessage message = session.createTextMessage();
        message.setText(msg);
        sender.send(message);
    }

    private Message recvMessageInternal(QueueSession session) 
        throws JMSException {
        // Create a message consumer
        QueueReceiver receiver = session.createReceiver(queue);
        System.out.println("Waiting for message on " + queue);
        Message message = receiver.receive(30000);
        return message;
    }

    public void setSessionContext(SessionContext sc) {
	this.sc = sc;
    }

    public void ejbRemove() throws RemoteException {}

    public void ejbActivate() {}

    public void ejbPassivate() {}
}
