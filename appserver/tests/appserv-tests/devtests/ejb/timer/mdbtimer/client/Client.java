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

package com.sun.s1asdev.ejb.timer.mdbtimer.client;

import java.io.*;
import java.util.*;
import javax.ejb.EJBHome;
import javax.naming.*;
import javax.jms.*;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

    private static SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");

    public static void main (String[] args) {
        Client client = new Client(args);

        stat.addDescription("ejb-timer-mdbtimer");
        client.go();
        stat.printSummary("ejb-timer-mdbtimerID");
        System.exit(0);
    }

    private InitialContext context;
    private QueueConnection queueCon;
    private QueueSession queueSession;
    private QueueSender queueSender;
    private QueueReceiver queueReceiver;
    private javax.jms.Queue clientDest;
    private javax.jms.Queue targetDest;

    private int numMessages = 2;
    public Client(String[] args) {
        
        if( args.length == 1 ) {
            numMessages = new Integer(args[0]).intValue();
        }
    }

    public void go() {
        try {
            setup();
            doTest();
            stat.addStatus("mdbtimer main", stat.PASS);
        } catch(Throwable t) {
            stat.addStatus("mdbtimer main", stat.FAIL);
            t.printStackTrace();
        } finally {
            cleanup();
        }
    }

    public void setup() throws Exception {
        context = new InitialContext();
        
        QueueConnectionFactory queueConFactory = 
            (QueueConnectionFactory) context.lookup
            ("java:comp/env/FooCF");

        queueCon = queueConFactory.createQueueConnection();

        queueSession = queueCon.createQueueSession
            (false, Session.AUTO_ACKNOWLEDGE); 

        targetDest = (javax.jms.Queue) context.lookup("java:comp/env/jms/MsgBeanQueue");

        // Producer will be specified when actual msg is sent.
        queueSender = queueSession.createSender(targetDest);        

        clientDest = (javax.jms.Queue) context.lookup("java:comp/env/jms/ClientQueue");

        queueReceiver = queueSession.createReceiver(clientDest);

        queueCon.start();


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

    public void sendMsg(Message msg) 
        throws JMSException {
        System.out.println("Sending message to " +
                               " at time " + System.currentTimeMillis());
        queueSender.send(msg);
        System.out.println("Sent message " +
                           " at time " + System.currentTimeMillis());
    }

    public Message recvQueueMsg(long timeout) throws JMSException {
        System.out.println("Waiting for queue message ");
        Message recvdmessage = queueReceiver.receive(timeout);
        if( recvdmessage != null ) {
            System.out.println("Received message : " + recvdmessage + 
                               " at " + new Date());
        } else {
            System.out.println("timeout after " + timeout + " seconds");
            throw new JMSException("timeout" + timeout + " seconds");
        }
        return recvdmessage;
    }

    public void doTest() throws Exception {

        TextMessage message = queueSession.createTextMessage();
        message.setText("ejb-timer-mdbtimer");
        sendMsg(message);

        ObjectMessage recvMsg1 = (ObjectMessage) recvQueueMsg(20000);
        Date cancellation = (Date) recvMsg1.getObject();
        
        Date now = new Date();
        
        // wait for message after timer is cancelled (plus some buffer time)
        long wait = now.before(cancellation) ? 
            (cancellation.getTime() - now.getTime()) + 30000 : 0;
        System.out.println("Timer will be cancelled after " + cancellation);
        System.out.println("Waiting for cancellation notification until " +
                           new Date(now.getTime() + wait));

        ObjectMessage recvMsg2 = (ObjectMessage) recvQueueMsg(wait);
        System.out.println("got message after periodic timer was cancelled!!");
        System.out.println("Cancellation time = " + recvMsg2.getObject());

    }
}

