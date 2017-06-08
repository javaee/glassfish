/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2011-2017 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.s1asdev.ejb.mdb.singleton.client;

import java.io.*;
import java.util.*;
import javax.naming.*;
import javax.jms.*;
import javax.annotation.*;
import javax.ejb.*;
import com.sun.s1asdev.ejb.mdb.singleton.FooRemoteIF;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

/**
 * Tests for http://java.net/jira/browse/GLASSFISH-13004 (Support MDB singleton).
 * The test ejb jar is configured with property singleton-bean-pool=true in 
 * sun-ejb-jar.xml.  This test client calls fooTest and doTest(...):
 * fooTest: verify the stateless bean FooBean is single instance;
 * doTest(..): verify the mdb MessageBean is single instance.
 */
public class Client {

    private static SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");

    public static void main (String[] args) {
        Client client = new Client(args);

        stat.addDescription("ejb-mdb-singleton");
        client.doTest();
        stat.printSummary("ejb-mdb-singletonID");
        System.exit(0);
    }

    private InitialContext context;
    private QueueConnection queueCon;
    private QueueSession queueSession;
    private QueueSender queueSender;
    private QueueReceiver queueReceiver;
    private javax.jms.Queue clientQueue;

    private int numMessages = 1;
    private int numOfCalls = 120;

    @EJB
    private static FooRemoteIF foo;

    public Client(String[] args) {
        
        if( args.length == 1 ) {
            numMessages = new Integer(args[0]).intValue();
        }
    }

    public void doTest() {
        try {
            setup();
	    doTest("jms/ejb_mdb_singleton_InQueue", numMessages);
            fooTest();
            stat.addStatus("singleton main", stat.PASS);
        } catch(Throwable t) {
            stat.addStatus("singleton main", stat.FAIL);
            t.printStackTrace();
        } finally {
            cleanup();
        }
    }

    public void fooTest() {
        final Set<String> fooBeans = Collections.synchronizedSet(new HashSet<String>());
        Thread[] threads = new Thread[numOfCalls];
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(new Runnable() {
                public void run() {
                    fooBeans.add(foo.foo());
                }
            });
            threads[i].start();
        }

        for (int i = 0; i < threads.length; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
            }
        }
        verifySingleInstance(fooBeans);
    }

    public void setup() throws Exception {
        context = new InitialContext();
        QueueConnectionFactory queueConFactory = (QueueConnectionFactory) context.lookup ("java:comp/env/FooCF");
        queueCon = queueConFactory.createQueueConnection();
        queueSession = queueCon.createQueueSession(false, Session.AUTO_ACKNOWLEDGE); 
        queueSender = queueSession.createSender(null);        
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

    public void sendMsgs(javax.jms.Queue queue, Message msg, int num) 
        throws JMSException {
        for(int i = 0; i < num; i++) {
            //System.out.println("Sending message " + i + " to " + queue + 
            //                   " at time " + System.currentTimeMillis());
            queueSender.send(queue, msg);
           // System.out.println("Sent message " + i + " to " + queue + 
           //                    " at time " + System.currentTimeMillis());
        }
    }

    public void doTest(String destName, int num) throws Exception {
        Destination dest = (Destination) context.lookup(destName);
            
        for(int i = 0; i < numOfCalls; i++) {
            Message message = queueSession.createTextMessage(destName);
            //        Message message = topicSession.createTextMessage(destName);
            message.setBooleanProperty("flag", true);
            message.setIntProperty("num", i);
            sendMsgs((javax.jms.Queue) dest, message, num);
        }

        List<String> messageBeanInstances = new ArrayList<String>();
        int trials = 0;
        do {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }
            messageBeanInstances = foo.getMessageBeanInstances();
            ++trials;
        } while (messageBeanInstances.size() < numOfCalls && trials < 5);

        if(messageBeanInstances.size() <= 1 || messageBeanInstances.size() < numOfCalls) {
            throw new RuntimeException("Expecting number of instances " + numOfCalls + ", but got " +
                messageBeanInstances.size() + ": " + messageBeanInstances);
        }
        Set<String> messageBeanInstancesUnique = new HashSet<String>(messageBeanInstances);
        verifySingleInstance(messageBeanInstancesUnique);
    }

    private void verifySingleInstance(Collection<String> c) {
        if (c.size() == 1) {
            System.out.println("Got expected instances (single one): " + c);
        } else {
            throw new RuntimeException("Expecting single instance, but got " + c);
        }
    }
}

