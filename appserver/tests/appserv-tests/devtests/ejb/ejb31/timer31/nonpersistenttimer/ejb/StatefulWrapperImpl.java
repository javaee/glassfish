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

package com.sun.s1asdev.ejb31.timer.nonpersistenttimer;

import java.io.Serializable;
import java.rmi.NoSuchObjectException;
import java.util.Date;
import java.util.Properties;
import java.util.Set;

import javax.ejb.*;
//import javax.jms.*;
import javax.naming.*;
import javax.rmi.PortableRemoteObject;

//@javax.ejb.Stateful
public class StatefulWrapperImpl implements StatefulWrapper {

    private SessionContext context;
    private TimerStuff foo = null;

    /**
    private TopicConnection topicCon;
    private TopicSession topicSession;
    private TopicPublisher topicPublisher;
    private TopicSubscriber topicSubscriber;

    private QueueConnection queueCon;
    private QueueSession queueSession;
    private QueueSender queueSender;
    */

    public boolean doMessageDrivenTest(String jndiName, 
                                    boolean jms) {
        boolean result = false;
	/**
        if( jms ) { return; }

        try {
System.out.println("********PG-> in doMessageDrivenTest() for jndiName = " + jndiName );
           
            setup(setup);
System.out.println("********PG-> in doMessageDrivenTest() after setup");
            Context ic = new InitialContext();
	     Queue messageDrivenDest = (Queue) ic.lookup("java:comp/env/" + jndiName);

            System.out.println("Doing message driven tests for" + jndiName);

            String testName;
            int numTests = 6;
            Timer ths[] = new Timer[numTests];
            for(int i = 1; i < numTests; i++) {
                testName = "test" + i;
                System.out.println("Doing " + testName);
                ths[i] = foo.createTimer(1000000, testName);
                ObjectMessage objMsg = queueSession.createObjectMessage(ths[i]);
                sendMsgs(messageDrivenDest, objMsg, 1);
            }

            long sleepTime = 30000;
            System.out.println("Sleeping for " + sleepTime / 1000 + " seconds");
            Thread.sleep(sleepTime);
            
            // at this point, all foo timers should have been cancelled
            // by the message bean.
            foo.assertNoTimers();
            result = true;

        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            cleanup();
        }
	**/

        return result;
    }

    private void setup() throws Exception {
	/**
//PG->        context = new InitialContext();
        
        TopicConnectionFactory topicConFactory = 
            (TopicConnectionFactory) context.lookup
                ("java:comp/env/jms/MyTopicConnectionFactory");
                
System.out.println("********PG-> setup(): after  lookup");
        topicCon = topicConFactory.createTopicConnection();

System.out.println("********PG-> setup(): after  createTopicConnection");
        topicSession = 
            topicCon.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
System.out.println("********PG-> setup(): after  createTopicSession");
            
        // Producer will be specified when actual msg is published.
        topicPublisher = topicSession.createPublisher(null);
System.out.println("********PG-> setup(): after createPublisher");

        topicCon.start();
        System.out.println("********PG-> setup(): after start");

        QueueConnectionFactory queueConFactory = 
            (QueueConnectionFactory) context.lookup
            ("java:comp/env/jms/MyQueueConnectionFactory");

        queueCon = queueConFactory.createQueueConnection();

        queueSession = queueCon.createQueueSession
            (false, Session.AUTO_ACKNOWLEDGE); 

        // Producer will be specified when actual msg is sent.
        queueSender = queueSession.createSender(null);        

        queueCon.start();
	**/

    }

    private void cleanup() {
	/**
        try {
            if( topicCon != null ) {
                topicCon.close();
            }
            if( queueCon != null ) {
                queueCon.close();
            }
        } catch(Throwable t) {
            t.printStackTrace();
        }
        **/
    }
    /**
    public void publishMsgs(Topic topic, Message msg, int num) 
        throws JMSException {
        for(int i = 0; i < num; i++) {
            System.out.println("Publishing message " + i + " to " + topic);
            topicPublisher.publish(topic, msg);
        }
    }

    public void sendMsgs(Queue queue, Message msg, int num) 
        throws JMSException {
        for(int i = 0; i < num; i++) {
            System.out.println("Publishing message " + i + " to " + queue);
            queueSender.send(queue, msg);
        }
    }
    */

    public boolean doFooTest(String jndiName, boolean jms) {
        boolean result = false;
        try {
            Context ic = new InitialContext();
            Object fooObjref = ic.lookup("java:comp/env/" + jndiName);

            System.out.println("Doing foo timer test for " + jndiName);
            FooHome  fooHome = (FooHome)PortableRemoteObject.narrow
                (fooObjref, FooHome.class);

            foo = fooHome.create();
            if( jms ) {
                doJmsTest(foo);
            } else {
                doTest(foo);
            }

            result = true;
        } catch(Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    private void doJmsTest(TimerStuff timerStuff) throws Exception {
        timerStuff.sendMessageAndCreateTimer();
        timerStuff.recvMessageAndCreateTimer(true);
        timerStuff.sendMessageAndCreateTimerAndRollback();
        timerStuff.recvMessageAndCreateTimerAndRollback(false);
    }

    private void doTest(TimerStuff timerStuff) throws Exception {

        System.out.println("doTest(): creating the runtimeExTimer ");
        Timer runtimeExTimer =
            timerStuff.createTimer(1, "RuntimeException");

        System.out.println("doTest(): creating the timer");
        Timer timer = timerStuff.createTimer(1, 1);
        
        //
        System.out.println("doTest(): creating the timer2");
        Timer timer2 = timerStuff.createTimer(10000, 10000);
        
        //
        System.out.println("doTest(): creating the timer3");
        Timer timer3 = timerStuff.createTimer(new Date());
        
        //
        System.out.println("doTest(): creating the timer4");
        Timer timer4 = timerStuff.createTimer(new Date(new Date().getTime() + 2000));
        
        //
        System.out.println("doTest(): creating the timer5");
        Timer timer5 = timerStuff.createTimer(new Date(new Date().getTime() + 20000), 10000);

        System.out.println("doTest(): creating the createTimerAndRollback");
        timerStuff.createTimerAndRollback(20000);
        
        //
        System.out.println("doTest(): creating the createTimerAndCancel");
        timerStuff.createTimerAndCancel(20000);
        
        // @@@ reevaluate double cancel logic
        //timerStuff.createTimerAndCancelAndCancel(20000);
        
        //
        System.out.println("doTest(): creating the createTimerAndCancelAndRollback");
        timerStuff.createTimerAndCancelAndRollback(20000);
        
        //
        System.out.println("doTest(): creating the cancelTimer(timer2)");
        timerStuff.cancelTimer(timer2);
        System.out.println("doTest(): assertTimerNotactive(timer2)");
        timerStuff.assertTimerNotActive(timer2);

        //
        timerStuff.cancelTimerAndRollback(timer5);
        // @@@ reevaluate double cancel logic
        //timerStuff.cancelTimerAndCancelAndRollback(timer6);
        
        Timer timer7 = 
            timerStuff.createTimer(1, 1, "cancelTimer");
        Timer timer8 = 
            timerStuff.createTimer(1, 1, "cancelTimerAndRollback");
        Timer timer9 =         
            timerStuff.createTimer(1, "cancelTimerAndRollback");

        Timer timer11 = timerStuff.getTimeRemainingTest1(20);
        timerStuff.getTimeRemainingTest2(20, timer11);
        timerStuff.getTimeRemainingTest2(20, timer);
        
        Timer timer12 = timerStuff.getNextTimeoutTest1(20);
        timerStuff.getNextTimeoutTest2(20, timer12);
        timerStuff.getNextTimeoutTest2(20, timer);

        System.out.println("cancelling timer");
        timerStuff.cancelTimer(timer);

        System.out.println("cancelling timer5");
        timerStuff.cancelTimer(timer5);

        System.out.println("cancelling timer11");
        timerStuff.cancelTimer(timer11);

        System.out.println("cancelling timer12");
        timerStuff.cancelTimer(timer12);

        // It's possible that the following timers haven't expired yet
        try {
            timerStuff.cancelTimerNoError(timer8);
        } catch(Exception e) {
            e.printStackTrace();
        }

        try {
            timerStuff.cancelTimerNoError(timer3);
        } catch(Exception e) {
            e.printStackTrace();
        }
        try {
            timerStuff.cancelTimerNoError(timer4);
        } catch(Exception e) {
            e.printStackTrace();
        }

        try {
            timerStuff.cancelTimerNoError(timer7);
        } catch(Exception e) {
            e.printStackTrace();
        }

        try {
            timerStuff.cancelTimerNoError(runtimeExTimer);
        } catch(Exception e) {
            e.printStackTrace();
        }

        timerStuff.assertNoTimers();
    }

    public void removeFoo() throws java.rmi.RemoteException, javax.ejb.RemoveException {
        if (foo != null) {
            ((Foo) foo).remove();
        }
    }
}
